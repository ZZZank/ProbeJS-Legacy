package zzzank.probejs.events;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.util.Map;
import java.util.function.Consumer;

public class TypingModificationEventJS extends ScriptEventJS {

    private final Map<ClassPath, TypeScriptFile> files;

    public TypingModificationEventJS(ScriptDump dump, Map<ClassPath, TypeScriptFile> files) {
        super(dump);
        this.files = files;
    }

    public void modify(Class<?> clazz, Consumer<TypeScriptFile> modifier) {
        val path = ClassPath.fromJava(clazz);
        val ts = files.get(path);
        if (ts == null) {
            getScriptType().console.errorf("Class with path '%s' not found, skipping", path);
            return;
        }
        modifier.accept(ts);
    }
}
