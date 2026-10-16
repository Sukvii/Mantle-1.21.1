package slimeknights.mantle.loot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.EventPriority;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.listener.IEarlyReloadListener;
import slimeknights.mantle.loot.LootTableInjection.LootPoolInjection;
import slimeknights.mantle.util.JsonHelper;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Class handling injecting additional entries into loot tables */
public enum LootTableInjector implements IEarlyReloadListener {
  INSTANCE;

  /** Datapack folder for the injector */
  public static final String FOLDER = "mantle/loot_injectors";

  /** Initializes the loot table injector */
  public static void init() {
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AddReloadListenerEvent.class, event -> {
      event.addListener(INSTANCE);
      INSTANCE.context = event.getConditionContext();
      // 1.21 parses loot tables before reload listeners run, so inject into this reload's tables after loading
      INSTANCE.registryAccess = event.getRegistryAccess();
    });
  }

  /** Registry access of the reload in progress, containing the loot tables it just parsed */
  private volatile RegistryAccess registryAccess = RegistryAccess.EMPTY;

  /** Condition context for preventing load */
  private IContext context = IContext.EMPTY;
  /** Map of injections to use on loot table load */
  private Map<ResourceLocation,LootTableInjection> injections = Collections.emptyMap();

  @Override
  public void onResourceManagerReload(ResourceManager manager) {
    long time = System.nanoTime();
    Map<ResourceLocation,LootTableInjection.Builder> builders = new HashMap<>();
    int loaded = 0;
    for (Entry<ResourceLocation,Resource> entry : manager.listResources(FOLDER, loc -> loc.getPath().endsWith(".json")).entrySet()) {
      try (Reader reader = entry.getValue().openAsReader()) {
        JsonObject json = GsonHelper.fromJson(JsonHelper.DEFAULT_GSON, reader, JsonObject.class);
        if (json != null) {
          // skip if empty for easy removals
          if (!json.keySet().isEmpty() && (!json.has("conditions") || ICondition.LIST_CODEC.parse(JsonOps.INSTANCE, json.get("conditions"))
              .getOrThrow(JsonParseException::new).stream().allMatch(condition -> condition.test(context)))) {
            // the builder allows us to merge from multiple sources, for efficiency
            // ensures a given table name and pool name both show just once
            LootTableInjection injection = LootTableInjection.LOADABLE.deserialize(json);
            LootTableInjection.Builder builder = builders.computeIfAbsent(injection.name(), id -> new LootTableInjection.Builder());
            for (LootPoolInjection pool : injection.pools()) {
              builder.addToPool(pool);
            }
            loaded++;
          }
        } else {
          Mantle.logger.error("Couldn't parse loot table injection from {} as it's null or empty", entry.getKey());
        }
      } catch (IllegalArgumentException | IOException | JsonParseException ex) {
        Mantle.logger.error("Couldn't parse loot injection from {}", entry.getKey(), ex);
      }
    }
    // build final map
    injections = builders.entrySet().stream().map(entry -> entry.getValue().build(entry.getKey()))
                         .collect(Collectors.toUnmodifiableMap(LootTableInjection::name, Function.identity()));
    // log timer
    Mantle.logger.info("Loaded {} loot table injectors injecting into {} tables in {} ms", loaded, injections.size(), (System.nanoTime() - time) / 1000000f);
    injectIntoLoadedTables();
  }

  /** Injects into the tables of the current reload; they are not visible to the running server until the reload completes */
  private void injectIntoLoadedTables() {
    RegistryAccess access = registryAccess;
    registryAccess = RegistryAccess.EMPTY; // do not retain the reload's registries
    Registry<LootTable> tables = access.registryOrThrow(Registries.LOOT_TABLE);
    for (LootTableInjection injection : injections.values()) {
      LootTable table = tables.get(injection.name());
      if (table == null) {
        Mantle.logger.warn("Failed to inject loot into missing table {}", injection.name());
        continue;
      }
      Mantle.logger.debug("Injecting into {} pools in the table {}", injection.pools().size(), injection.name());
      for (LootPoolInjection pool : injection.pools()) {
        pool.inject(table);
      }
    }
  }
}
