package zzzank.probejs.lang.typescript;

import dev.latvian.kubejs.script.ScriptType;
import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.utils.Asser;

import java.util.*;
import java.util.function.BiFunction;

/**
 * @author ZZZank
 */
public class TypeSpecificFiles {
    private final Map<ClassPath, TypeScriptFile> globalFiles;
    private final ScriptDump scriptDump;
    private final Set<ClassPath> requested = new HashSet<>();
    private final Map<ClassPath, TypeScriptFile> dumpSpecific = new HashMap<>();

    public TypeSpecificFiles(Map<ClassPath, TypeScriptFile> globalFiles, ScriptDump scriptDump) {
        this.globalFiles = globalFiles;
        this.scriptDump = scriptDump;
    }

    public TypeScriptFile request(ClassPath path) {
        markRequested(path);
        return globalFiles.get(path);
    }

    public TypeScriptFile request(Class<?> clazz) {
        return request(ClassPath.fromJava(clazz));
    }

    public <T extends Code> T requestCode(Class<?> clazz, Class<T> codeType) {
        val got = request(clazz);
        if (got == null) {
            return null;
        }
        return got.findCodeNullable(codeType);
    }

    public void deRequest(ClassPath path) {
        requested.remove(path);
    }

    public void markRequested(ClassPath path) {
        requested.add(path);
    }

    /// @see Map#compute(Object, BiFunction)
    public TypeScriptFile addDumpSpecificFile(TypeScriptFile file) {
        Asser.tNotNull(file.path, "class path of file");
        return dumpSpecific.compute(file.path, (path, f) -> {
            if (f == null) {
                return file;
            }
            f.codes.addAll(file.codes);
            return f;
        });
    }

    /// @see #addDumpSpecificFile(TypeScriptFile)
    public TypeScriptFile addDumpSpecificFile(ClassPath path, Collection<? extends Code> codes) {
        val file = new TypeScriptFile(path);
        file.codes.addAll(codes);
        return addDumpSpecificFile(file);
    }

    /// @see #addDumpSpecificFile(TypeScriptFile)
    public TypeScriptFile addDumpSpecificFile(ClassPath path, Code... codes) {
        return addDumpSpecificFile(path, Arrays.asList(codes));
    }

    public Map<ClassPath, TypeScriptFile> dumpSpecificFiles() {
        return dumpSpecific;
    }

    public Map<ClassPath, TypeScriptFile> globalFiles() {
        return Collections.unmodifiableMap(globalFiles);
    }

    public ScriptDump scriptDump() {
        return scriptDump;
    }

    public ScriptType scriptType() {
        return scriptDump.scriptType;
    }

    public Set<ClassPath> requested() {
        return Collections.unmodifiableSet(requested);
    }
}
