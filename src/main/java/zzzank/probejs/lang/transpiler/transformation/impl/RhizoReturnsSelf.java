package zzzank.probejs.lang.transpiler.transformation.impl;

import dev.latvian.mods.rhino.annotations.typing.ReturnsSelf;
import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.utils.CollectUtils;

/**
 * @author ZZZank
 */
public class RhizoReturnsSelf implements ClassTransformer {

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        if (methodInfo.hasAnnotation(ReturnsSelf.class)) {
            methodDecl.returnType = Types.THIS;
            return;
        }
        val matches = clazz.getAnnotations(ReturnsSelf.class);
        if (matches.isEmpty()) {
            return;
        }
        val classMatches = CollectUtils.mapToList(matches, ReturnsSelf::value);
        if (classMatches.contains(Object.class)) {
            if (methodInfo.returnType.asClass() == clazz.getOriginal()) {
                methodDecl.returnType = Types.THIS;
            }
        } else if (classMatches.contains(methodInfo.returnType.asClass())) {
            methodDecl.returnType = Types.THIS;
        }
    }
}
