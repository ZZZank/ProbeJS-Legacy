package zzzank.probejs.features.rhizo;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.RemapForJS;
import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import dev.latvian.mods.rhino.util.Remapper;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.remap.ClazzNamesRemapper;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZZZank
 */
public class RhizoClazzRemapper implements ClazzNamesRemapper {
    private final Remapper remapper;
    private final Map<Class<?>, String[]> prefixCache = new HashMap<>();

    public RhizoClazzRemapper(Context cx) {
        this.remapper = cx.getRemapper();
    }

    @Override
    public @NotNull String remapClass(@NotNull Class<?> from) {
        val remapped = remapper.getMappedClass(from);
        return remapped.isEmpty() ? from.getName() : remapped;
    }

    @Override
    public @NotNull String unmapClass(@NotNull String from) {
        val remapped = remapper.getUnmappedClass(from);
        return remapped.isEmpty() ? from : remapped;
    }

    @Override
    public @NotNull String remapField(@NotNull Class<?> from, @NotNull Field field) {
        // direct
        var remapped = getDirectRemap(field);
        if (!remapped.isEmpty()) {
            return remapped;
        }
        // prefix
        remapped = field.getName();
        for (var prefix : getPrefixRemap(from)) {
            if (remapped.startsWith(prefix) && remapped.length() > prefix.length()) {
                return remapped.substring(prefix.length()).trim();
            }
        }
        // mapping
        remapped = remapper.getMappedField(from, field);
        // fallback
        return remapped.isEmpty() ? field.getName() : remapped;
    }

    @Override
    public @NotNull String remapMethod(@NotNull Class<?> from, @NotNull Method method) {
        // direct
        var remapped = getDirectRemap(method);
        if (!remapped.isEmpty()) {
            return remapped;
        }
        // prefix
        remapped = method.getName();
        for (var prefix : getPrefixRemap(from)) {
            if (remapped.startsWith(prefix) && remapped.length() > prefix.length()) {
                return remapped.substring(prefix.length()).trim();
            }
        }
        // mapping
        remapped = remapper.getMappedMethod(from, method);
        // fallback
        return remapped.isEmpty() ? method.getName() : remapped;
    }

    private String[] getPrefixRemap(Class<?> clazz) {
        return prefixCache.computeIfAbsent(
            clazz,
            c -> Arrays.stream(c.getAnnotationsByType(RemapPrefixForJS.class))
                .map(RemapPrefixForJS::value)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new)
        );
    }

    private String getDirectRemap(AnnotatedElement annotated) {
        val anno = annotated.getAnnotation(RemapForJS.class);
        return anno == null ? "" : anno.value().trim();
    }
}
