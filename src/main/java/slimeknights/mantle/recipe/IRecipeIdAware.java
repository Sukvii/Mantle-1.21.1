package slimeknights.mantle.recipe;

import net.minecraft.resources.ResourceLocation;

/**
 * A recipe that keeps the ID supplied by its {@code RecipeHolder}.
 *
 * <p>In 1.21 the holder owns the canonical recipe ID, while Mantle's recipe
 * implementations still expose an ID for existing integrations such as JEI.
 * The ID is therefore assigned after the holder has been created.</p>
 */
public interface IRecipeIdAware {
  /** Gets the currently assigned recipe ID. */
  ResourceLocation getId();

  /** Updates the ID assigned by the recipe manager. */
  void setId(ResourceLocation id);
}
