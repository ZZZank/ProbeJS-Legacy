package zzzank.probejs.lang.transpiler.members;

import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.member.ConstructorDecl;
import zzzank.probejs.utils.CollectUtils;

public class Constructor extends Converter<ConstructorInfo, ConstructorDecl> {
    private final Param param;

    public Constructor(TypeConverter converter) {
        super(converter);
        this.param = new Param(converter);
    }

    @Override
    public ConstructorDecl transpile(ConstructorInfo input) {
        return new ConstructorDecl(
            CollectUtils.mapToList(input.variableTypes, converter::convertType),
            CollectUtils.mapToList(input.params, param::transpile)
        );
    }
}
