package zzzank.probejs.lang.java.remap;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author ZZZank
 */
public interface ClazzNamesRemapper {
    /**
     * @return full class name, for example `java.lang.String`
     */
    @NotNull
    String remapClass(@NotNull Class<?> from);

    @NotNull
    String unmapClass(@NotNull String from);

    @NotNull
    String remapField(@NotNull Class<?> from, @NotNull Field field);

    @NotNull
    String remapMethod(@NotNull Class<?> from, @NotNull Method method);
}
