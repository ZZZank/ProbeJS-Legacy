package com.probejs.info.type;

import java.lang.reflect.Type;

/**
 * TS type: 'string & number'
 */
public class TypeIntersection implements ITypeInfo {
    private final ITypeInfo leftType;
    private final ITypeInfo rightType;

    public TypeIntersection(ITypeInfo leftType, ITypeInfo rightType) {
        this.leftType = leftType;
        this.rightType = rightType;
    }

    public ITypeInfo left() {
        return this.leftType;
    }

    public ITypeInfo right() {
        return this.rightType;
    }

    @Override
    public Type getRaw() {
        return null;
    }

    @Override
    public ITypeInfo getBaseType() {
        return this.leftType;
    }

    @Override
    public Class<?> getResolvedClass() {
        return null;
    }

    @Override
    public String getTypeName() {
        return this.leftType.getTypeName() + " & "+this.rightType.getTypeName();
    }

    @Override
    public ITypeInfo copy() {
        return new TypeIntersection(this.leftType, this.rightType);
    }

    @Override
    public boolean assignableFrom(ITypeInfo info) {
        return this.leftType.assignableFrom(info) && this.rightType.assignableFrom(info);
    }
}