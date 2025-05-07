package zzzank.probejs.lang.typescript;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.KubeJSPaths;
import dev.latvian.kubejs.script.ScriptManager;
import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.server.ServerScriptManager;
import dev.latvian.kubejs.util.UtilsJS;
import lombok.val;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import zzzank.probejs.ProbePaths;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.dump.*;
import zzzank.probejs.lang.typescript.refer.ImportType;
import zzzank.probejs.plugin.ProbeJSPlugins;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Controls a dump. A dump is made of a script type, and is responsible for
 * maintaining the file structures
 */
public class ScriptDump {
    public static final Function<SharedDump, ScriptDump> SERVER_DUMP = (codeDump) -> {
        ServerScriptManager scriptManager = ServerScriptManager.instance;
        if (scriptManager == null) {
            return null;
        }

        return new ScriptDump(
            codeDump,
            scriptManager.scriptManager,
            ProbePaths.PROBE.resolve("server"),
            KubeJSPaths.SERVER_SCRIPTS,
            clazz -> clazz.getAnnotations(OnlyIn.class)
                .stream()
                .map(OnlyIn::value)
                .noneMatch(Dist::isClient)
        );
    };

    public static final Function<SharedDump, ScriptDump> CLIENT_DUMP = (codeDump) -> new ScriptDump(
        codeDump,
        KubeJS.clientScriptManager,
        ProbePaths.PROBE.resolve("client"),
        KubeJSPaths.CLIENT_SCRIPTS,
        clazz -> clazz.getAnnotations(OnlyIn.class)
            .stream()
            .map(OnlyIn::value)
            .noneMatch(Dist::isDedicatedServer)
    );

    public static final Function<SharedDump, ScriptDump> STARTUP_DUMP = (codeDump) -> new ScriptDump(
        codeDump,
        KubeJS.startupScriptManager,
        ProbePaths.PROBE.resolve("startup"),
        KubeJSPaths.STARTUP_SCRIPTS,
        (clazz -> true)
    );

    public final SharedDump parent;

    public final ScriptType scriptType;
    public final ScriptManager manager;

    public final Path basePath;
    public final Path scriptPath;

    public final Set<Clazz> recordedClasses = new HashSet<>();
    private final Predicate<Clazz> accept;
    private final Multimap<ClassPath, TypeDecl> convertibles = ArrayListMultimap.create();

    public final TSFilesDump filesDump;
    public final TSGlobalDump globalDump;

    public ScriptDump(SharedDump parent, ScriptManager manager, Path basePath, Path scriptPath, Predicate<Clazz> scriptPredicate) {
        this.parent = parent;
        this.scriptType = manager.type;
        this.manager = manager;
        this.basePath = basePath;
        this.scriptPath = scriptPath;
        this.accept = scriptPredicate;
        filesDump = new TSFilesDump(getPackageFolder());
        globalDump = new TSGlobalDump(getGlobalFolder());
    }

    public void acceptClasses(Collection<Clazz> classes) {
        for (Clazz clazz : classes) {
            if (accept.test(clazz)) {
                recordedClasses.add(clazz);
            }
        }
    }

    public Set<Class<?>> retrieveClasses() {
        Set<Class<?>> classes = CollectUtils.identityHashSet();
        ProbeJSPlugins.forEachPlugin(plugin -> classes.addAll(plugin.provideJavaClass(this)));
        return classes;
    }

    public void assignType(Class<?> classPath, BaseType type) {
        assignType(ClassPath.fromJava(classPath), type);
    }

    public void assignType(ClassPath classPath, BaseType type) {
        convertibles.put(classPath, new TypeDecl(null, type));
    }

