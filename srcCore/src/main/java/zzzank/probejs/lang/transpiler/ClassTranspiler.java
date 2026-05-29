package zzzank.probejs.lang.transpiler;

import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.type.impl.VariableType;
import zzzank.probejs.lang.java.type.impl.WildType;
import zzzank.probejs.lang.transpiler.members.Constructor;
import zzzank.probejs.lang.transpiler.members.Converter;
import zzzank.probejs.lang.transpiler.members.Field;
import zzzank.probejs.lang.transpiler.members.Method;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.InterfaceDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.utils.CollectUtils;

import java.util.*;

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

        val variableTypes = convertVariableDeclaration(converter, clazz.variableTypes);
        val superClass = clazz.superClass == null
            ? null
            : converter.convertTypeBuiltin(clazz.superClass);
        val interfaces = CollectUtils.mapToList(
            clazz.interfaces,
            t -> (BaseType) converter.convertType(t).contextShield(BaseType.FormatType.INTERFACE)
        );

        ClassDecl decl;
        if (clazz.isInterface()) {
            decl = new InterfaceDecl(
                clazz.classPath.getSimpleName(),
                superClass == Types.ANY ? null : superClass,
                interfaces,
                variableTypes
            );
        } else {
            decl = new ClassDecl(
                clazz.classPath.getSimpleName(),
                superClass == Types.ANY ? null : superClass,
                interfaces,
                variableTypes
            );
        }
        decl.nativeClazz = clazz;

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

    public static List<TSVariableType> convertVariableDeclaration(
        TypeConverter converter,
        List<VariableType> variableTypes
    ) {
        if (variableTypes.isEmpty()) {
            return List.of();
        }

        var result = new ArrayList<TSVariableType>();

        // when declaring variables, we can assume that they all come from the same class
        // use TreeSet to improve efficiency in small variable list, and ensure solid performance lower bound at scale
        var declared = new TreeSet<VariableType>(Comparator.comparing(t -> t.raw().getName()));
        for (var variableType : variableTypes) {
            var bounds = variableType.getDescriptors();

            var converted = converter.convertVariableType(variableType);
            converted.defaultTo = Types.and(CollectUtils.mapToList(
                bounds,
                t -> converter.convertType(
                    t.consolidate((type) ->
                        declared.contains(type) ? type : WildType.NO_BOUND))
            ));
            result.add(converted);

            declared.add(variableType);
        }

        return result;
    }
}
