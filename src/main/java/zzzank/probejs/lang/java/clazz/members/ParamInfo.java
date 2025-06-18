package zzzank.probejs.lang.java.clazz.members;

import zzzank.probejs.lang.java.base.AnnotationHolder;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import java.lang.reflect.Parameter;

public class ParamInfo extends AnnotationHolder {
    public String name;
    public TypeDescriptor type;
    public final boolean varArgs;

    public ParamInfo(Parameter parameter) {
        super(parameter.getAnnotations());
        this.name = parameter.getName();
        this.type = TypeAdapter.getTypeDescription(parameter.getAnnotatedType());
        this.varArgs = parameter.isVarArgs();
    }

    @Override
    public String toString() {
        return String.format("ParamInfo(%s: %s%s)", name, varArgs ? "..." : "", type);
    }
}
