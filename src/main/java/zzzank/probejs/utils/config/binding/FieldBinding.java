package zzzank.probejs.utils.config.binding;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * @author ZZZank
 */
public class FieldBinding<T> extends BindingBase<T> {

    private final Object instance;
    private final MethodHandle getter;
    private final MethodHandle setter;

    public FieldBinding(@NotNull Field field, Object instance, @NotNull String name) throws IllegalAccessException {
        super((T) field.get(instance), (Class<T>) field.getType(), name);
        this.instance = instance;
        val lookup = MethodHandles.publicLookup();
        getter = lookup.unreflectGetter(field);
        setter = lookup.unreflectSetter(field);
    }

    public FieldBinding(Field field, Object instance) throws IllegalAccessException {
        this(field, instance, field.getName());
    }

    @Override
    protected void setImpl(T value) {
        try {
            if (instance == null) {
                setter.invoke(value);
            } else {
                setter.invoke(instance, value);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull T get() {
        try {
            val got = instance == null ? getter.invoke() : getter.invoke(instance);
            return (T) got;
        } catch (Throwable e) {
            return defaultValue;
        }
    }
}
