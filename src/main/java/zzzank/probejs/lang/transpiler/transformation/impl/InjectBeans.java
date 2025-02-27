package zzzank.probejs.lang.transpiler.transformation.impl;

import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.utils.NameUtils;

import java.util.HashSet;
import java.util.Set;

public class InjectBeans implements ClassTransformer {
    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        Set<String> names = new HashSet<>();
        for (val method : classDecl.methods) {
            names.add(method.name);
        }
        for (val method : classDecl.methods) {
            if (method.isStatic) {
                continue;
            }
            if (method.name.startsWith("set") && method.params.size() == 1) {
                if (method.name.length() == 3) {
                    continue;
                }
                val beanName = NameUtils.firstLower(method.name.substring(3));
                if (names.contains(beanName)) {
                    continue;
                }
                classDecl.bodyCode.add(new BeanDecl.Getter(beanName, method.params.get(0).type));
            } else if (method.params.isEmpty()) {
                if (method.name.startsWith("get")) {
                    if (method.name.length() == 3) {
                        continue;
                    }
                    val beanName = NameUtils.firstLower(method.name.substring(3));
                    if (names.contains(beanName)) {
                        continue;
                    }
                    classDecl.bodyCode.add(new BeanDecl.Getter(beanName, method.returnType));
                } else if (method.name.startsWith("is")) {
                    if (method.name.length() == 2) {
                        continue;
                    }
                    val beanName = NameUtils.firstLower(method.name.substring(2));
                    if (names.contains(beanName)) {
                        continue;
                    }
                    classDecl.bodyCode.add(new BeanDecl.Getter(beanName, Types.BOOLEAN));
                }
            }
        }
    }
}
