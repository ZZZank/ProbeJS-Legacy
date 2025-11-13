package zzzank.probejs.lang.transpiler.redirect;

import com.google.common.collect.ImmutableSet;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.ClassType;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.type.BaseType;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * only redirects {@link ClassType}
 *
 * @author ZZZank
 */
public final class SimpleClassRedirect implements TypeRedirect {

    private final Set<Class<?>> targets;
    private final Function<ClassType, BaseType> mapper;

    public SimpleClassRedirect(Class<?> target, Function<ClassType, BaseType> mapper) {
        this.targets = ImmutableSet.of(target);
        this.mapper = Objects.requireNonNull(mapper);
    }

    public SimpleClassRedirect(Collection<Class<?>> targets, Function<ClassType, BaseType> mapper) {
        this.targets = ImmutableSet.copyOf(targets);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public BaseType apply(TypeDescriptor typeDesc, TypeConverter converter) {
        return mapper.apply((ClassType) typeDesc);
    }

    @Override
    public boolean test(TypeDescriptor typeDescriptor, TypeConverter converter) {
        return typeDescriptor instanceof ClassType classType && targets.contains(classType.clazz);
    }
}
