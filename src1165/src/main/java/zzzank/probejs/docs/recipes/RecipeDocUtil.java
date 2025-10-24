package zzzank.probejs.docs.recipes;

import dev.latvian.kubejs.fluid.FluidStackJS;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.item.ingredient.IngredientJS;
import dev.latvian.kubejs.recipe.minecraft.CookingRecipeJS;
import dev.latvian.kubejs.recipe.minecraft.ShapedRecipeJS;
import dev.latvian.kubejs.recipe.minecraft.ShapelessRecipeJS;
import lombok.val;
import zzzank.probejs.docs.Primitives;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;
import zzzank.probejs.lang.typescript.code.type.js.JSObjectType;
import zzzank.probejs.lang.typescript.code.type.ts.TSArrayType;
import zzzank.probejs.lang.typescript.code.type.ts.TSClassType;

/**
 * @author ZZZank
 */
public interface RecipeDocUtil {
    TSClassType FLUID = Types.type(FluidStackJS.class);
    TSClassType STACK = Types.type(ItemStackJS.class);
    TSArrayType STACK_N = STACK.asArray();
    TSClassType INGR = Types.type(IngredientJS.class);
    TSArrayType INGR_N = Types.array(INGR);
    JSObjectType STR2INGR = Types.object().literalMember("[x in string]", INGR).build();
    TSArrayType STR_N = Types.array(Primitives.CHAR_SEQUENCE);

    static TSClassType classType(String className) {
        try {
            val c = Class.forName(className);
            return Types.type(c);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static JSLambdaType basicShapedRecipe() {
        return basicShapedRecipe(Types.type(ShapedRecipeJS.class));
    }

    static JSLambdaType basicShapedRecipe(BaseType returnType) {
        return RecipeDocProvider.recipeFn()
            .output(STACK)
            .param("pattern", STR_N)
            .param("items", STR2INGR)
            .returnType(returnType)
            .build();
    }

    static RecipeLambdaBuilder basicShapelessRecipe() {
        return basicShapelessRecipe(Types.type(ShapelessRecipeJS.class));
    }

    static RecipeLambdaBuilder basicShapelessRecipe(BaseType returnType) {
        return RecipeDocProvider.recipeFn()
            .output(STACK)
            .inputs(INGR_N)
            .returnType(returnType);
    }

    static RecipeLambdaBuilder basicCookingRecipe(BaseType returnType) {
        return RecipeDocProvider
            .recipeFn()
            .output(STACK)
            .input(INGR)
            .returnType(returnType);
    }

    static RecipeLambdaBuilder basicCookingRecipe() {
        return basicCookingRecipe(Types.type(CookingRecipeJS.class));
    }

    static BaseType selfOrArray(BaseType type) {
        return Types.or(type, Types.array(type));
    }
}
