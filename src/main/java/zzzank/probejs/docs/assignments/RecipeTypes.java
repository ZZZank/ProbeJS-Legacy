package zzzank.probejs.docs.assignments;

import dev.latvian.kubejs.item.ingredient.IngredientJS;
import dev.latvian.kubejs.recipe.filter.RecipeFilter;
import dev.latvian.kubejs.recipe.ingredientaction.IngredientActionFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Collections;
import java.util.Set;

public class RecipeTypes implements ProbeJSPlugin {
    @Override
    public void assignType(ScriptDump scriptDump) {

//        scriptDump.assignType(ItemPredicate.class, Types.type(Item.class));
//        scriptDump.assignType(ItemPredicate.class, Types.literal("*"));
//        scriptDump.assignType(ItemPredicate.class, Types.literal("-"));
//        scriptDump.assignType(ItemPredicate.class, Types.lambda()
//                .param("item", Types.type(ItemStack.class))
//                .returnType(Types.BOOLEAN)
//                .build());

//        scriptDump.assignType(SizedIngredient.class, Types.type(ItemStack.class));

        scriptDump.assignType(RecipeFilter.class, Types.primitive("RegExp"));
        scriptDump.assignType(RecipeFilter.class, Types.literal("*"));
        scriptDump.assignType(RecipeFilter.class, Types.literal("-"));
        scriptDump.assignType(RecipeFilter.class, Types.type(RecipeFilter.class).asArray());
        scriptDump.assignType(
            RecipeFilter.class,
            "RecipeFilterObject",
            Types.object()
                .member("exact", true, Types.BOOLEAN)
                .member("or", true, Types.type(RecipeFilter.class))
                .member("not", true, Types.type(RecipeFilter.class))
                .member("id", true, Types.primitive("Special.RecipeId"))
                .member("type", true, Types.primitive("Special.RecipeType"))
                .member("group", true, Types.STRING)
                .member("mod", true, Types.primitive("Special.Mod"))
                .member("input", true, Types.type(Ingredient.class))
                .member("output", true, Types.type(ItemStack.class))
                .build()
        );

        scriptDump.assignType(IngredientActionFilter.class, Types.NUMBER);
        scriptDump.assignType(IngredientActionFilter.class, Types.type(IngredientJS.class));
        scriptDump.assignType(IngredientActionFilter.class,
            Types.object()
                .member("index", true, Types.NUMBER)
                .member("itemFilter", true, Types.type(IngredientJS.class))
                .build()
        );

        // Note that this is fluid ingredient without amount
//        scriptDump.assignType(FluidIngredientJS.class, Types.type(Fluid.class));
//        scriptDump.assignType(FluidIngredientJS.class, Types.primitive("`#${Special.FluidTag}`"));
//        scriptDump.assignType(FluidIngredientJS.class, Types.primitive("`@${Special.Mod}`"));
//        scriptDump.assignType(FluidIngredientJS.class, Types.primitive("RegExp"));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return Collections.singleton(RecipeFilter.class);
    }
}
