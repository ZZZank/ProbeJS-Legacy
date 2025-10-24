package zzzank.probejs.lang.transpiler.redirect;

import com.google.common.collect.ImmutableSet;
import lombok.val;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.ClassType;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.utils.CollectUtils;

import java.util.*;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public class InheritableClassRedirect implements TypeRedirect {

    private final Set<Class<?>> targets;
    private final Function<Class<?>, BaseType> mapper;

    private Map.Entry<TypeDescriptor, Class<?>> MATCH_CACHE = null;

    public InheritableClassRedirect(Class<?> target, Function<Class<?>, BaseType> mapper) {
        targets = Collections.singleton(target);
        this.mapper = Objects.requireNonNull(mapper);
    }

    public InheritableClassRedirect(Collection<Class<?>> targets, Function<Class<?>, BaseType> mapper) {
        this.targets = ImmutableSet.copyOf(targets);
        this.mapper = mapper;
    }

    @Override
    public BaseType apply(TypeDescriptor typeDesc, TypeConverter converter) {
        val cache = MATCH_CACHE; // only get field once for safe concurrency
        if (cache != null && cache.getKey() == typeDesc) {
            return mapper.apply(cache.getValue());
        }
        if (typeDesc instanceof ClassType classType) {
            Class<?> c = classType.clazz;
            while (c != null) {
                if (targets.contains(c)) {
                    return mapper.apply(c);
                }
                c = c.getSuperclass();
            }
        }
        throw new IllegalStateException("no matched class");
    }

    @Override
    public boolean test(TypeDescriptor typeDescriptor, TypeConverter converter) {
        if (typeDescriptor instanceof ClassType classType) {
            Class<?> c = classType.clazz;
            while (c != null) {
                if (targets.contains(c)) {
                    MATCH_CACHE = CollectUtils.ofEntry(typeDescriptor, c);
                    return true;
                }
                c = c.getSuperclass();
            }
        }
        return false;
    }
}