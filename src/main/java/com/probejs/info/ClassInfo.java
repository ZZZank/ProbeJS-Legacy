package com.probejs.info;

import com.probejs.ProbeJS;
import com.probejs.formatter.ClassResolver;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.FormatterMethod;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.TypeInfoResolver;
import com.probejs.info.type.TypeInfoParameterized;
import com.probejs.info.type.TypeInfoVariable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassInfo {

    public static final Map<Class<?>, ClassInfo> CLASS_CACHE = new HashMap<>();

    public static ClassInfo ofCache(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        //No computeIfAbsent because new ClassInfo will call ofCache for superclass lookup
        //This will cause a CME because multiple updates occurred in one computeIfAbsent
        if (CLASS_CACHE.containsKey(clazz)) {
            return CLASS_CACHE.get(clazz);
        }
        ClassInfo info = new ClassInfo(clazz);
        CLASS_CACHE.put(clazz, info);
        return info;
    }

    public static ClassInfo of(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return new ClassInfo(clazz);
    }

    private final Class<?> clazzRaw;
    private final String name;
    private final int modifiers;
    private final boolean isInterface;
    private final boolean isFunctionalInterface;
    private final List<ITypeInfo> parameters;
    private final List<MethodInfo> methodInfo;
    private final List<FieldInfo> fieldInfo;
    private final List<ConstructorInfo> constructorInfo;
    private final ClassInfo superClass;
    private final List<ClassInfo> interfaces;

    private ClassInfo(Class<?> clazz) {
        clazzRaw = clazz;
        name = clazzRaw.getName();
        modifiers = clazzRaw.getModifiers();
        isInterface = clazzRaw.isInterface();
        superClass = ofCache(clazzRaw.getSuperclass());

        interfaces = new ArrayList<>(0);
        constructorInfo = new ArrayList<>(0);
        parameters = new ArrayList<>(0);
        methodInfo = new ArrayList<>(0);
        fieldInfo = new ArrayList<>(0);
        try {
            interfaces.addAll(
                Arrays.stream(clazzRaw.getInterfaces()).map(ClassInfo::ofCache).collect(Collectors.toList())
            );
            constructorInfo.addAll(
                Arrays
                    .stream(clazzRaw.getConstructors())
                    .map(ConstructorInfo::new)
                    .collect(Collectors.toList())
            );
            parameters.addAll(
                Arrays
                    .stream(clazzRaw.getTypeParameters())
                    .map(TypeInfoResolver::resolveType)
                    .collect(Collectors.toList())
            );
            methodInfo.addAll(
                Arrays
                    .stream(clazzRaw.getMethods())
                    .filter(method -> {
                        if (!ProbeJS.CONFIG.trimming) {
                            return true;
                        }
                        if (isInterface) {
                            return method.getDeclaringClass() == clazzRaw;
                        }
                        return !hasIdenticalParentMethod(method);
                    })
                    .map(m -> new MethodInfo(m, clazz))
                    .filter(m -> ClassResolver.acceptMethod(m.getName()))
                    .filter(m -> !m.shouldHide())
                    .collect(Collectors.toList())
            );
            fieldInfo.addAll(
                Arrays
                    .stream(clazzRaw.getFields())
                    .filter(field -> !ProbeJS.CONFIG.trimming || field.getDeclaringClass() == clazzRaw)
                    .map(FieldInfo::new)
                    .filter(f -> ClassResolver.acceptField(f.getName()))
                    .filter(f -> !f.shouldHide())
                    .collect(Collectors.toList())
            );
        } catch (NoClassDefFoundError e) {
            // https://github.com/ZZZank/ProbeJS-Forge/issues/2
            ProbeJS.LOGGER.error("Unable to fetch infos for class '{}'", clazzRaw.getName());
        }
        //Resolve types - rollback everything till Object
        applySuperGenerics(methodInfo, fieldInfo);
        //Functional Interfaces
        List<MethodInfo> abstracts =
            this.methodInfo.stream().filter(MethodInfo::isAbstract).collect(Collectors.toList());
        this.isFunctionalInterface = isInterface && abstracts.size() == 1;
        if (this.isFunctionalInterface) {
            NameResolver.addSpecialAssignments(
                this.clazzRaw,
                () -> {
                    FormatterMethod formatterLmbda = new FormatterMethod(abstracts.get(0));
                    String lmbda = String.format(
                        "((%s)=>%s)",
                        formatterLmbda.formatParams(new HashMap<>(0), true),
                        formatterLmbda.formatReturn()
                    );
                    return Arrays.asList(lmbda);
                }
            );
        }
    }

    private static Map<String, ITypeInfo> resolveTypeOverrides(ITypeInfo typeInfo) {
        Map<String, ITypeInfo> caughtTypes = new HashMap<>();
        if (typeInfo instanceof TypeInfoParameterized) {
            TypeInfoParameterized parType = (TypeInfoParameterized) typeInfo;
            List<ITypeInfo> rawClassNames = Arrays
                .stream(parType.getResolvedClass().getTypeParameters())
                .map(TypeInfoResolver::resolveType)
                .collect(Collectors.toList());
            List<ITypeInfo> parTypeNames = parType.getParamTypes();
            for (int i = 0; i < parTypeNames.size(); i++) {
                caughtTypes.put(rawClassNames.get(i).getTypeName(), parTypeNames.get(i));
            }
        }
        return caughtTypes;
    }

    private void applySuperGenerics(List<MethodInfo> methodsToMutate, List<FieldInfo> fieldsToMutate) {
        if (superClass != null) {
            //Apply current level changes
            ITypeInfo typeInfo = TypeInfoResolver.resolveType(clazzRaw.getGenericSuperclass());
            Map<String, ITypeInfo> internalGenericMap = resolveTypeOverrides(typeInfo);
            applyGenerics(internalGenericMap, methodsToMutate, fieldsToMutate);
            Arrays
                .stream(clazzRaw.getGenericInterfaces())
                .map(TypeInfoResolver::resolveType)
                .map(ClassInfo::resolveTypeOverrides)
                .forEach(m -> applyGenerics(m, methodsToMutate, fieldsToMutate));
            //Step to next level
            superClass.applySuperGenerics(methodsToMutate, fieldsToMutate);
            //Rewind
            applyGenerics(internalGenericMap, methodsToMutate, fieldsToMutate);
            Arrays
                .stream(clazzRaw.getGenericInterfaces())
                .map(TypeInfoResolver::resolveType)
                .map(ClassInfo::resolveTypeOverrides)
                .forEach(m -> applyGenerics(m, methodsToMutate, fieldsToMutate));
        }
        applyInterfaceGenerics(methodsToMutate, fieldsToMutate);
    }

    private void applyInterfaceGenerics(List<MethodInfo> methodsToMutate, List<FieldInfo> fieldsToMutate) {
        //Apply current level changes
        Arrays
            .stream(clazzRaw.getGenericInterfaces())
            .map(TypeInfoResolver::resolveType)
            .map(ClassInfo::resolveTypeOverrides)
            .forEach(m -> applyGenerics(m, methodsToMutate, fieldsToMutate));
        //Step to next level
        interfaces.forEach(i -> i.applyInterfaceGenerics(methodsToMutate, fieldsToMutate));
        //Rewind
        Arrays
            .stream(clazzRaw.getGenericInterfaces())
            .map(TypeInfoResolver::resolveType)
            .map(ClassInfo::resolveTypeOverrides)
            .forEach(m -> applyGenerics(m, methodsToMutate, fieldsToMutate));
    }

    private static void applyGenerics(
        Map<String, ITypeInfo> internalGenericMap,
        List<MethodInfo> methodInfo,
        List<FieldInfo> fieldInfo
    ) {
        for (MethodInfo method : methodInfo) {
            Map<String, ITypeInfo> maskedNames = new HashMap<>();
            method
                .getTypeVariables()
                .stream()
                .filter(i -> i instanceof TypeInfoVariable)
                .map(i -> (TypeInfoVariable) i)
                .forEach(v -> {
                    maskedNames.put(v.getTypeName(), v);
                    v.setUnderscored(true);
                });

            method.setReturnType(TypeInfoResolver.mutateTypeMap(method.getReturnType(), maskedNames));
            method
                .getParams()
                .forEach(p -> p.setTypeInfo(TypeInfoResolver.mutateTypeMap(p.getType(), maskedNames)));

            method.setReturnType(TypeInfoResolver.mutateTypeMap(method.getReturnType(), internalGenericMap));
            method
                .getParams()
                .forEach(p -> p.setTypeInfo(TypeInfoResolver.mutateTypeMap(p.getType(), internalGenericMap)));
        }
        for (FieldInfo field : fieldInfo) {
            field.setTypeInfo(TypeInfoResolver.mutateTypeMap(field.getType(), internalGenericMap));
        }
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isFunctionalInterface() {
        return isFunctionalInterface;
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }

    public ClassInfo getSuperClass() {
        return superClass;
    }

    public List<ClassInfo> getInterfaces() {
        return interfaces;
    }

    public List<FieldInfo> getFieldInfo() {
        return fieldInfo;
    }

    public List<ConstructorInfo> getConstructorInfo() {
        return constructorInfo;
    }

    public List<MethodInfo> getMethodInfo() {
        return methodInfo;
    }

    public List<ITypeInfo> getParameters() {
        return parameters;
    }

    public boolean isEnum() {
        return clazzRaw.isEnum();
    }

    public Class<?> getClazzRaw() {
        return clazzRaw;
    }

    public String getName() {
        return name;
    }

    /**
     * seems not working for parameterized interfaces
     */
    private boolean hasIdenticalParentMethod(Method method) {
        if (method.isDefault()) {
            return false;
        }
        for (
            Class<?> parent = this.clazzRaw.getSuperclass();
            parent != null;
            parent = parent.getSuperclass()
        ) {
            try {
                Method parentMethod = parent.getMethod(method.getName(), method.getParameterTypes());
                // Check if the generic return type is the same
                // seems not working for interfaces, e.g. RecipeFilter
                return parentMethod.getGenericReturnType().equals(method.getGenericReturnType());
            } catch (NoSuchMethodException ignored) {}
        }
        return false;
    }
}
