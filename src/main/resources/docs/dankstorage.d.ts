/**
 * @mod dankstorage
 */
 class RecipeHolder {
    dankstorage: Document.DankStorageRecipes;
}

/**
 * @mod dankstorage
 */
class DankStorageRecipes {
    upgrade(output: dev.latvian.kubejs.item.ItemStackJS, pattern: string[], items: { [key: string]: Internal.IngredientJS_ }): dev.latvian.kubejs.recipe.minecraft.ShapedRecipeJS;
}