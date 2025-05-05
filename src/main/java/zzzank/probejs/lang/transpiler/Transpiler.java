package zzzank.probejs.lang.transpiler;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.plugin.ProbeJSPlugins;
import zzzank.probejs.utils.Asser;

import java.util.*;

/**
 * Converts a Clazz into a TypeScriptFile ready for dump.
 */
public class Transpiler {
    public final TypeConverter typeConverter;
    public final Set<ClassPath> rejectedClasses = new HashSet<>();

    public Transpiler() {
        this(new TypeConverter());
    }

    public Transpiler(TypeConverter typeConverter) {
        this.typeConverter = Asser.tNotNull(typeConverter, "typeConverter");
    }

    public void reject(Class<?> clazz) {
        rejectedClasses.add(ClassPath.fromJava(clazz));
    }

    public void init() {
        ProbeJSPlugins.forEachPlugin(plugin -> {
            plugin.addPredefinedTypes(typeConverter);
            plugin.denyTypes(this);
        });
    }

    public Map<ClassPath, TypeScriptFile> dump(ScriptDump scriptDump, Collection<Clazz> clazzes) {
        val transpiler = new ClassTranspiler(
            typeConverter,
            ClassTransformer.fromPlugin(scriptDump, this)
        );
        Map<ClassPath, TypeScriptFile> result = new HashMap<>();

        for (val clazz : clazzes) {
            if (isRejected(clazz)) {
                continue;
            }

            val classDecl = transpiler.transpile(clazz);
            val scriptFile = new TypeScriptFile(clazz.classPath);
            scriptFile.addCode(classDecl);

            result.put(clazz.classPath, scriptFile);
        }

        return result;
    }

    public boolean isRejected(Clazz clazz) {
        return rejectedClasses.contains(clazz.classPath) || clazz.hasAnnotation(HideFromJS.class);
    }
}
