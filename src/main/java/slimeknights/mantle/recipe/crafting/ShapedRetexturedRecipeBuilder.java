package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;


@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fromShaped")
public class ShapedRetexturedRecipeBuilder {
  private final ShapedRecipeBuilder parent;
  private Ingredient texture = null;
  private char textureKey = '\0';
  private boolean matchAll = false;

  /**
   * Sets the texture source to the given ingredient
   * @param texture Ingredient to use for texture
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setSource(Ingredient texture) {
    this.texture = texture;
    this.textureKey = '\0';
    return this;
  }

  /**
   * Sets the texture source to the given tag
   * @param tag Tag to use for texture
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setSource(TagKey<Item> tag) {
    return setSource(Ingredient.of(tag));
  }

  /** Sets the texture source to a key from the texture map. Is not validated as that is too much work. */
  public ShapedRetexturedRecipeBuilder setSource(char textureKey) {
    this.textureKey = textureKey;
    this.texture = null;
    return this;
  }

  /**
   * Sets the match first property on the recipe.
   * If set, the recipe uses the first ingredient match for the texture. If unset, all items that match the ingredient must be the same or no texture is applied
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setMatchAll() {
    this.matchAll = true;
    return this;
  }

  /**
   * Builds the recipe with the default name using the given consumer
   * @param consumer Recipe consumer
   */
  public void build(RecipeOutput output) {
    this.validate();
    parent.save(wrap(output));
  }

  /**
   * Builds the recipe using the given consumer
   * @param consumer Recipe consumer
   * @param location Recipe location
   */
  public void build(RecipeOutput output, ResourceLocation location) {
    this.validate();
    parent.save(wrap(output), location);
  }

  /**
   * Ensures this recipe can be built
   * @throws IllegalStateException If the recipe cannot be built
   */
  private void validate() {
    if (texture == null && textureKey == '\0') {
      throw new IllegalStateException("No texture defined for texture recipe");
    }
  }

  private RecipeOutput wrap(RecipeOutput output) {
    return new RecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
        ShapedRecipe shaped = (ShapedRecipe)recipe;
        output.accept(id, new ShapedRetexturedRecipe(shaped, textureSource(shaped), texture == null ? textureKey : '\0', matchAll), advancement, conditions);
      }

      @Override
      public Advancement.Builder advancement() {
        return output.advancement();
      }
    };
  }

  /** Resolves a legacy pattern symbol from the shaped recipe's codec data. */
  private Ingredient textureSource(ShapedRecipe recipe) {
    if (texture != null) {
      return texture;
    }
    JsonObject json = ShapedRecipe.Serializer.CODEC.codec().encodeStart(JsonOps.INSTANCE, recipe)
      .getOrThrow(JsonParseException::new).getAsJsonObject();
    JsonObject key = json.getAsJsonObject("key");
    JsonElement source = key == null ? null : key.get(String.valueOf(textureKey));
    if (source == null) {
      throw new IllegalStateException("Texture symbol '" + textureKey + "' is not defined in the shaped recipe");
    }
    return Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, source).getOrThrow(JsonParseException::new);
  }
}
