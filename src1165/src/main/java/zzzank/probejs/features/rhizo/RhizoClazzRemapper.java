package zzzank.probejs.features.rhizo;

import dev.latvian.mods.rhino.util.remapper.Remapper;
import dev.latvian.mods.rhino.util.remapper.RemapperManager;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.remap.ClazzNamesRemapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author ZZZank
 */
public class RhizoClazzRemapper implements ClazzNamesRemapper {
    private final Remapper remapper;

    public RhizoClazzRemapper() {
        this.remapper = RemapperManager.getDefault();
    }

    @Override
    public @NotNull String remapClass(@NotNull Class<?> from) {
        val remapped = remapper.remapClass(from);
        return remapped.isEmpty() ? from.getName() : remapped;
    }

    @Override
    public @NotNull String unmapClass(@NotNull String from) {
        val remapped = remapper.unmapClass(from);
        return remapped.isEmpty() ? from : remapped;
    }

    @Override
    public @NotNull String remapField(@NotNull Class<?> from, @NotNull Field field) {
        val remapped = remapper.remapField(from, field);
        return remapped.isEmpty() ? field.getName() : remapped;
    }

    @Override
    public @NotNull String remapMethod(@NotNull Class<?> from, @NotNull Method method) {
        val remapped = remapper.remapMethod(from, method);
        return remapped.isEmpty() ? method.getName() : remapped;
    }
}
