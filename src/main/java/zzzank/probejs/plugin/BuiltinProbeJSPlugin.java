package zzzank.probejs.plugin;

import dev.latvian.kubejs.KubeJSPlugin;
import dev.latvian.kubejs.script.BindingsEvent;
import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.util.ClassFilter;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.events.*;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformerRegistration;
import zzzank.probejs.lang.transpiler.transformation.impl.*;
import zzzank.probejs.lang.typescript.DumpSpecificFiles;
import zzzank.probejs.lang.typescript.ScriptDump;

public class BuiltinProbeJSPlugin extends KubeJSPlugin implements ProbeJSPlugin {

    @Override
    public void addBindings(BindingsEvent event) {
        event.add("require", new Require(event.manager));
    }

    @Override
    public void addClasses(ScriptType type, ClassFilter filter) {
        // lol
        filter.deny("org.jetbrains.java.decompiler");
        filter.deny("com.github.javaparser");
        filter.deny("org.java_websocket");
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        new AddGlobalEventJS(scriptDump).post(ScriptType.CLIENT, ProbeEvents.ADD_GLOBAL);
    }

    @Override
    public void assignType(ScriptDump scriptDump) {
        new TypeAssignmentEventJS(scriptDump).post(ScriptType.CLIENT, ProbeEvents.ASSIGN_TYPE);
    }

    @Override
    public void modifyFiles(DumpSpecificFiles files) {
        new TypingModificationEventJS(files).post(ScriptType.CLIENT, ProbeEvents.MODIFY_DOC);
    }

    @Override
    public void denyTypes(Transpiler transpiler) {
        transpiler.reject(Object.class);
        transpiler.reject(Void.class);

        transpiler.reject(Character.TYPE);
        transpiler.reject(Void.TYPE);
        transpiler.reject(Long.TYPE);
        transpiler.reject(Integer.TYPE);
        transpiler.reject(Short.TYPE);
        transpiler.reject(Byte.TYPE);
        transpiler.reject(Double.TYPE);
        transpiler.reject(Float.TYPE);
        transpiler.reject(Boolean.TYPE);
    }

    @Override
    public void registerClassTransformer(ClassTransformerRegistration registration) {
        if (ProbeConfig.autoParamRename.get()) {
            registration.register(new AutoParamRename());
        }
        if (RhizoState.RETURNS_SELF_ANNOTATION) {
            registration.register(new RhizoReturnsSelf());
        }
        registration.register(
            new InjectAnnotation(),
            new InjectArray(),
            new InjectBeans(ProbeConfig.fieldAsBeaning.get()),
            new InjectSpecialType()
        );
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        new SnippetGenerationEventJS(dump).post(ScriptType.CLIENT, ProbeEvents.SNIPPETS);
    }
}
