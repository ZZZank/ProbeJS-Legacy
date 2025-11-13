package zzzank.probejs.lang.transpiler.redirect;

import lombok.val;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.ClassType;
import zzzank.probejs.lang.java.type.impl.ParamType;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;

import java.util.Set;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public class ClassRedirect implements TypeRedirect {

    private final Set<Class<?>> convertibles;

    public ClassRedirect(Set<Class<?>> targets) {
        this.convertibles = targets;
    }

    @Override
    public boolean test(TypeDescriptor typeDescriptor, TypeConverter converter) {
        return typeDescriptor instanceof ParamType paramType
            && paramType.params.size() == 1
            && paramType.base instanceof ClassType base
            && convertibles.contains(base.clazz);
    }

    @Override
    public BaseType apply(TypeDescriptor typeDescriptor, TypeConverter converter) {
        val type = converter.convertTypeExcluding(typeDescriptor, this);
        if (!(type instanceof TSParamType paramType)) {
            return type;
        }
        val param = paramType.params.get(0);
        if (param instanceof JSPrimitiveType || param instanceof TSVariableType) {
            return type;
        }
        val withTypeOf = Types.and(type, Types.typeOf(param));
        val selector = (Function<BaseType.FormatType, BaseType>)
            formatType -> formatType == BaseType.FormatType.RETURN ? withTypeOf : type;
        return Types.custom(
            (declaration, formatType) -> selector.apply(formatType).line(declaration, formatType),
            (formatType) -> selector.apply(formatType).getImportInfos(formatType)
        );
    }
}
