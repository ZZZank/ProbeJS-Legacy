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
            val name = method.name;
            if (name.startsWith("set") && method.params.size() == 1) {
                if (name.length() == 3) {
                    continue;
                }
                val beanName = NameUtils.firstLower(name.substring(3));
                if (names.contains(beanName)) {
                    continue;
                }
                classDecl.bodyCode.add(new BeanDecl.Getter(beanName, method.params.get(0).type));
            } else if (method.params.isEmpty()) {
                if (name.startsWith("get")) {
                    if (name.length() == 3) {
                        continue;
                    }
                    val beanName = NameUtils.firstLower(name.substring(3));
                    if (names.contains(beanName)) {
                        continue;
                    }
                    classDecl.bodyCode.add(new BeanDecl.Getter(beanName, method.returnType));
                } else if (name.startsWith("is")) {
                    if (name.length() == 2) {
                        continue;
                    }
                    val beanName = NameUtils.firstLower(name.substring(2));
                    if (names.contains(beanName)) {
                        continue;
                    }
                    classDecl.bodyCode.add(new BeanDecl.Getter(beanName, Types.BOOLEAN));
                }
            }
        }
    }
}
