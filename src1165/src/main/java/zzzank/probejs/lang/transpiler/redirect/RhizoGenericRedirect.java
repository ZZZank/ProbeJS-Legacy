package zzzank.probejs.lang.transpiler.redirect;

import dev.latvian.mods.rhino.annotations.typing.Generics;
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
        return typeDescriptor.hasAnnotation(Generics.class);
    }

    @Override
    public BaseType apply(TypeDescriptor typeDescriptor, TypeConverter converter) {
        var annot = typeDescriptor.getAnnotation(Generics.class);
        assert annot != null;

        if (annot.base() != Object.class) {
            return converter.convertType(annot.base());
        }

        var base = converter.convertType(((ParamType) typeDescriptor).base);
        var params = CollectUtils.mapToList(annot.value(), converter::convertType);
        return Types.parameterized(base, params);
    }
}
