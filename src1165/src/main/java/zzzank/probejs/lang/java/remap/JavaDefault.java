package zzzank.probejs.lang.java.remap;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author ZZZank
 */
public class JavaDefault implements ClazzNamesRemapper {
    @Override
    public @NotNull String remapClass(@NotNull Class<?> from) {
        return from.getName();
    }

    @Override
    public @NotNull String unmapClass(@NotNull String from) {
        return from;
    }

    @Override
    public @NotNull String remapField(@NotNull Class<?> from, @NotNull Field field) {
        return field.getName();
    }

    @Override
    public @NotNull String remapMethod(@NotNull Class<?> from, @NotNull Method method) {
        return method.getName();
    }
}
