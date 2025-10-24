package zzzank.probejs.docs.recipes.doc;

import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.lang.typescript.ScriptDump;

import static zzzank.probejs.docs.recipes.RecipeDocUtil.basicShapedRecipe;

/**
 * @author ZZZank
 */
class Cucumber extends RecipeDocProvider {
    @Override
    public void addDocs(ScriptDump scriptDump) {
        add("shaped_no_mirror", basicShapedRecipe());
    }

    @Override
    public String namespace() {
        return "cucumber";
    }
}
