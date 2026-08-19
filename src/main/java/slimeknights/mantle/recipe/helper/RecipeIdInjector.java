package slimeknights.mantle.recipe.helper;

import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import slimeknights.mantle.recipe.IRecipeIdAware;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** Restores Mantle's recipe-owned IDs from the canonical 1.21 recipe holders. */
public final class RecipeIdInjector {
  private RecipeIdInjector() {}

  /** Registers the server-side listener used after every recipe reload. */
  public static void init() {
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AddReloadListenerEvent.class, event ->
      event.addListener(new ReloadListener(event.getServerResources().getRecipeManager())));
  }

  /** Applies holder IDs to recipes that retain an ID for compatibility APIs. */
  public static void inject(RecipeManager manager) {
    for (RecipeHolder<?> holder : manager.getRecipes()) {
      Recipe<?> recipe = holder.value();
      if (recipe instanceof IRecipeIdAware idAware) {
        idAware.setId(holder.id());
      }
    }
  }

  private record ReloadListener(RecipeManager manager) implements PreparableReloadListener {
    @Override
    public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager,
                                          ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
      return stage.wait(Unit.INSTANCE).thenRunAsync(() -> inject(manager), gameExecutor);
    }
  }
}
