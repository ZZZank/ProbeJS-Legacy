package zzzank.probejs.lang.java.type;

import zzzank.probejs.lang.java.type.impl.*;

import java.lang.reflect.*;
import java.util.Collections;

public class TypeAdapter {
    public static TypeDescriptor getTypeDescription(AnnotatedType type) {
        return getTypeDescription(type, true);
    }

    public static TypeDescriptor getTypeDescriptionShallow(AnnotatedType type) {
        return getTypeDescription(type, false);
    }

    public static TypeDescriptor getTypeDescription(AnnotatedType type, boolean recursive) {
        if (type == null) {
            return null;
        } else if (type instanceof AnnotatedArrayType arrayType) {
            return new ArrayType(arrayType);
        } else if (type instanceof AnnotatedParameterizedType paramType) {
            return new ParamType(paramType);
        } else if (type instanceof AnnotatedTypeVariable typeVariable) {
            return new VariableType(typeVariable, recursive);
        } else if (type instanceof AnnotatedWildcardType wildcardType) {
            return new WildType(wildcardType);
        }

        if (type.getType() instanceof Class<?> clazz) {
            if (!recursive) {
                return new ClassType(clazz);
            }
            var typeParameters = clazz.getTypeParameters();
            if (typeParameters.length == 0) {
                return new ClassType(clazz);
            }
            return new ParamType(
                new ClassType(clazz),
                Collections.nCopies(typeParameters.length, new ClassType(Object.class))
            );
        }
        throw new RuntimeException("Unknown type to be resolved");
    }

    public static TypeDescriptor getTypeDescription(Type type) {
        return getTypeDescription(type, true);
    }

    public static TypeDescriptor getTypeDescriptionShallow(Type type) {
        return getTypeDescription(type, false);
    }

    public static TypeDescriptor getTypeDescription(Type type, boolean recursive) {
        if (type == null) {
            return null;
        } else if (type instanceof GenericArrayType arrayType) {
            return new ArrayType(arrayType);
        } else if (type instanceof ParameterizedType parameterizedType) {
            return new ParamType(parameterizedType);
        } else if (type instanceof TypeVariable<?> typeVariable) {
            return new VariableType(typeVariable, recursive);
        } else if (type instanceof WildcardType wildcardType) {
            return new WildType(wildcardType);
        } else if (type instanceof Class<?> clazz) {
            if (!recursive) {
                return new ClassType(clazz);
            }
            var typeParameters = clazz.getTypeParameters();
            if (typeParameters.length == 0) {
                return new ClassType(clazz);
            }
            return new ParamType(
                new ClassType(clazz),
                Collections.nCopies(typeParameters.length, new ClassType(Object.class))
            );
        }
        throw new RuntimeException("Unknown type to be resolved");
    }
}
