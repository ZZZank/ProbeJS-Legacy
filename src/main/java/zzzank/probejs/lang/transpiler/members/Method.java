package zzzank.probejs.lang.transpiler.members;

import lombok.val;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.utils.CollectUtils;

import java.util.ArrayList;

public class Method extends Converter<MethodInfo, MethodDecl> {
    private final Param param;

    public Method(TypeConverter converter) {
        super(converter);
        this.param = new Param(converter);
    }

    @Override
    public MethodDecl transpile(MethodInfo input) {
        val decl = new MethodDecl(
            input.name,
            CollectUtils.mapToList(input.variableTypes, converter::convertVariableType),
            CollectUtils.mapToList(input.params, this.param::transpile),
            converter.convertType(input.returnType)
        );
        decl.isAbstract = input.attributes.isAbstract;
        decl.isStatic = input.attributes.isStatic;

        return decl;
    }
}
