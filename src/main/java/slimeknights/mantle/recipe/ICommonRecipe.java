package slimeknights.mantle.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Extension of {@link Recipe} to set some methods that always set.
 * @param <C>  Inventory type
 */
public interface ICommonRecipe<C extends RecipeInput> extends Recipe<C> {
  @Override
  default ItemStack assemble(C inv, HolderLookup.Provider access) {
    return assemble(inv, (RegistryAccess) access);
  }

  /** Upstream signature; recipes implement this. */
  default ItemStack assemble(C inv, RegistryAccess access) {
    return getResultItem(access).copy();
  }

  @Override
  default ItemStack getResultItem(HolderLookup.Provider access) {
    return getResultItem((RegistryAccess) access);
  }

  /** Upstream signature; recipes implement this. */
  default ItemStack getResultItem(RegistryAccess access) {
    return ItemStack.EMPTY;
  }

  /** @deprecated Means nothing outside of crafting tables */
  @Deprecated
  @Override
  default boolean canCraftInDimensions(int width, int height) {
    return true;
  }

  /**
   * Returns true to hide this recipe from the recipe book. Needed until Forge has proper recipe book support.
   * @return  True
   */
  @Override
  default boolean isSpecial() {
    return true;
  }
}
