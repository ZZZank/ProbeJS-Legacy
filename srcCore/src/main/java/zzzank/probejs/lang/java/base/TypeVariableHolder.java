package zzzank.probejs.lang.java.base;

import zzzank.probejs.lang.java.type.impl.VariableType;
import zzzank.probejs.utils.CollectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.util.List;

public abstract class TypeVariableHolder extends AnnotationHolder {
    public final List<VariableType> variableTypes;

    public TypeVariableHolder(TypeVariable<?>[] variables, Annotation[] annotations) {
        super(annotations);
        this.variableTypes = CollectUtils.mapToList(variables, VariableType::new);
    }
}
