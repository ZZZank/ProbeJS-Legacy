package zzzank.probejs.lang.transpiler.members;


import lombok.val;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.member.FieldDecl;

public class Field extends Converter<FieldInfo, FieldDecl> {
    public Field(TypeConverter converter) {
        super(converter);
    }

    @Override
    public FieldDecl transpile(FieldInfo input) {
        val decl = new FieldDecl(input.name, converter.convertType(input.type));
        decl.isFinal = input.attributes.isFinal;
        decl.isStatic = input.attributes.isStatic;

        return decl;
    }
}
