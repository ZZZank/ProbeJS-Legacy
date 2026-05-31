package zzzank.probejs.lang.java.clazz.members;

import lombok.val;
import org.jetbrains.annotations.Nullable;
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
    public String name;
    public TypeDescriptor type;
    public final boolean varArgs;

    public ParamInfo(Parameter parameter, int index) {
        super(parameter.getAnnotations());
        this.name = parameter.isNamePresent() ? parameter.getName() : autoParamName(type, index);
        this.type = TypeAdapter.getTypeDescription(parameter.getAnnotatedType());
        this.varArgs = parameter.isVarArgs();
    }

    @Nullable
    public static String autoParamName(TypeDescriptor type, int index) {
        if (type instanceof ClassType c) {
            val simpleName = c.clazz.getSimpleName();
            if (simpleName.isEmpty()) {
                return null;
            }
            return NameUtils.firstLower(simpleName) + index;
        } else if (type instanceof ArrayType arr) {
            return autoParamName(arr.component, index) + 's';
        } else if (type instanceof ParamType param) {
            return autoParamName(param.base, index);
        } else if (type instanceof VariableType vari) {
            return vari.getSymbol().toLowerCase(Locale.ROOT) + index;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("ParamInfo(%s: %s%s)", name, varArgs ? "..." : "", type);
    }
}
