package zzzank.probejs.lang.typescript;

import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.code.Code;

import java.util.*;

/**
 * @author ZZZank
 */
public class RequestAwareFiles {
    private final Map<ClassPath, TypeScriptFile> globalFiles;
    private final ScriptDump scriptDump;
    private final Set<ClassPath> requested = new HashSet<>();

    public RequestAwareFiles(Map<ClassPath, TypeScriptFile> globalFiles, ScriptDump scriptDump) {
        this.globalFiles = globalFiles;
        this.scriptDump = scriptDump;
    }

    public TypeScriptFile request(ClassPath path) {
        markRequested(path);
        return globalFiles.get(path);
    }

    public TypeScriptFile request(Class<?> clazz) {
        return request(ClassPath.ofJava(clazz));
    }

    public <T extends Code> T requestCode(Class<?> clazz, Class<T> codeType) {
        val got = request(clazz);
        if (got == null) {
            return null;
        }
        return got.findCodeNullable(codeType);
    }

    public void unRequest(ClassPath path) {
        requested.remove(path);
    }

    public void markRequested(ClassPath path) {
        requested.add(path);
    }

    public TypeScriptFile requestOrCreate(ClassPath path) {
        markRequested(path);
        return globalFiles.computeIfAbsent(path, TypeScriptFile::new);
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
