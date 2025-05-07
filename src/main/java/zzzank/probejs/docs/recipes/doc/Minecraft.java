package zzzank.probejs.docs.recipes.doc;

import dev.latvian.kubejs.recipe.minecraft.SmithingRecipeJS;
import dev.latvian.kubejs.recipe.minecraft.StonecuttingRecipeJS;
import lombok.val;
import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.lang.typescript.ScriptDump;

import static zzzank.probejs.docs.recipes.RecipeDocUtil.*;

/**
 * @author ZZZank
 */
class Minecraft extends RecipeDocProvider {

    @Override
    public void addDocs(ScriptDump scriptDump) {
        val converter = scriptDump.transpiler.typeConverter;
        add("smelting", basicCookingRecipe());
        add("smoking", basicCookingRecipe());
        add("blasting", basicCookingRecipe());
        add("campfire_cooking", basicCookingRecipe());
        add("crafting_shaped", basicShapedRecipe());
        add("crafting_shapeless", basicShapelessRecipe());
        add(
            "stonecutting",
            recipeFn().output(STACK)
                .inputs(INGR_N)
                .returnType(converter.convertType(StonecuttingRecipeJS.class))
        );
        add(
            "smithing",
            recipeFn().output(STACK)
                .param("base", INGR)
                .param("addition", INGR)
                .returnType(converter.convertType(SmithingRecipeJS.class))
        );
    }

    @Override
    public String namespace() {
        return "minecraft";
    }

    @Override
    public boolean shouldEnable() {
        return true;
    }
}
