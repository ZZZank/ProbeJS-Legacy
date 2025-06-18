package zzzank.probejs.lang.java.type.impl;

import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class ArrayType extends TypeDescriptor.MaybeConsolidatable {
    public final TypeDescriptor component;
    private Class<?> asClass;

    public ArrayType(AnnotatedArrayType arrayType) {
        this(arrayType.getAnnotations(), TypeAdapter.getTypeDescription(arrayType.getAnnotatedGenericComponentType()));
    }

    public ArrayType(GenericArrayType arrayType) {
        this(NO_ANNOTATION, TypeAdapter.getTypeDescription(arrayType.getGenericComponentType()));
    }

    public ArrayType(Annotation[] annotations, TypeDescriptor component) {
        super(annotations);
        this.component = Objects.requireNonNull(component);
    }

    @Override
    public Class<?> asClass() {
        if (asClass == null) {
            asClass = Array.newInstance(component.asClass(), 0).getClass();
        }
        return asClass;
    }

    @Override
    public boolean canConsolidate() {
        return component.canConsolidate();
    }

    @Override
    protected TypeDescriptor consolidateImpl(Map<VariableType, TypeDescriptor> mapping) {
        return new ArrayType(this.annotations, component.consolidate(mapping));
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return component.stream();
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        return component.getClassPaths();
    }

    @Override
    public Collection<Class<?>> getClasses() {
        return component.getClasses();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ArrayType another && component.equals(another.component));
    }

    @Override
    public int hashCode() {
        return component.hashCode();
    }
}
