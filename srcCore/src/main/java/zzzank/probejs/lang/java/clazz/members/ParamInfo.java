package zzzank.probejs.lang.java.clazz.members;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.base.AnnotationHolder;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.ArrayType;
import zzzank.probejs.lang.java.type.impl.ClassType;
import zzzank.probejs.lang.java.type.impl.ParamType;
import zzzank.probejs.lang.java.type.impl.VariableType;
import zzzank.probejs.utils.NameUtils;

import java.lang.reflect.Parameter;
import java.util.Locale;

public class ParamInfo extends AnnotationHolder {
    @NotNull
    public String name;
    @NotNull
    public TypeDescriptor type;
    public final boolean varArgs;

    public ParamInfo(Parameter parameter, int index) {
        super(parameter.getAnnotations());
        this.type = TypeAdapter.getTypeDescription(parameter.getAnnotatedType());
        this.name = parameter.isNamePresent() ? parameter.getName() : autoParamName(type, index);
        this.varArgs = parameter.isVarArgs();
    }

    @NotNull
    public static String autoParamName(@NotNull TypeDescriptor type, int index) {
        if (type instanceof ClassType c) {
            val simpleName = c.clazz.getSimpleName();
            if (simpleName.isEmpty()) {
                return "arg" + index;
            }
            return NameUtils.firstLower(simpleName) + index;
        } else if (type instanceof ArrayType arr) {
            return autoParamName(arr.component, index) + 's';
        } else if (type instanceof ParamType param) {
            return autoParamName(param.base, index);
        } else if (type instanceof VariableType vari) {
            return vari.getSymbol().toLowerCase(Locale.ROOT) + index;
        }
        return "arg" + index;
    }

    @Override
    public String toString() {
        return String.format("ParamInfo(%s: %s%s)", name, varArgs ? "..." : "", type);
    }
}
