package zzzank.probejs.lang.java.type.impl;

import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.util.Collection;
import java.util.stream.Stream;

public class ArrayType extends TypeDescriptor {
    public TypeDescriptor component;
    private Class<?> asClass;

    public ArrayType(AnnotatedArrayType arrayType) {
        super(arrayType.getAnnotations());
        this.component = TypeAdapter.getTypeDescription(arrayType.getAnnotatedGenericComponentType());
    }

    public ArrayType(GenericArrayType arrayType) {
        super(NO_ANNOTATION);
        this.component = TypeAdapter.getTypeDescription(arrayType.getGenericComponentType());
    }

    public ArrayType(Annotation[] annotations, TypeDescriptor arrayType) {
        super(annotations);
        this.component = arrayType;
    }

    @Override
    public Class<?> asClass() {
        if (asClass == null) {
            asClass = Array.newInstance(component.asClass(), 0).getClass();
        }
        return asClass;
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
    public int hashCode() {
        return component.hashCode() * 31;
    }
}
