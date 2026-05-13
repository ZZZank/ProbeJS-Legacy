package zzzank.probejs.docs;

import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.FieldDecl;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.NameUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ZZZank
 */
public class InjectBeaning implements ProbeJSPlugin {

    /// apply after all modification to method/field has been applied
    @Override
    public byte priority() {
        return -100;
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        if (files.scriptDump().scriptType != ScriptType.STARTUP) {
            // applied once
            return;
        }
        boolean convertFields = ProbeConfig.fieldAsBeaning.get();

        for (var file : files.globalFiles().values()) {
            if (file.path.isArtificial()) {
                continue;
            }
            var classDecl = file.findCodeNullable(ClassDecl.class);
            if (classDecl == null) {
                continue;
            }

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
    }

    private static void fromMethod(ClassDecl classDecl, Set<String> usedNames) {
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

    private static String extractBeanName(String name, String prefix) {
        if (name.length() <= prefix.length() || !name.startsWith(prefix)) {
            return null;
        }
        var substring = name.substring(prefix.length());
        if (!Character.isUpperCase(substring.charAt(0))) {
            // .setter() should not be transformed to .ter
            return null;
        }
        if (substring.length() > 1 && Character.isUpperCase(substring.charAt(1))) {
            // .isRGB() -> .RGB
            return substring;
        }
        // .getLevel() -> .level
        return NameUtils.firstLower(substring);
    }

    private static void fromField(ClassDecl clazzDecl, Set<String> excludedNames) {
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
