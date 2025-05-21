package zzzank.probejs.docs;

import lombok.val;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.docs.assignments.*;
import zzzank.probejs.docs.bindings.Bindings;
import zzzank.probejs.docs.events.ForgeEvents;
import zzzank.probejs.docs.events.KubeEvents;
import zzzank.probejs.docs.events.RecipeEvents;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.lang.schema.SchemaDump;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.GameUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * Delegate calls to a set of internal ProbeJSPlugin to separate different
 * features, so docs can be added stateless
 */
public final class ProbeBuiltinDocs implements ProbeJSPlugin {

    private final ProbeJSPlugin[] builtinDocs;

    public ProbeBuiltinDocs() {
        builtinDocs = new ProbeJSPlugin[]{
            //type
            new RegistryTypes(),
            new SpecialTypes(),
            new Primitives(),
            new JavaPrimitives(),
            new RecipeTypes(),
            new WorldTypes(),
            new EnumTypes(),
            new KubeWrappers(),
            new FunctionalInterfaces(),
            new TypeRedirecting(),
            //binding
            new Bindings(),
            new LoadClassFn(),
            //event
            new KubeEvents(),
    //      new TagEvents(),
            new RecipeEvents(),
            new ForgeEvents(),
            //misc
            new KubeJSDenied(),
            new GlobalClasses(),
            new ParamFix(),
            new Snippets(),
            new SimulateOldTyping()
        };
    }

    public List<ProbeJSPlugin> getBuiltinDocs() {
        return Collections.unmodifiableList(Arrays.asList(builtinDocs));
    }

    private void forEach(Consumer<ProbeJSPlugin> consumer) {
        for (val builtinDoc : builtinDocs) {
            try {
                consumer.accept(builtinDoc);
            } catch (Throwable t) {
                ProbeJS.LOGGER.error("Error when applying builtin doc: {}", builtinDoc.getClass().getName());
                GameUtils.logThrowable(t);
                ProbeJS.LOGGER.error("If you found any problem in generated docs, please report to ProbeJS's github!");
            }
        }
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        forEach(builtinDoc -> builtinDoc.addGlobals(scriptDump));
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        forEach(builtinDoc -> builtinDoc.modifyFiles(files));
    }

    @Override
    public void assignType(ScriptDump scriptDump) {
        forEach(builtinDoc -> builtinDoc.assignType(scriptDump));
    }

    @Override
    public void addPredefinedTypes(TypeConverter converter) {
        forEach(builtinDoc -> builtinDoc.addPredefinedTypes(converter));
    }

    @Override
    public void denyTypes(Transpiler transpiler) {
        forEach(builtinDoc -> builtinDoc.denyTypes(transpiler));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> allClasses = new HashSet<>();
        forEach(builtinDoc -> allClasses.addAll(builtinDoc.provideJavaClass(scriptDump)));
        return allClasses;
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        forEach(builtinDoc -> builtinDoc.addVSCodeSnippets(dump));
    }

    @Override
    public void addJsonSchema(SchemaDump dump) {
        forEach(builtinDoc -> builtinDoc.addJsonSchema(dump));
    }

    @Override
    public void addPredefinedRecipeDoc(ScriptDump scriptDump, Map<ResourceLocation, JSLambdaType> predefined) {
        forEach(builtinDoc -> builtinDoc.addPredefinedRecipeDoc(scriptDump, predefined));
    }

    @Override
    public void denyBindings(BindingFilter filter) {
        forEach(builtinDoc -> builtinDoc.denyBindings(filter));
    }
}
