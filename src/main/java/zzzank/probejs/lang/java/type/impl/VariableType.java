package zzzank.probejs.lang.java.type.impl;

import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class VariableType extends TypeDescriptor {
    private final TypeVariable<?> raw;
    private final List<TypeDescriptor> bounds;

    public VariableType(AnnotatedTypeVariable typeVariable) {
        this(typeVariable, true);
    }

    public VariableType(TypeVariable<?> typeVariable) {
        this(typeVariable, true);
    }

    public VariableType(AnnotatedTypeVariable typeVariable, boolean checkBounds) {
        this((TypeVariable<?>) typeVariable.getType(), checkBounds);
    }

    public VariableType(TypeVariable<?> typeVariable, boolean checkBounds) {
        super(typeVariable.getAnnotations());
        this.raw = typeVariable;
        this.bounds = checkBounds
            ? Arrays.stream(typeVariable.getAnnotatedBounds())
                .filter(bound -> Object.class != bound.getType())
                .map(TypeAdapter::getTypeDescription)
                .toList()
            : List.of();
    }

    @Override
    public Class<?> asClass() {
        if (this.bounds.isEmpty()) {
            return Object.class;
        }
        return bounds.get(0).asClass();
    }

    @Override
    public boolean canConsolidate() {
        return true;
    }

    @Override
    public TypeDescriptor consolidate(Map<VariableType, TypeDescriptor> mapping) {
        return mapping.getOrDefault(this, this);
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return bounds.stream().flatMap(TypeDescriptor::stream);
    }

    public TypeVariable<?> raw() {
        return raw;
    }

    public String getSymbol() {
        return raw.getName();
    }

    public List<TypeDescriptor> getDescriptors() {
        return bounds;
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof VariableType another && raw.equals(another.raw));
    }
}
