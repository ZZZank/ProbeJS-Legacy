package zzzank.probejs.docs.bindings;

import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaClass;
import lombok.val;
import zzzank.probejs.lang.typescript.ScriptDump;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZZZank
 */
public final class BindingReader {

    public final Map<String, Object> constants = new HashMap<>();
    public final Map<String, Class<?>> classes = new HashMap<>();
    public final Map<String, BaseFunction> functions = new HashMap<>();

    public final ScriptDump scriptDump;

    public BindingReader(ScriptDump scriptDump) {
        this.scriptDump = scriptDump;
    }

    public void read() {
        val manager = scriptDump.manager;
        val cx = manager.context;
        val scope = manager.topLevelScope;

        for (val idObj : scope.getIds(cx)) {
            if (!(idObj instanceof String id)) {
                continue;
            }

            var value = scope.get(cx, id, scope);
            value = value instanceof NativeJavaClass nativeJavaClass
                ? nativeJavaClass.getClassObject()
                : Context.jsToJava(cx, value, Object.class);

            if (value instanceof Class<?> c) {
                classes.put(id, c);
            } else if (value instanceof BaseFunction fn) {
                functions.put(id, fn);
            } else {
                constants.put(id, value);
            }
        }
    }
}
