package zzzank.probejs.lang.transpiler;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.utils.Asser;

import java.util.*;

/**
 * Converts a Clazz into a TypeScriptFile ready for dump.
 */
public abstract class Transpiler {
    public final TypeConverter typeConverter;
    public final Set<ClassPath> rejectedClasses = new HashSet<>();

    public Transpiler(TypeConverter typeConverter) {
        this.typeConverter = Asser.tNotNull(typeConverter, "typeConverter");
    }

    public void reject(Class<?> clazz) {
        rejectedClasses.add(ClassPath.ofJava(clazz));
    }

    public Map<ClassPath, TypeScriptFile> dump(Collection<Clazz> clazzes) {
        val transpiler = createClassTranspiler();
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

    protected abstract ClassTranspiler createClassTranspiler();

    public boolean isRejected(Clazz clazz) {
        return rejectedClasses.contains(clazz.classPath);
    }
}
