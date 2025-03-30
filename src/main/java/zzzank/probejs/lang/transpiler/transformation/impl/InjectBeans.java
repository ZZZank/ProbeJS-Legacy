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
    public final boolean convertFields;

    public InjectBeans(boolean convertFields) {
        this.convertFields = convertFields;
    }

    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        val usedNames = new HashSet<String>();
        for (val method : classDecl.methods) {
            usedNames.add(method.name);
        }

        if (convertFields) {
            fromField(classDecl, usedNames);
        }
        for (val field : classDecl.fields) {
            usedNames.add(field.name);
        }

        fromMethod(classDecl, usedNames);
    }

    private void fromMethod(ClassDecl classDecl, Set<String> usedNames) {
        for (val method : classDecl.methods) {
            if (method.isStatic) {
                continue;
            }

            if (method.params.size() == 1) {
                val beanName = extractBeanName(method.name, "set");
                if (beanName != null && !usedNames.contains(beanName)) {
                    classDecl.bodyCode.add(new BeanDecl.Setter(beanName, method.params.get(0).type));
                }
            } else if (method.params.isEmpty()) {
                var beanName = extractBeanName(method.name, "get");
                if (beanName == null) {
                    beanName = extractBeanName(method.name, "is");
                }
                if (beanName != null && !usedNames.contains(beanName)) {
                    classDecl.bodyCode.add(new BeanDecl.Getter(beanName, method.returnType));
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

    private void fromField(ClassDecl clazzDecl, Set<String> excludedNames) {
        val keptFields = new ArrayList<FieldDecl>();
        for (val field : clazzDecl.fields) {
            if (field.isStatic || field.isFinal || excludedNames.contains(field.name)) {
                keptFields.add(field);
                continue;
            }

            val getter = new BeanDecl.Getter(field.name, field.type);
            getter.comments.addAll(field.comments);
            clazzDecl.bodyCode.add(getter);

            val setter = new BeanDecl.Setter(field.name, field.type);
            setter.comments.addAll(field.comments);
            clazzDecl.bodyCode.add(setter);

            excludedNames.add(field.name);
        }
        clazzDecl.fields.clear();
        clazzDecl.fields.addAll(keptFields);
    }
}
