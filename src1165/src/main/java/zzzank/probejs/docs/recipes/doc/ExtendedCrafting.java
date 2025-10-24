package zzzank.probejs.docs.recipes.doc;

import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.docs.recipes.RecipeDocUtil;
import zzzank.probejs.lang.typescript.ScriptDump;

/**
 * @author ZZZank
 */
class ExtendedCrafting extends RecipeDocProvider {
    @Override
    public void addDocs(ScriptDump scriptDump) {
        add("shaped_table", RecipeDocUtil.basicShapedRecipe());
        add("shapeless_table", RecipeDocUtil.basicShapelessRecipe());
    }

    @Override
    public String namespace() {
        return "extendedcrafting";
    }
}
