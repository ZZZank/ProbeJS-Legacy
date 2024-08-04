package zzzank.probejs.docs.recipes;

import me.shedaniel.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Map;

/**
 * a helper class for easing the usage of recipe doc
 * <p>
 * {@link RecipeDocProvider#namespace()} should return a valid ResourceLocation namespace, used by {@link RecipeDocProvider#add(String, JSLambdaType)}
 * to generate recipe type id
 * <p>
 * {@link RecipeDocProvider#addDocs(ScriptDump)} is where docs are actually added, you should use {@link RecipeDocProvider#recipeFn()}
 * to get a barebone for your recipe doc, and specify types of params/return, then call {@link JSLambdaType.Builder#build()}
 * and {@link RecipeDocProvider#add(String, JSLambdaType)} to actually add this doc
 * <p>
 * {@link RecipeDocProvider#shouldEnable()} is optional, used for determining if this recipe doc should be applied, you
 * need to overwrite it if this recipe doc requires multiple mods installed, or some other complex conditions
 *
 * @author ZZZank
 */
public abstract class RecipeDocProvider extends ProbeJSPlugin {

    protected Map<ResourceLocation, JSLambdaType> defined = null;

    public static JSLambdaType.Builder recipeFn() {
        return Types.lambda().methodStyle();
    }

    @Override
    public void addPredefinedRecipeDoc(ScriptDump scriptDump, Map<ResourceLocation, JSLambdaType> predefined) {
        if (!shouldEnable()) {
            return;
        }
        defined = predefined;
        addDocs(scriptDump);
    }

    public abstract void addDocs(ScriptDump scriptDump);

    public void add(String name, JSLambdaType doc) {
        defined.put(new ResourceLocation(namespace(), name), doc);
    }

    public abstract String namespace();

    public boolean shouldEnable() {
        return Platform.isModLoaded(namespace());
    }
}
