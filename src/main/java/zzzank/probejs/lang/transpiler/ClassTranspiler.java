package zzzank.probejs.lang.transpiler;

import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.members.Constructor;
import zzzank.probejs.lang.transpiler.members.Converter;
import zzzank.probejs.lang.transpiler.members.Field;
import zzzank.probejs.lang.transpiler.members.Method;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.InterfaceDecl;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ClassTranspiler extends Converter<Clazz, ClassDecl> {

    private final Method method;
    private final Field field;
    private final Constructor constructor;
    private final ClassTransformer transformer;

    public ClassTranspiler(TypeConverter converter, ClassTransformer transformer) {
        super(converter);
        this.method = new Method(converter);
        this.field = new Field(converter);
        this.constructor = new Constructor(converter);
        this.transformer = transformer;
    }

    @Override
    public ClassDecl transpile(Clazz clazz) {
        val variableTypes = new ArrayList<TSVariableType>(clazz.variableTypes.size());
        for (val variableType : clazz.variableTypes) {
            variableTypes.add((TSVariableType) converter.convertType(variableType));
        }
        val superClass = clazz.superClass == null ? null : converter.convertTypeBuiltin(clazz.superClass);
        val interfaces = clazz.interfaces.stream()
            .map(converter::convertType)
            .filter(t -> t != Types.ANY)
            .collect(Collectors.toList());
        ClassDecl decl;
        if (clazz.isInterface()) {
            decl = new InterfaceDecl(
                clazz.classPath.getName(),
                superClass == Types.ANY ? null : superClass,
                interfaces,
                variableTypes
            );
        } else {
            decl = new ClassDecl(
                clazz.classPath.getName(),
                superClass == Types.ANY ? null : superClass,
                interfaces,
                variableTypes
            );
        }

        for (val fieldInfo : clazz.fields) {
            val fieldDecl = field.transpile(fieldInfo);
            transformer.transformField(clazz, fieldInfo, fieldDecl);
            decl.fields.add(fieldDecl);
        }

        for (val methodInfo : clazz.methods) {
            val methodDecl = method.transpile(methodInfo);
            transformer.transformMethod(clazz, methodInfo, methodDecl);
            decl.methods.add(methodDecl);
        }

        for (val constructorInfo : clazz.constructors) {
            val constructorDecl = constructor.transpile(constructorInfo);
            transformer.transformConstructor(clazz, constructorInfo, constructorDecl);
            decl.constructors.add(constructorDecl);
        }

        transformer.transform(clazz, decl);
        return decl;
    }
}
