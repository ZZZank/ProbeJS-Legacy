package zzzank.probejs.events;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.DumpSpecificFiles;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.util.Map;
import java.util.function.Consumer;

public class TypingModificationEventJS extends ScriptEventJS {

    private final DumpSpecificFiles files;

    public TypingModificationEventJS(DumpSpecificFiles files) {
        super(files.scriptDump());
        this.files = files;
    }

    public void modify(Class<?> clazz, Consumer<TypeScriptFile> modifier) {
        val path = ClassPath.fromJava(clazz);
        val ts = files.request(path);
        if (ts == null) {
            getScriptType().console.errorf("Class with path '%s' not found, skipping", path);
            return;
        }
        modifier.accept(ts);
    }

    public Map<ClassPath, TypeScriptFile> viewGlobalFiles() {
        return files.globalFiles();
    }
}
