package zzzank.probejs.lang.transpiler.transformation.impl;

import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.ParamDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;

/**
 * Use hybrid to represent functional interfaces
 * <p>
 * {@code (a: SomeClass<number>, b: SomeClass<string>): void;}
 * <p>
 * barely useful now as we have type assignment
 * @author ZZZank
 */
public class InjectHybrid implements ClassTransformer {

    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        if (!clazz.isInterface()) {
            return;
        }
        val count = new int[]{0};
        val hybrid = classDecl.methods
            .stream()
            .filter(method -> !method.isStatic)
            .filter(method -> method.isAbstract)
            .peek(c -> count[0]++)
            .findFirst()
            .orElse(null);
        if (count[0] != 1) {
            return;
        }
        classDecl.bodyCode.add(Types.format(
            "%s: %s;",
            Types.custom((decl, formatType) -> ParamDecl.formatParams(hybrid.params, decl, BaseType.FormatType.RETURN)),
            hybrid.returnType.contextShield(BaseType.FormatType.INPUT)
        ));
    }
}
