package zzzank.probejs.lang.java.type.impl;

import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.utils.CollectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ParamType extends TypeDescriptor.MaybeConsolidatable {
    public final TypeDescriptor base;
    public final List<TypeDescriptor> params;

    public ParamType(AnnotatedParameterizedType annotatedType) {
        this(
            annotatedType.getAnnotations(),
            TypeAdapter.getTypeDescription(((ParameterizedType) annotatedType.getType()).getRawType(), false),
            CollectUtils.mapToList(
                annotatedType.getAnnotatedActualTypeArguments(),
                t -> TypeAdapter.getTypeDescription(t, false)
            )
        );
    }

    public ParamType(ParameterizedType parameterizedType) {
        this(
            NO_ANNOTATION,
            TypeAdapter.getTypeDescription(parameterizedType.getRawType(), false),
            CollectUtils.mapToList(
                parameterizedType.getActualTypeArguments(),
                t -> TypeAdapter.getTypeDescription(t, false)
            )
        );
    }

    public ParamType(Annotation[] annotations, TypeDescriptor base, List<TypeDescriptor> params) {
        super(annotations);
        this.base = base;
        this.params = params;
    }

    public ParamType(TypeDescriptor base, List<TypeDescriptor> params) {
        this(base.annotations, base, params);
    }

    @Override
    public boolean canConsolidate() {
        return this.params.stream().anyMatch(TypeDescriptor::canConsolidate);
    }

    @Override
    protected TypeDescriptor consolidateImpl(Map<VariableType, TypeDescriptor> mapping) {
        return new ParamType(this.base, CollectUtils.mapToList(this.params, t -> t.consolidate(mapping)));
    }

    @Override
    public Class<?> asClass() {
        return base.asClass();
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return Stream.concat(base.stream(), params.stream().flatMap(TypeDescriptor::stream));
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ParamType another && base.equals(another) && params.equals(another.params));
    }

    @Override
    public int hashCode() {
        return base.hashCode() * 31 + params.hashCode();
    }
}
