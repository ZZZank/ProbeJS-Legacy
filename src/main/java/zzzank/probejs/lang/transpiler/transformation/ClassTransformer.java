package zzzank.probejs.lang.transpiler.transformation;

import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.ConstructorDecl;
import zzzank.probejs.lang.typescript.code.member.FieldDecl;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.plugin.ProbeJSPlugins;

/**
 * Accepts a Clazz and a transpiled TS file, modify the
 * file to respect some stuffs.
 */
public interface ClassTransformer {

    static ClassTransformer fromPlugin(Transpiler transpiler) {
        val registration = new ClassTransformerRegistration(transpiler);
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.registerClassTransformer(registration));
        return new TransformerSequence(registration.getRegistered().toArray(new ClassTransformer[0]));
    }

    default void transform(Clazz clazz, ClassDecl classDecl) {
    }

    default void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
    }

    default void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
    }

    default void transformField(Clazz clazz, FieldInfo fieldInfo, FieldDecl fieldDecl) {
    }
}
