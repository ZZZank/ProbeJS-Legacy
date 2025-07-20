package zzzank.probejs.docs.recipes.doc;

import com.google.common.base.Suppliers;
import me.shedaniel.architectury.platform.Platform;
import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSJoinedType;
import zzzank.probejs.lang.typescript.code.type.ts.TSArrayType;
import zzzank.probejs.lang.typescript.code.type.ts.TSClassType;

import java.util.function.Supplier;

import static zzzank.probejs.docs.recipes.RecipeDocUtil.*;

/**
 * @author ZZZank
 */
class Create extends RecipeDocProvider {

    private static final JSJoinedType.Union INGR_FLUID = INGR.or(FLUID);
    private static final JSJoinedType.Union STACK_FLUID = STACK.or(FLUID);
    private static final TSArrayType INGR_FLUID_N = INGR_FLUID.asArray();

    private static final Supplier<TSClassType> PROCESSING =
        Suppliers.memoize(() -> classType("dev.latvian.kubejs.create.ProcessingRecipeJS"));
    private static final Supplier<TSClassType> SEQUENCED_ASSEMBLY =
        Suppliers.memoize(() -> classType("dev.latvian.kubejs.create.SequencedAssemblyRecipeJS"));

    @Override
    public void addDocs(ScriptDump scriptDump) {
        add("crushing", recipeFn().outputs(STACK_N).input(INGR).returnType(PROCESSING.get()));
        add("milling", recipeFn().outputs(STACK_N).input(INGR).returnType(PROCESSING.get()));
        add(
            "compacting",
            recipeFn()
                .output(STACK_FLUID)
                .inputs(INGR_FLUID_N)
                .returnType(PROCESSING.get())
        );
        add(
            "mixing",
            recipeFn()
                .output(STACK_FLUID)
                .inputs(INGR_FLUID_N)
                .returnType(PROCESSING.get())
        );
        add(
            "pressing",
            recipeFn()
                .output(STACK)
                .input(INGR)
                .returnType(PROCESSING.get())
        );
        add(
            "deploying",
            recipeFn()
                .output(STACK)
                .input(INGR)
                .returnType(PROCESSING.get())
        );
        add(
            "cutting",
            recipeFn().output(STACK).input(INGR).returnType(PROCESSING.get())
        );
        add(
            "filling",
            recipeFn()
                .output(STACK)
                .param("input", Types.tuple().member("fluid", FLUID).member("base", INGR).build())
                .returnType(PROCESSING.get())
        );
        add(
            "sequenced_assembly",
            recipeFn()
                .output(STACK_N)
                .input(INGR)
                .param("sequence", PROCESSING.get().asArray())
                .returnType(SEQUENCED_ASSEMBLY.get())
        );
        add(
            "splashing", recipeFn()
                .output(STACK_N)
                .input(INGR)
                .returnType(PROCESSING.get())
        );
        add(
            "sandpaper_polishing",
            recipeFn()
                .output(STACK)
                .input(INGR)
                .returnType(PROCESSING.get())
        );
        add("mechanical_crafting", basicShapedRecipe(PROCESSING.get()));
        add(
            "emptying",
            recipeFn()
                .param("outputs", Types.tuple().member("item", STACK).member("fluid", FLUID).build())
                .input(INGR)
                .returnType(PROCESSING.get())
        );
    }

    @Override
    public String namespace() {
        return "create";
    }

    @Override
    public boolean shouldEnable() {
        return Platform.isModLoaded("kubejs_create") && super.shouldEnable();
    }
}
