package zzzank.probejs.lang.transpiler.transformation;

import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.ConstructorDecl;
import zzzank.probejs.lang.typescript.code.member.FieldDecl;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.utils.Asser;

/**
 * @author ZZZank
 */
public final class TransformerSequence implements ClassTransformer {
    private final ClassTransformer[] transformers;

    public TransformerSequence(ClassTransformer[] transformers) {
        this.transformers = Asser.tNotNullAll(transformers, "transformers");
    }

    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        for (val transformer : transformers) {
            transformer.transform(clazz, classDecl);
        }
    }

    @Override
    public void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        for (val transformer : transformers) {
            transformer.transformConstructor(clazz, constructorInfo, constructorDecl);
        }
    }

    @Override
    public void transformField(Clazz clazz, FieldInfo fieldInfo, FieldDecl fieldDecl) {
        for (val transformer : transformers) {
            transformer.transformField(clazz, fieldInfo, fieldDecl);
        }
    }

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        for (val transformer : transformers) {
            transformer.transformMethod(clazz, methodInfo, methodDecl);
        }
    }
}
