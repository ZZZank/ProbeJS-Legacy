package com.probejs.formatter.formatter;

import com.google.gson.Gson;
import com.probejs.ProbeConfig;
import com.probejs.document.DocumentClass;
import com.probejs.document.DocumentComment;
import com.probejs.document.DocumentField;
import com.probejs.document.DocumentMethod;
import com.probejs.document.Manager;
import com.probejs.document.comment.special.CommentHidden;
import com.probejs.document.type.TypeNamed;
import com.probejs.formatter.NameResolver;
import com.probejs.info.*;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.InfoTypeResolver;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.info.type.TypeInfoParameterized;
import com.probejs.util.PUtil;
import dev.latvian.kubejs.recipe.RecipeEventJS;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

public class FormatterClass extends DocumentReceiver<DocumentClass> implements IFormatter {

    private final ClassInfo classInfo;
    private final Map<String, FormatterField> fieldFormatters = new HashMap<>();
    private final Map<String, List<FormatterMethod>> methodFormatters = new HashMap<>();
    private final List<DocumentField> fieldAdditions = new ArrayList<>();
    private final List<DocumentMethod> methodAdditions = new ArrayList<>();
    private boolean internal = false;

    public FormatterClass(ClassInfo classInfo) {
        this.classInfo = classInfo;
        for (MethodInfo methodInfo : classInfo.getMethodInfo()) {
            // TODO: dirty hack, should remove
            if (
                classInfo.getName().equals(RecipeEventJS.class.getName()) &&
                methodInfo.getName().equals("getRecipes")
            ) {
                continue;
            }
            methodFormatters
                .computeIfAbsent(methodInfo.getName(), s -> new ArrayList<>())
                .add(new FormatterMethod(methodInfo));
        }
        for (FieldInfo fieldInfo : classInfo.getFieldInfo()) {
            fieldFormatters.put(fieldInfo.getName(), new FormatterField(fieldInfo));
        }
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        Gson gson = new Gson();
        List<String> formatted = new ArrayList<>();
        DocumentComment comment = document == null ? null : document.getComment();
        if (comment != null) {
            if (comment.getSpecialComment(CommentHidden.class) != null) return formatted;
            formatted.addAll(comment.format(indent, stepIndent));
        }

        List<String> assignableTypes = Manager.typesAssignable
            .getOrDefault(classInfo.getClazzRaw().getName(), new ArrayList<>())
            .stream()
            .map(t ->
                t.getTransformedName((i, s) -> {
                    if (!(i instanceof TypeNamed)) {
                        return s;
                    }
                    TypeNamed named = (TypeNamed) i;
                    if (
                        NameResolver.resolvedNames.containsKey(named.getRawTypeName()) &&
                        !NameResolver.resolvedPrimitives.contains(named.getRawTypeName())
                    ) {
                        return s + "_";
                    }
                    return s;
                })
            )
            .collect(Collectors.toList());

        if (classInfo.isEnum()) {
            //TODO: add special processing for KubeJS
            Class<?> clazz = classInfo.getClazzRaw();
            try {
                Method values = clazz.getMethod("values");
                values.setAccessible(true);
                Object[] enumValues = (Object[]) values.invoke(null);
                for (Object enumValue : enumValues) {
                    //Use the name() here so won't be affected by overrides
                    Method name = Enum.class.getMethod("name");
                    assignableTypes.add(
                        gson.toJson(name.invoke(enumValue).toString().toLowerCase(Locale.ROOT))
                    );
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // First line
        List<String> firstLine = new ArrayList<>();
        if (!internal) {
            firstLine.add("declare");
        }
        if (classInfo.isInterface()) {
            firstLine.add("interface");
        } else if (classInfo.isAbstract()) {
            firstLine.add("abstract class");
        } else {
            firstLine.add("class");
        }
        firstLine.add(NameResolver.getResolvedName(classInfo.getName()).getLastName());
        if (classInfo.getClazzRaw().getTypeParameters().length != 0) {
            firstLine.add(
                String.format(
                    "<%s>",
                    Arrays
                        .stream(classInfo.getClazzRaw().getTypeParameters())
                        .map(TypeVariable::getName)
                        .collect(Collectors.joining(", "))
                )
            );
        }
        // super class
        if (classInfo.getSuperClass() != null) {
            firstLine.add("extends");
            Class<?> superClass = classInfo.getClazzRaw().getSuperclass();
            if (superClass == Object.class) {
                // redirect to another `Object` so that we can bypass replacement of original `Object`
                firstLine.add("Document.Object");
            } else {
                firstLine.add(
                    formatTypeParameterized(
                        InfoTypeResolver.resolveType(classInfo.getClazzRaw().getGenericSuperclass())
                    )
                );
            }
        }
        // interface
        if (!classInfo.getInterfaces().isEmpty()) {
            firstLine.add(classInfo.isInterface() ? "extends" : "implements");
            firstLine.add(
                String.format(
                    "%s",
                    Arrays
                        .stream(classInfo.getClazzRaw().getGenericInterfaces())
                        .map(InfoTypeResolver::resolveType)
                        .map(FormatterClass::formatTypeParameterized)
                        .collect(Collectors.joining(", "))
                )
            );
        }
        firstLine.add("{");
        formatted.add(PUtil.indent(indent) + String.join(" ", firstLine));
        // first line processing, end

        // Fields, methods
        methodFormatters
            .values()
            .forEach(fmtrMethods ->
                fmtrMethods
                    .stream()
                    .filter(fmtrMethod ->
                        ProbeConfig.INSTANCE.keepBeaned ||
                        fmtrMethod.getBean() == null ||
                        fieldFormatters.containsKey(fmtrMethod.getBean()) ||
                        methodFormatters.containsKey(fmtrMethod.getBean())
                    )
                    .forEach(fmtrMethod -> {
                        if (classInfo.isInterface() && fmtrMethod.getMethodInfo().isStatic() && internal) {
                            return;
                        }
                        formatted.addAll(fmtrMethod.format(indent + stepIndent, stepIndent));
                    })
            );
        fieldFormatters
            .entrySet()
            .stream()
            .filter(e -> !methodFormatters.containsKey(e.getKey()))
            .forEach(f -> {
                if (classInfo.isInterface() && f.getValue().getFieldInfo().isStatic() && internal) return;
                f.getValue().setInterface(classInfo.isInterface());
                formatted.addAll(f.getValue().format(indent + stepIndent, stepIndent));
            });

        // beans
        if (!classInfo.isInterface()) {
            Map<String, FormatterMethod> getterMap = new HashMap<>();
            Map<String, List<FormatterMethod>> setterMap = new HashMap<>();

            methodFormatters
                .values()
                .forEach(ml ->
                    ml.forEach(m -> {
                        String beanName = m.getBean();
                        if (
                            beanName != null &&
                            Character.isUpperCase(beanName.charAt(0)) &&
                            !fieldFormatters.containsKey(beanName) &&
                            !methodFormatters.containsKey(beanName)
                        ) {
                            if (m.isGetter()) {
                                getterMap.put(beanName, m);
                            } else {
                                setterMap.computeIfAbsent(beanName, s -> new ArrayList<>()).add(m);
                            }
                        }
                    })
                );

            getterMap.forEach((k, v) -> formatted.addAll(v.formatBean(indent + stepIndent, stepIndent)));
            setterMap.forEach((k, v) -> {
                Optional<FormatterMethod> result = v
                    .stream()
                    .filter(m ->
                        !getterMap.containsKey(m.getBean()) ||
                        getterMap.get(m.getBean()).getBeanTypeString().equals(m.getBeanTypeString())
                    )
                    .findFirst();
                if (result.isPresent()) {
                    formatted.addAll(result.get().formatBean(indent + stepIndent, stepIndent));
                }
            });
        }

        // constructors
        if (!classInfo.isInterface()) {
            if (internal) {
                String indnt = PUtil.indent(indent + stepIndent);
                formatted.add(indnt + "/**");
                formatted.add(
                    indnt +
                    " * Internal constructor, this means that it's not valid and you will get an error if you use it."
                );
                formatted.add(indnt + " */");
                formatted.add(indnt + "protected constructor();");
            } else {
                classInfo
                    .getConstructorInfo()
                    .stream()
                    .map(FormatterConstructor::new)
                    .forEach(f -> formatted.addAll(f.format(indent + stepIndent, stepIndent)));
            }
        }
        // additions
        fieldAdditions.forEach(fieldDoc -> formatted.addAll(fieldDoc.format(indent + stepIndent, stepIndent))
        );
        methodAdditions.forEach(methodDoc ->
            formatted.addAll(methodDoc.format(indent + stepIndent, stepIndent))
        );

        formatted.add(PUtil.indent(indent) + "}");
        //type conversion
        String origName = NameResolver.getResolvedName(classInfo.getName()).getLastName();
        String underName = origName + "_";
        if (NameResolver.specialTypeFormatters.containsKey(classInfo.getClazzRaw())) {
            assignableTypes.add(
                new FormatterType(
                    new TypeInfoParameterized(
                        new TypeInfoClass(classInfo.getClazzRaw()),
                        classInfo.getParameters()
                    )
                )
                    .format(0, 0)
            );
        }
        List<ITypeInfo> params = classInfo.getParameters();
        if (params.size() > 0) {
            String paramString = String.format(
                "<%s>",
                params.stream().map(ITypeInfo::getTypeName).collect(Collectors.joining(", "))
            );
            underName += paramString;
            origName += paramString;
        }

        assignableTypes.add(origName);
        formatted.add(
            PUtil.indent(indent) +
            String.format("type %s = %s;", underName, String.join(" | ", assignableTypes))
        );
        return formatted;
    }

    @Override
    public void addDocument(DocumentClass document) {
        super.addDocument(document);
        document
            .getFields()
            .forEach(documentField -> {
                if (fieldFormatters.containsKey(documentField.getName())) {
                    fieldFormatters.get(documentField.getName()).addDocument(documentField);
                } else {
                    fieldAdditions.add(documentField);
                }
            });

        document
            .getMethods()
            .forEach(documentMethod -> {
                if (methodFormatters.containsKey(documentMethod.getName())) {
                    methodFormatters
                        .get(documentMethod.getName())
                        .forEach(formatterMethod -> {
                            if (documentMethod.testMethod(formatterMethod.getMethodInfo())) {
                                formatterMethod.addDocument(documentMethod);
                            }
                        });
                } else {
                    methodAdditions.add(documentMethod);
                }
            });
    }

    public static String formatTypeParameterized(ITypeInfo info) {
        StringBuilder sb = new StringBuilder(new FormatterType(info, false).format(0, 0));
        if (info instanceof TypeInfoClass) {
            TypeInfoClass clazz = (TypeInfoClass) info;
            if (clazz.getTypeVariables().size() != 0) {
                sb.append(
                    String.format(
                        "<%s>",
                        String.join(", ", Collections.nCopies(clazz.getTypeVariables().size(), "any"))
                    )
                );
            }
        }
        return sb.toString();
    }
}
