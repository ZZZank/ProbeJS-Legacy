package zzzank.probejs.utils.config.binding;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * @author ZZZank
 */
public class FieldBinding<T> extends BindingBase<T> {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

    private final Object instance;
    private final MethodHandle getter;
    private final MethodHandle setter;

    public FieldBinding(@NotNull Field field, Object instance, @NotNull String name) throws IllegalAccessException {
        this(LOOKUP, field, instance, name);
    }

    public FieldBinding(MethodHandles.Lookup lookup, @NotNull Field field, Object instance, @NotNull String name)
        throws IllegalAccessException {
        // implicit field accessibility check via `field.get(instance)`
        super((T) field.get(instance), (Class<T>) field.getType(), name);
        this.instance = instance;
        this.getter = lookup.unreflectGetter(field);
        this.setter = lookup.unreflectSetter(field);
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
            return instance == null ? (T) getter.invoke() : (T) getter.invoke(instance);
        } catch (Throwable e) {
            return defaultValue;
        }
    }
}
