package zzzank.probejs.docs.recipes.doc;

import lombok.val;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class BuiltinRecipeDocs implements ProbeJSPlugin {

    public static boolean ENABLED = true;

    public static final List<Supplier<ProbeJSPlugin>> ALL = new ArrayList<>(Arrays.asList(
        ArsNouveau::new,
        ArtisanWorktables::new,
        Botania::new,
        BotanyPots::new,
        Create::new,
        Cucumber::new,
        CustomMachinery::new,
        DankStorage::new,
        ExtendedCrafting::new,
        IntegratedDynamics::new,
        KubeJS::new,
        Minecraft::new,
        Thermal::new
    ));

    @Override
    public void addPredefinedRecipeDoc(ScriptDump scriptDump, Map<ResourceLocation, JSLambdaType> predefined) {
        if (!ENABLED) {
            return;
        }
        for (val supplier : ALL) {
            supplier.get().addPredefinedRecipeDoc(scriptDump, predefined);
        }
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (!ENABLED) {
            return;
        }
        for (val supplier : ALL) {
            supplier.get().modifyClasses(scriptDump, globalClasses);
        }
    }
}
