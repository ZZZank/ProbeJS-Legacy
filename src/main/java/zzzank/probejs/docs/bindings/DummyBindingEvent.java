package zzzank.probejs.docs.bindings;

import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;

/**
 * @author ZZZank
 */
public class DummyBindingEvent extends BindingsEvent {
    private final BindingReader reader;

    public DummyBindingEvent(ScriptManager manager, Context cx, Scriptable scope, BindingReader reader) {
        super(manager, cx, scope);
        this.reader = reader;
    }

    @Override
    public void add(String name, Object value) {
        if (value instanceof Class<?> c) {
            reader.classes.put(name, c);
        } else if (value instanceof BaseFunction fn) {
            reader.functions.put(name, fn);
        } else {
            reader.constants.put(name, value);
        }
    }
}
