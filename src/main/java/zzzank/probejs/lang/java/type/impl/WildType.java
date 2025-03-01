package zzzank.probejs.lang.java.type.impl;

import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.stream.Stream;

public class WildType extends TypeDescriptor {
    @Nullable
    public TypeDescriptor bound;

    public WildType(AnnotatedWildcardType wildcardType) {
        super(wildcardType.getAnnotations());
        if (wildcardType.getAnnotatedLowerBounds().length != 0) {
            bound = TypeAdapter.getTypeDescription(wildcardType.getAnnotatedLowerBounds()[0]);
        } else if (!wildcardType.getAnnotatedUpperBounds()[0].getType().equals(Object.class)) {
            bound = TypeAdapter.getTypeDescription(wildcardType.getAnnotatedUpperBounds()[0]);
        } else {
            bound = null;
        }
    }

    public WildType(java.lang.reflect.WildcardType wildcardType) {
        super(NO_ANNOTATION);
        if (wildcardType.getLowerBounds().length != 0) {
            bound = TypeAdapter.getTypeDescription(wildcardType.getLowerBounds()[0]);
        } else if (!wildcardType.getUpperBounds()[0].equals(Object.class)) {
            bound = TypeAdapter.getTypeDescription(wildcardType.getUpperBounds()[0]);
        } else {
            bound = null;
        }
    }

    public WildType(@Nonnull Annotation[] annotations, @Nullable TypeDescriptor bound) {
        super(annotations);
        this.bound = bound;
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return bound != null ? Stream.of(bound) : Stream.empty();
    }
}
