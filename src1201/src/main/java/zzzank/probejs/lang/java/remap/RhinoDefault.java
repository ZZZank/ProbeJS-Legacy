package zzzank.probejs.lang.java.remap;

import dev.latvian.mods.rhino.util.RemapForJS;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author ZZZank
 */
public class RhinoDefault extends JavaDefault {

    @Override
    public @NotNull String remapField(@NotNull Class<?> from, @NotNull Field field) {
        if (field.isAnnotationPresent(RemapForJS.class)) {
            return field.getAnnotation(RemapForJS.class).value();
        }
        return super.remapField(from, field);
    }

    @Override
    public @NotNull String remapMethod(@NotNull Class<?> from, @NotNull Method method) {
        if (method.isAnnotationPresent(RemapForJS.class)) {
            return method.getAnnotation(RemapForJS.class).value();
        }
        return super.remapMethod(from, method);
    }
}
