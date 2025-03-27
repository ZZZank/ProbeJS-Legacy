package zzzank.probejs.lang.transpiler.transformation.impl;

import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.FieldDecl;
import zzzank.probejs.utils.NameUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class InjectBeans implements ClassTransformer {
    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        val excludedGetterNames = new HashSet<String>();
        for (val method : classDecl.methods) {
            excludedGetterNames.add(method.name);
        }

        if (ProbeConfig.fieldAsBean.get()) {
            fromField(classDecl, excludedGetterNames);
        }

        fromMethod(classDecl, excludedGetterNames);
    }

    private void fromMethod(ClassDecl classDecl, Set<String> excludedGetterNames) {
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
                if (beanName != null && !excludedGetterNames.contains(beanName)) {
                    classDecl.ensureGetters().put(beanName, new BeanDecl.Getter(beanName, method.returnType));
                    excludedGetterNames.add(beanName);
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

    private void fromField(ClassDecl clazzDecl, Set<String> excludedGetterNames) {
        val keptFields = new ArrayList<FieldDecl>();
        for (val field : clazzDecl.fields) {
            if (field.isStatic) {
                keptFields.add(field);
                continue;
            }

            if (!excludedGetterNames.contains(field.name)) {
                val getter = new BeanDecl.Getter(field.name, field.type);
                getter.comments.addAll(field.comments);
                clazzDecl.ensureGetters().put(getter.name, getter);
                excludedGetterNames.add(field.name);
            }

            if (!field.isFinal) {
                val setter = new BeanDecl.Setter(field.name, field.type);
                setter.comments.addAll(field.comments);
                clazzDecl.ensureSetters().add(setter.name, setter);
            }
        }
        clazzDecl.fields.clear();
        clazzDecl.fields.addAll(keptFields);
    }
}
