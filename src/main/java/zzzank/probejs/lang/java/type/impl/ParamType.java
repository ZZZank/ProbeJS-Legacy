package zzzank.probejs.lang.java.type.impl;

import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.utils.CollectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Stream;

public class ParamType extends TypeDescriptor {
    public TypeDescriptor base;
    public final List<TypeDescriptor> params;

    public ParamType(AnnotatedParameterizedType annotatedType) {
        super(annotatedType.getAnnotations());
        this.base = TypeAdapter.getTypeDescription(((ParameterizedType) annotatedType.getType()).getRawType(), false);
        this.params = CollectUtils.mapToList(
            annotatedType.getAnnotatedActualTypeArguments(),
            t -> TypeAdapter.getTypeDescription(t, false)
        );
    }

    public ParamType(ParameterizedType parameterizedType) {
        super(NO_ANNOTATION);
        this.base = TypeAdapter.getTypeDescription(parameterizedType.getRawType(), false);
        this.params = CollectUtils.mapToList(
            parameterizedType.getActualTypeArguments(),
            t -> TypeAdapter.getTypeDescription(t, false)
        );
    }

    public ParamType(Annotation[] annotations, TypeDescriptor base, List<TypeDescriptor> params) {
        super(annotations);
        this.base = base;
        this.params = params;
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return Stream.concat(base.stream(), params.stream().flatMap(TypeDescriptor::stream));
    }

    @Override
    public int hashCode() {
        return base.hashCode() * 31 + params.hashCode();
    }
}
