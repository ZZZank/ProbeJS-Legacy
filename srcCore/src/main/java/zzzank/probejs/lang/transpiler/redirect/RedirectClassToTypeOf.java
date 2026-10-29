package zzzank.probejs.lang.transpiler.redirect;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.ClassType;
import zzzank.probejs.lang.java.type.impl.ParamType;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.Set;

/// Redirect `ClassLike<T>` to `ClassLike<T> & (typeof T)`
///
/// @author ZZZank
public class RedirectClassToTypeOf implements TypeRedirect {

    private final Set<Class<?>> classLikes;

    public RedirectClassToTypeOf(Set<Class<?>> targets) {
        this.classLikes = targets;
    }

    public RedirectClassToTypeOf() {
        this(Set.of(Class.class));
    }

    @Override
    public boolean test(TypeDescriptor typeDescriptor, TypeConverter converter) {
        return typeDescriptor instanceof ParamType paramType
            && paramType.params.size() == 1
            && paramType.base instanceof ClassType base
            && classLikes.contains(base.clazz);
    }

    @Override
    public BaseType apply(TypeDescriptor typeDescriptor, TypeConverter converter) {
        // ClassLike<ABC>
        val type = converter.convertTypeExcluding(typeDescriptor, this);
        if (!(type instanceof TSParamType paramType)) {
            return type;
        }
        val param = paramType.params.get(0);
        if (param instanceof JSPrimitiveType || param instanceof TSVariableType) {
            return type;
        }
        return new WithTypeOf(type, Types.and(type, Types.typeOf(param)));
    }

    private static class WithTypeOf extends BaseType {
        private final BaseType original;
        private final BaseType withTypeOf;

        public WithTypeOf(BaseType original, BaseType withTypeOf) {
            this.original = original;
            this.withTypeOf = withTypeOf;
        }

        @Override
        public ImportInfos getImportInfos(@NotNull FormatType type) {
            return (type == FormatType.RETURN ? withTypeOf : original).getImportInfos(type);
        }

        @Override
        public String line(Declaration declaration, FormatType formatType) {
            return (formatType == FormatType.RETURN ? withTypeOf : original).line(declaration, formatType);
        }
    }
}