    public void assignType(Class<?> classPath, String name, BaseType type) {
        assignType(ClassPath.fromJava(classPath), name, type);
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
            (k) -> new TypeScriptFile(ClassPath.fromRaw(k))
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

    public Path ensurePath(String path) {
        return ensurePath(path, false);
    }

    public Path ensurePath(String path, boolean script) {
        Path full = (script ? scriptPath : basePath).resolve(path);
        if (Files.notExists(full)) {
            UtilsJS.tryIO(() -> Files.createDirectories(full));
        }
        return full;
    }

    public Path getTypeFolder() {
        return ensurePath("probe-types");
    }

    public Path getPackageFolder() {
        return ensurePath("probe-types/packages");
    }

    public Path getGlobalFolder() {
        return ensurePath("probe-types/global");
    }

    public void dumpClasses() throws IOException {
        val globalClasses = transpiler().dump(recordedClasses);

        val filesToModify = new TypeSpecificFiles(globalClasses, this);
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.modifyFiles(filesToModify));
        val requested = filesToModify.requested();
        this.parent.denied.addAll(requested);

        for (val entry : globalClasses.entrySet()) {
            val classPath = entry.getKey();
            val output = entry.getValue();
            val classDecl = output.findCodeNullable(ClassDecl.class);
            if (classDecl == null) {
                continue;
            }

            if (!requested.contains(classPath)) {
                output.codes.clear();
            }

            // Add all assignable types
            // type ExportedType = ConvertibleTypes
            // declare global {
            //     type Type_ = ExportedType
            // }
            val globalSymbol = classPath.getName() + "_";
            val exportedSymbol = ImportType.TYPE.fmt(classPath.getName());

            val exportedType = Types.type(classPath)
                .withPossibleParams(classDecl.variableTypes)
                .contextShield(BaseType.FormatType.INPUT);
            val thisType = Types.type(classPath)
                .withPossibleParams(classDecl.variableTypes)
                .contextShield(BaseType.FormatType.RETURN);

            val allTypes = new ArrayList<BaseType>();
            allTypes.add(thisType);
            for (val typeDecl : convertibles.get(classPath)) {
                if (typeDecl.name != null) {
                    output.addCode(typeDecl);
                    allTypes.add(Types.primitive(typeDecl.name));
                } else {
                    allTypes.add(typeDecl.type);
                }
            }

            val typeConvertible = new TypeDecl(exportedSymbol, classDecl.variableTypes, Types.or(allTypes));
            typeConvertible.addComment("""
                Class-specific type exported by ProbeJS, use global `{Type}_` types for convenience unless there's a naming conflict.""");
            val typeGlobal = new Wrapped.Global();
            typeGlobal.addCode(new TypeDecl(globalSymbol, classDecl.variableTypes, exportedType));
            typeGlobal.addComment("""
                Global type exported for convenience, use class-specific types if there's a naming conflict.""");
            output.addCode(typeConvertible);
            output.addCode(typeGlobal);
        }

        filesDump.files = globalClasses.values();
        filesDump.dump();
    }

    public void dumpGlobal() throws IOException {
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.addGlobals(this));
        globalDump.dump();
    }

    public void dumpJSConfig() throws IOException {
        val config = (JsonObject) JsonUtils.parseObject(
            CollectUtils.ofMap(
                "compilerOptions", CollectUtils.ofMap(
                    "module", "commonjs",
                    "target", "ES2015",
                    "lib", CollectUtils.ofList("ES5", "ES2015"),
                    "rootDir", ".",
                    "typeRoots", CollectUtils.ofList(
                        String.format("../../.probe/%s/probe-types", basePath.getFileName())
                    ),
                    "baseUrl", String.format("../../.probe/%s/probe-types", basePath.getFileName()),
                    "skipLibCheck", true
                ),
                "include", CollectUtils.ofList("./**/*.js")
            )
        );
        zzzank.probejs.utils.FileUtils.writeMergedConfig(scriptPath.resolve("jsconfig.json"), config);
    }

    public void removeClasses() throws IOException {
        FileUtils.deleteDirectory(getTypeFolder().toFile());
    }

    public void dump() throws IOException, ClassNotFoundException {
        transpiler().init();
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.assignType(this));

        dumpClasses();
        dumpGlobal();
        dumpJSConfig();
    }

    public Transpiler transpiler() {
        return parent.transpiler;
    }
}
