package zzzank.probejs.docs.recipes;

import dev.latvian.kubejs.recipe.mod.BotaniaRunicAltarRecipeJS;
import zzzank.probejs.docs.Primitives;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;


import static zzzank.probejs.docs.recipes.BuiltinRecipeDocs.STACK;
import static zzzank.probejs.docs.recipes.BuiltinRecipeDocs.INGR_N;

/**
 * @author ZZZank
 */
class Botania extends RecipeDocProvider {
    @Override
    public void addDocs(ScriptDump scriptDump) {
        add(
            "runic_altar",
            recipeFn()
                .param("output", STACK)
                .param("inputs", INGR_N)
                .param("mana", Primitives.INTEGER, true)
                .returnType(Types.type(BotaniaRunicAltarRecipeJS.class))
                .build()
        );
    }

    @Override
    public String namespace() {
        return "botania";
    }
}
