package zzzank.probejs.docs.recipes.doc;

import dev.latvian.kubejs.recipe.mod.BotanyPotsCropRecipeJS;
import zzzank.probejs.docs.Primitives;
import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;

import static zzzank.probejs.docs.recipes.RecipeDocUtil.INGR;
import static zzzank.probejs.docs.recipes.RecipeDocUtil.STACK;

/**
 * @author ZZZank
 */
class BotanyPots extends RecipeDocProvider {
    @Override
    public void addDocs(ScriptDump scriptDump) {
        add("crop", recipeFn()
            .param(
                "outputs",
                STACK.or(Types.object()
                    .member("item", STACK)
                    .member("minRolls", Primitives.INTEGER)
                    .member("maxRolls", Primitives.INTEGER)
                    .build())
            )
            .input(INGR)
            .returnType(Types.type(BotanyPotsCropRecipeJS.class))
        );
    }

    @Override
    public String namespace() {
        return "botanypots";
    }
}
