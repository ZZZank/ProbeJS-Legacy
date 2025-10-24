package zzzank.probejs.docs.recipes.doc;

import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.docs.recipes.RecipeDocUtil;
import zzzank.probejs.lang.typescript.ScriptDump;

/**
 * @author ZZZank
 */
class DankStorage extends RecipeDocProvider {
    @Override
    public void addDocs(ScriptDump scriptDump) {
        add("upgrade", RecipeDocUtil.basicShapedRecipe());
    }

    @Override
    public String namespace() {
        return "dankstorage";
    }
}
