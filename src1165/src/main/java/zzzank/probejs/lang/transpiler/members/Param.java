package zzzank.probejs.lang.transpiler.members;

import zzzank.probejs.lang.java.clazz.members.ParamInfo;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.member.ParamDecl;

public class Param extends Converter<ParamInfo, ParamDecl> {
    public Param(TypeConverter converter) {
        super(converter);
    }

    @Override
    public ParamDecl transpile(ParamInfo input) {
        return new ParamDecl(input.name, converter.convertType(input.type), input.varArgs, false);
    }
}
