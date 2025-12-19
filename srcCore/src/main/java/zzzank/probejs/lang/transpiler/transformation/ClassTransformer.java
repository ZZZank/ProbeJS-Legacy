package zzzank.probejs.lang.transpiler.transformation;

import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.ConstructorDecl;
import zzzank.probejs.lang.typescript.code.member.FieldDecl;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;

/**
 * Accepts a Clazz and a transpiled TS file, modify the
 * file to respect some stuffs.
 */
public interface ClassTransformer {
    ClassTransformer NO_OP = new ClassTransformer() {};

    default void transform(Clazz clazz, ClassDecl classDecl) {
    }

    default void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
    }

    default void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
    }

    default void transformField(Clazz clazz, FieldInfo fieldInfo, FieldDecl fieldDecl) {
    }
}
