package zzzank.probejs.docs.recipes.doc;

import lombok.val;
import me.shedaniel.architectury.platform.Platform;
import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.docs.recipes.RecipeLambdaBuilder;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;

import static zzzank.probejs.docs.recipes.RecipeDocUtil.*;

/**
 * @author ZZZank
 */
class Thermal extends RecipeDocProvider {

    public static final BaseType MIXED_IN = Types.or(INGR, FLUID);
    public static final BaseType MIXED_OUT = Types.or(STACK, FLUID);

    public static RecipeLambdaBuilder catalystStyleRecipe() {
        return recipeFn()
            .input(INGR)
            .returnType(classType("dev.latvian.kubejs.thermal.CatalystRecipeJS"));
    }

    public static RecipeLambdaBuilder fuelStyleRecipe() {
        return recipeFn()
            .input(INGR)
            .returnType(classType("dev.latvian.kubejs.thermal.FuelRecipeJS"));
    }

    @Override
    public void addDocs(ScriptDump scriptDump) {
        //fuel
        add("compression_fuel", fuelStyleRecipe());
        add("lapidary_fuel", fuelStyleRecipe());
        add("magmatic_fuel", fuelStyleRecipe());
        add("numismatic_fuel", fuelStyleRecipe());
        add("stirling_fuel", fuelStyleRecipe());
        //catalyst
        add("insolator_catalyst", catalystStyleRecipe());
        add("pulverizer_catalyst", catalystStyleRecipe());
        add("smelter_catalyst", catalystStyleRecipe());
        //general
        val basicReturn = classType("dev.latvian.kubejs.thermal.BasicThermalRecipeJS");
        add("bottler",
            recipeFn().output(STACK)
                .input(selfOrArray(MIXED_IN))
                .returnType(basicReturn)
        );
        add("brewer",
            recipeFn().output(FLUID)
                .input(selfOrArray(MIXED_IN))
                .returnType(basicReturn)
        );
        add("centrifuge",
            recipeFn().output(selfOrArray(MIXED_OUT))
                .input(INGR)
                .returnType(basicReturn)
        );
        add("crucible",
            recipeFn().output(FLUID).input(INGR).returnType(basicReturn)
        );
        add("furnance",
            recipeFn().output(STACK).input(INGR).returnType(basicReturn)
        );
        add("insolator",
            recipeFn()
                .output(selfOrArray(STACK))
                .input(INGR)
                .returnType(basicReturn)
        );
        add("press",
            recipeFn()
                .outputs(selfOrArray(MIXED_OUT))
                .input(selfOrArray(INGR))
                .returnType(basicReturn)
        );
        add("pulverizer",
            recipeFn()
                .output(selfOrArray(STACK))
                .input(INGR)
                .returnType(basicReturn)
        );
        add("pyrolyzer",
            recipeFn()
                .outputs(selfOrArray(MIXED_OUT))
                .input(INGR)
                .returnType(basicReturn)
        );
        add("refinery",
            recipeFn()
                .outputs(selfOrArray(MIXED_OUT))
                .input(FLUID)
                .returnType(basicReturn)
        );
        add("sawmill",
            recipeFn()
                .outputs(selfOrArray(MIXED_OUT))
                .input(INGR)
                .returnType(basicReturn)
        );
        add("smelter",
            recipeFn()
                .outputs(selfOrArray(STACK))
                .inputs(selfOrArray(INGR))
                .returnType(basicReturn)
        );
    }

    @Override
    public String namespace() {
        return "thermal";
    }

    @Override
    public boolean shouldEnable() {
        return super.shouldEnable() && Platform.isModLoaded("kubejs_thermal");
    }
}
