package zzzank.probejs.lang.java.type.impl;

import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class VariableType extends TypeDescriptor {
    public String symbol;
    public List<TypeDescriptor> descriptors;

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
        this.symbol = typeVariable.getName();
        this.descriptors = checkBounds
            ? Arrays.stream(typeVariable.getAnnotatedBounds())
                .filter(bound -> Object.class != bound.getType())
                .map(TypeAdapter::getTypeDescription)
                .toList()
            : List.of();
    }

    @Override
    public Class<?> asClass() {
        if (this.descriptors.isEmpty()) {
            return Object.class;
        }
        return descriptors.get(0).asClass();
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return descriptors.stream().flatMap(TypeDescriptor::stream);
    }

    public String getSymbol() {
        return symbol;
    }

    public List<TypeDescriptor> getDescriptors() {
        return descriptors;
    }
}
