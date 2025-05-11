package zzzank.probejs.events;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.TypeSpecificFiles;

import java.util.Map;
import java.util.function.Consumer;

public class TypingModificationEventJS extends ScriptEventJS {

    private final TypeSpecificFiles files;

    public TypingModificationEventJS(TypeSpecificFiles files) {
        super(files.scriptDump());
        this.files = files;
    }

    public void modify(Class<?> clazz, Consumer<TypeScriptFile> modifier) {
        val ts = files.request(clazz);
        if (ts == null) {
            getScriptType().console.errorf("Class with path '%s' not found, skipping", ClassPath.fromJava(clazz).getName());
            return;
        }
        modifier.accept(ts);
    }

    public Map<ClassPath, TypeScriptFile> viewGlobalFiles() {
        return files.globalFiles();
    }
}
