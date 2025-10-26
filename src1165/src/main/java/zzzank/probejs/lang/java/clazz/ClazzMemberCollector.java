package zzzank.probejs.lang.java.clazz;

import dev.latvian.mods.rhino.util.HideFromJS;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class ClazzMemberCollector extends BasicMemberCollector {

    @Override
    protected Stream<Constructor<?>> rawConstructors() {
        return super.rawConstructors().filter(NO_HIDE_FROM_JS);
    }

    @Override
    public Stream<MethodInfo> methods() {
        return super.methods().sorted(Comparator.comparing(info -> info.name));
    }

    @Override
    protected @NotNull Stream<Method> rawMethods() {
        return super.rawMethods().filter(NO_HIDE_FROM_JS);
    }

    @Override
    public Stream<FieldInfo> fields() {
        // those not declared by it will be inherited from super
        return super.fields().sorted(Comparator.comparing(f -> f.name));
    }

    @Override
    protected @NotNull Stream<Field> rawFields() {
        return super.rawFields().filter(NO_HIDE_FROM_JS);
    }

    public static final Predicate<AnnotatedElement> NO_HIDE_FROM_JS =
        element -> !element.isAnnotationPresent(HideFromJS.class);
}
