package zzzank.probejs.docs.recipes.doc;

import dev.latvian.kubejs.recipe.mod.IDSqueezerRecipeJS;
import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;

import static zzzank.probejs.docs.recipes.RecipeDocUtil.INGR;
import static zzzank.probejs.docs.recipes.RecipeDocUtil.STACK_N;

/**
 * @author ZZZank
 */
class IntegratedDynamics extends RecipeDocProvider {
    @Override
    public void addDocs(ScriptDump scriptDump) {
        add(
            "squeezer",
            recipeFn().outputs(STACK_N).input(INGR).returnType(Types.type(IDSqueezerRecipeJS.class))
        );
        add(
            "mechanical_squeezer",
            recipeFn().outputs(STACK_N).input(INGR).returnType(Types.type(IDSqueezerRecipeJS.class))
        );
    }

    @Override
    public String namespace() {
        return "integrateddynamics";
    }
}
