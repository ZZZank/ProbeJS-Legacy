package zzzank.probejs.docs.recipes.doc;

import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.docs.Primitives;
import zzzank.probejs.docs.recipes.RecipeDocProvider;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;

import static zzzank.probejs.docs.recipes.RecipeDocUtil.classType;

/**
 * @author ZZZank
 */
class CustomMachinery extends RecipeDocProvider {
    @Override
    public void addDocs(ScriptDump scriptDump) {
        add(
            "custom_machine",
            recipeFn()
                .param("machine_id", Types.type(ResourceLocation.class))
                .param("duration", Primitives.INTEGER)
                .returnType(classType("fr.frinn.custommachinery.common.integration.kubejs.CustomMachineJSRecipeBuilder"))
        );
    }

    @Override
    public String namespace() {
        return "custommachinery";
    }
}
