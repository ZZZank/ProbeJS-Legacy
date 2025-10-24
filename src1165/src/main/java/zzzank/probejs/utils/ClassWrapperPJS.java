package zzzank.probejs.utils;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaClass;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.util.CustomJavaObjectWrapper;

import java.util.Objects;

/**
 * @author ZZZank
 */
public class ClassWrapperPJS<T> implements CustomJavaObjectWrapper {
    private final Class<T> clazz;

    public ClassWrapperPJS(Class<T> type) {
        this.clazz = Objects.requireNonNull(type);
    }

    @Override
    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Class<?> staticType) {
        return new NativeJavaClass(cx, scope, this.clazz);
    }
}
