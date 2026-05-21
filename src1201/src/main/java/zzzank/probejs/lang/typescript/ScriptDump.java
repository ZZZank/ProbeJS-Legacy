package zzzank.probejs.lang.typescript;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.val;
import net.minecraftforge.api.distmarker.OnlyIn;
import zzzank.probejs.ProbePaths;
import zzzank.probejs.api.dump.*;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Controls a dump. A dump is made of a script type, and is responsible for
 * maintaining the file structures
 */
public class ScriptDump extends MultiDump implements ProbeNamedDump {
    public static final Function<SharedDump, ScriptDump> SERVER_DUMP = (codeDump) -> new ScriptDump(
        codeDump,
        ScriptType.SERVER,
        KubeJSPaths.SERVER_SCRIPTS,
        clazz -> {
            val annotation = clazz.getAnnotation(OnlyIn.class);
            return annotation == null || annotation.value().isDedicatedServer();
        }
    );

    public static final Function<SharedDump, ScriptDump> CLIENT_DUMP = (codeDump) -> new ScriptDump(
        codeDump,
        ScriptType.CLIENT,
        KubeJSPaths.CLIENT_SCRIPTS,
        clazz -> {
            val annotation = clazz.getAnnotation(OnlyIn.class);
            return annotation == null || annotation.value().isClient();
        }
    );

    public static final Function<SharedDump, ScriptDump> STARTUP_DUMP = (codeDump) -> new ScriptDump(
        codeDump,
        ScriptType.STARTUP,
        KubeJSPaths.STARTUP_SCRIPTS,
        (clazz -> true)
    );

    public final SharedDump parent;
    public final Transpiler transpiler;

    public final ScriptType scriptType;
    public final ScriptManager manager;
    public final Path scriptPath;

    public final Set<Clazz> recordedClasses = new HashSet<>();
    private final Predicate<Clazz> accept;
    public final Multimap<ClassPath, TypeDecl> convertibles = ArrayListMultimap.create();

    public final TSFilesDump filesDump;
    public final TSGlobalDump globalDump;
    public final JSConfigDump jsConfigDump;

    public ScriptDump(SharedDump parent, ScriptType type, Path scriptPath, Predicate<Clazz> classFilter) {
        super(ProbePaths.PROBE.resolve(type.name));
        this.parent = parent;
        this.transpiler = parent.transpiler;

        this.scriptType = type;
        this.manager = type.manager.get();
        this.scriptPath = scriptPath;

        this.accept = classFilter;
        this.filesDump = new TSFilesDump(writeTo().resolve("packages"));
        this.globalDump = new TSGlobalDump(writeTo().resolve("global"));
        this.jsConfigDump = new JSConfigDump(scriptPath.resolve("jsconfig.json"), scriptPath);
    }

    public Set<Class<?>> retrieveClasses() {
        Set<Class<?>> classes = new HashSet<>();
        ProbeJSPlugins.forEachPlugin(plugin -> classes.addAll(plugin.provideJavaClass(this)));
        return classes;
    }

    public void assignType(Class<?> classPath, BaseType type) {
        assignType(ClassPath.ofJava(classPath), null, type);
    }

    public void assignType(ClassPath classPath, BaseType type) {
        assignType(classPath, null, type);
    }

    public void assignType(Class<?> classPath, String name, BaseType type) {
        assignType(ClassPath.ofJava(classPath), name, type);
    }

    public void assignType(ClassPath classPath, String name, BaseType type) {
        convertibles.put(classPath, new TypeDecl(name, type));
    }

    public void addGlobal(String identifier, Code... content) {
        addGlobal(identifier, Collections.emptyList(), content);
    }

    public void addGlobal(String identifier, Collection<String> excludedNames, Code... content) {
        val file = globalDump.globals.computeIfAbsent(
            identifier,
            (k) -> new TypeScriptFile(ClassPath.ofArtificial(k))
        );

        for (val excluded : excludedNames) {
            file.excludeSymbol(excluded);
        }

        val global = new Wrapped.Global();
        for (val code : content) {
            global.addCode(code);
        }
        file.addCode(global);
    }

    private RequestAwareFiles loadClasses() {
        val globalClasses = transpiler.dump(recordedClasses);

        val filesToModify = new RequestAwareFiles(globalClasses, this);
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.modifyFiles(filesToModify));
        val requested = filesToModify.requested();
        this.parent.denied.addAll(requested);

        for (val entry : globalClasses.entrySet()) {
            val classPath = entry.getKey();
            var output = entry.getValue();

            val classDecl = output.findCodeNullable(ClassDecl.class);
            if (classDecl == null) {
                continue;
            }

            if (!requested.contains(classPath)) {
                entry.setValue(new TypeScriptFile(output.path));
            }
        }

        return filesToModify;
    }

    @Override
    public String pjsDumpName() {
        return this.manager.scriptType.toString();
    }

    @Override
    public void open() throws IOException {
        // prepare data for this dump
        this.recordedClasses.addAll(Collections2.filter(parent.classRegistry.getFoundClasses(), accept::test));

        ProbeJSPlugins.forEachPlugin(plugin -> plugin.addChildDump(this));

        // prepare data for child dumps
        filesDump.files = loadClasses().globalFiles().values();
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.addGlobals(this));

        super.open();
    }
}
