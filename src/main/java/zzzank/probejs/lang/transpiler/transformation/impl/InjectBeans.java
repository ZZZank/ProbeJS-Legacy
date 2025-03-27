package zzzank.probejs.lang.transpiler.transformation.impl;

import lombok.val;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
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
            if (method.params.size() == 1) {
                val beanName = extractBeanName(name, "set");
                if (beanName != null) {
                    classDecl.ensureSetters().add(
                        beanName,
                        new BeanDecl.Setter(name, method.params.get(0).type)
                    );
                }
            } else if (method.params.isEmpty()) {
                var beanName = extractBeanName(name, "get");
                if (beanName == null) {
                    beanName = extractBeanName(name, "is");
                }
                if (beanName != null && !names.contains(beanName)) {
                    classDecl.ensureGetters().put(beanName, new BeanDecl.Getter(beanName, method.returnType));
                    names.add(beanName);
                }
            }
        }
    }

    private String extractBeanName(String name, String prefix) {
        if (name.length() <= prefix.length() || !name.startsWith(prefix)) {
            return null;
        }
        val beanName = name.substring(prefix.length());
        return NameUtils.firstLower(beanName);
    }
}
