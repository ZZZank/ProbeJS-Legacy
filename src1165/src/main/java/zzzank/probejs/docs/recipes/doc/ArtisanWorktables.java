package zzzank.probejs.docs.recipes.doc;

import dev.latvian.kubejs.recipe.mod.ShapedArtisanRecipeJS;
import dev.latvian.kubejs.recipe.mod.ShapelessArtisanRecipeJS;
import lombok.val;
import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;

import static zzzank.probejs.docs.recipes.RecipeDocUtil.basicShapedRecipe;
import static zzzank.probejs.docs.recipes.RecipeDocUtil.basicShapelessRecipe;

/**
 * @author ZZZank
 */
class ArtisanWorktables extends RecipeDocProvider {
    @Override
    public void addDocs(ScriptDump scriptDump) {
        val types = new String[]{
            "basic", "blacksmith", "carpenter", "chef", "chemist", "designer", "engineer", "farmer", "jeweler", "mage",
            "mason", "potter", "scribe", "tailor", "tanner"
        };
        val shapedReturn = Types.type(ShapedArtisanRecipeJS.class);
        val shapelessReturn = Types.type(ShapelessArtisanRecipeJS.class);
        for (val type : types) {
            add(type + "_shaped", basicShapedRecipe(shapedReturn));
            add(type + "_shapeless", basicShapelessRecipe(shapelessReturn));
        }
    }

    @Override
    public String namespace() {
        return "artisanworktables";
    }
}
