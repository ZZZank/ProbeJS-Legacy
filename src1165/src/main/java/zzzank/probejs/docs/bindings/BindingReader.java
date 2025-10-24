package zzzank.probejs.docs.bindings;

import dev.latvian.kubejs.util.KubeJSPlugins;
import dev.latvian.mods.rhino.BaseFunction;
import lombok.val;
import zzzank.probejs.ProbeJS;
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
        val pack = manager.packs.get(manager.directory.getFileName().toString());
        if (pack == null) {
            ProbeJS.LOGGER.error("Script context not found, unable to read binding infos");
            return;
        }
        val context = pack.context;
        var scope = pack.scope;
        val dummy = new DummyBindingEvent(manager, context, scope, this);

        KubeJSPlugins.forEachPlugin(p -> p.addBindings(dummy));
        /*
        if (KessJSState.MOD) {
            scope = scope.getParentScope();
        }

        for (val idObj : scope.getIds()) {
            if (!(idObj instanceof String id)) {
                continue;
            }
            var value = scope.get(id, scope);
            value = value instanceof NativeJavaClass nativeJavaClass
                ? nativeJavaClass.getClassObject()
                : GameUtils.jsToJava(context, value, Object.class);
            if (value instanceof Class<?> c) {
                classes.put(id, c);
            } else if (value instanceof BaseFunction fn) {
                functions.put(id, fn);
            } else {
                constants.put(id, value);
            }
        }
         */
    }
}
