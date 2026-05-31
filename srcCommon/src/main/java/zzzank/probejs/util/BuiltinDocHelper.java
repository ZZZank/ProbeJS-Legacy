package zzzank.probejs.util;

import lombok.val;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.member.*;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportType;
import zzzank.probejs.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ZZZank
 */
public abstract class BuiltinDocHelper {
    public static void injectConvertibleTypeDecl(TypeScriptFile file, Collection<TypeDecl> convertibles) {
        var path = file.path;
        var classDecl = file.findCodeNullable(ClassDecl.class);
        if (classDecl == null) {
            return;
        }

        var allConvertibles = new ArrayList<BaseType>();
        var allTypeDecl = new ArrayList<TypeDecl>();

        allConvertibles.add(Types.type(path)
            .withPossibleParams(classDecl.variableTypes)
            .contextShield(BaseType.FormatType.RETURN));
        for (var decl : convertibles) {
            if (decl.name == null) {
                allConvertibles.add(decl.type);
            } else {
                allConvertibles.add(Types.primitive(decl.name));
                allTypeDecl.add(decl);
            }
        }

        var typeConvertible = Statements.type(ImportType.TYPE.fmt(path.getSimpleName()), Types.or(allConvertibles))
            .symbolVariables(classDecl.variableTypes)
            .exportDecl(true)
            .build();
        typeConvertible.addComment("""
            Use `Internal.{Type}` and `Internal.{Type}_` for referencing this type in JS file""");
        allTypeDecl.add(typeConvertible);

        file.addCodes(allTypeDecl);
    }

    public static void injectBeaning(ClassDecl classDecl, boolean convertFields) {
        val usedNames = new HashSet<String>();
        for (val method : classDecl.methods) {
            usedNames.add(method.name);
        }

        if (convertFields) {
            convertFieldToBeaning(classDecl, usedNames);
        }
        for (val field : classDecl.fields) {
            usedNames.add(field.name);
        }

        collectMethodBeaning(classDecl, usedNames);
    }

    private static void collectMethodBeaning(ClassDecl classDecl, Set<String> usedNames) {
        for (val method : classDecl.methods) {
            if (method.isStatic) {
                continue;
            }

            if (method.params.size() == 1) {
                val beanName = extractBeanName(method.name, "set");
                if (beanName != null && !usedNames.contains(beanName)) {
                    var paramDecl = method.params.get(0);
                    var setter = new BeanDecl.Setter(beanName, paramDecl.type);
                    setter.paramName = paramDecl.getTSSafeName(0);
                    classDecl.bodyCode.add(setter);
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

    private static void convertFieldToBeaning(ClassDecl clazzDecl, Set<String> excludedNames) {
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
