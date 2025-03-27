package zzzank.probejs.lang.transpiler.redirect;

import dev.latvian.mods.rhino.annotations.typing.Generics;
import lombok.val;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.ParamType;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.utils.CollectUtils;

/**
 * @author ZZZank
 */
public class RhizoGenericRedirect implements TypeRedirect {

    @Override
    public boolean test(TypeDescriptor typeDescriptor, TypeConverter converter) {
        return typeDescriptor instanceof ParamType
            && typeDescriptor.hasAnnotation(Generics.class);
    }

    @Override
    public BaseType apply(TypeDescriptor typeDescriptor, TypeConverter converter) {
        val paramType = (ParamType) typeDescriptor;
        val annot = typeDescriptor.getAnnotation(Generics.class);
        val baseType = annot.base() == Object.class
            ? converter.convertType(paramType.base)
            : Types.type(annot.base());
        val params = CollectUtils.mapToList(annot.value(), converter::convertType);
        return Types.parameterized(baseType, params);
    }
}
