package zzzank.probejs.lang.java.type.impl;

import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class WildType extends TypeDescriptor.MaybeConsolidatable {
    @Nullable
    public final TypeDescriptor bound;

    public WildType(AnnotatedWildcardType wildcardType) {
        super(wildcardType.getAnnotations());
        bound = TypeAdapter.getTypeDescription(extractBound((WildcardType) wildcardType.getType()));
    }

    public WildType(WildcardType wildcardType) {
        super(NO_ANNOTATION);
        bound = TypeAdapter.getTypeDescription(extractBound(wildcardType));
    }

    private WildType(@Nonnull Annotation[] annotations, @Nullable TypeDescriptor bound) {
        super(annotations);
        this.bound = bound;
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return bound != null ? Stream.of(bound) : Stream.empty();
    }

    @Override
    public Class<?> asClass() {
        return bound == null ? Object.class : bound.asClass();
    }

    @Override
    public boolean canConsolidate() {
        return bound != null && bound.canConsolidate();
    }

    @Override
    protected TypeDescriptor consolidateImpl(Map<VariableType, TypeDescriptor> mapping) {
        if (bound == null) {
            return this;
        }
        return new WildType(this.annotations, bound.consolidate(mapping));
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof WildType another && Objects.equals(bound, another.bound));
    }

    @Override
    public int hashCode() {
        return bound == null ? 0 : bound.hashCode();
    }

    private static Type extractBound(WildcardType wildcardType) {
        // upper
        var bounds = wildcardType.getUpperBounds();
        if (bounds[0] != Object.class) {
            return bounds[0];
        }

        // lower
        bounds = wildcardType.getLowerBounds();
        if (bounds.length > 0) {
            return bounds[0];
        }

        // fallback
        return null;
    }
}
