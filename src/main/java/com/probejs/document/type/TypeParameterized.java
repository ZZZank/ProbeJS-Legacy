package com.probejs.document.type;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TypeParameterized implements IType {

    private final IType rawType;
    private final List<IType> paramTypes;

    public TypeParameterized(IType rawType, List<IType> paramTypes) {
        this.rawType = rawType;
        this.paramTypes = paramTypes;
    }

    public IType getRawType() {
        return rawType;
    }

    public List<IType> getParamTypes() {
        return paramTypes;
    }

    @Override
    public String getTypeName() {
        return String.format(
            "%s<%s>",
            rawType.getTypeName(),
            paramTypes.stream().map(IType::getTypeName).collect(Collectors.joining(", "))
        );
    }

    @Override
    public String transform(BiFunction<IType, String, String> transformer) {
        return transformer.apply(this, String.format("%s<%s>",rawType.transform(transformer), paramTypes.stream().map(t -> t.transform(transformer)).collect(Collectors.joining(", "))));
    }
}
