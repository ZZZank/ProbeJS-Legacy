package zzzank.probejs.lang.transpiler.transformation.impl;

import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;

import java.util.List;
import java.util.Map;

/**
 * Inject {@code [Symbol.iterator](): IterableIterator<T>;} for {@link Iterable}{@code <T>}.
 * <p>
 * Inject {@code [index: number]: T;} for {@link List}{@code <T>}.
 * <p>
 * Inject {@code [index: string | number]: V;} for {@link Map}{@code <K, V>}.
 */
public class InjectArray implements ClassTransformer {

    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        if (isDirectlyImplementing(clazz.getOriginal(), Iterable.class)) {
            val iterType = classDecl.methods.stream()
                .filter(m -> m.name.equals("iterator"))
                .filter(m -> m.returnType instanceof TSParamType)
                .map(m -> ((TSParamType) m.returnType).params.get(0))
                .findFirst()
                .orElse(null);
            if (iterType == null) {
                return;
            }

            classDecl.bodyCode.add(Types.format("[Symbol.iterator](): IterableIterator<%s>;", iterType));
        }

        // AbstractCollection is not a List, and AbstractList is not directly implementing Iterable
        if (isDirectlyImplementing(clazz.getOriginal(), List.class)) {
            BaseType iterType = classDecl.methods.stream()
                .filter(m -> m.name.equals("iterator") && m.params.isEmpty())
                .filter(m -> m.returnType instanceof TSParamType)
                .map(m -> ((TSParamType) m.returnType).params.get(0))
                .findFirst()
                .orElse(null);
            if (iterType == null) {
                return;
            }
            classDecl.bodyCode.add(Types.format("[index: number]: %s", iterType));
        }


        if (isDirectlyImplementing(clazz.getOriginal(), Map.class)) {
            BaseType valueType = classDecl.methods.stream()
                .filter(m -> m.name.equals("get") && m.params.size() == 1)
                .map(m -> m.returnType)
                .findFirst()
                .orElse(null);
            if (valueType == null) {
                return;
            }
            classDecl.bodyCode.add(Types.format("[index: string | number]: %s", valueType));
        }
    }

    private boolean isDirectlyImplementing(Class<?> toExamine, Class<?> target) {
        if (!target.isAssignableFrom(toExamine)) {
            return false;
        }
        val superClass = toExamine.getSuperclass();
        if (superClass == null || superClass == Object.class) {
            return true;
        }
        return !target.isAssignableFrom(superClass);
    }
}
