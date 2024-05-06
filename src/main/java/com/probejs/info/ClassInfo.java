package com.probejs.info;

import com.probejs.ProbeJS;
import com.probejs.formatter.resolver.ClazzFilter;
import com.probejs.formatter.resolver.NameResolver;
import com.probejs.formatter.FormatterMethod;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.TypeInfoParameterized;
import com.probejs.info.type.TypeInfoVariable;
import com.probejs.info.type.TypeResolver;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ClassInfo implements Comparable<ClassInfo> {

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

    @Getter
    private final Class<?> raw;
    @Getter
    private final String name;
    private final int modifiers;
    private final boolean isInterface;
    private final boolean isFunctionalInterface;
    @Getter
    private final List<TypeInfoVariable> typeParameters;
    /**
     * filtered view of {@link ClassInfo#allMethodInfos}
     */
    @Getter
    private final List<MethodInfo> methodInfos;
    /**
     * filtered view of {@link ClassInfo#allFieldInfos}
     */
    @Getter
    private final List<FieldInfo> fieldInfos;
    @Getter
    private final List<ConstructorInfo> constructorInfos;
    @Getter
    private final ClassInfo superClass;
    @Getter
    private final ITypeInfo superType;
    @Getter
    private final List<ITypeInfo> interfaces;
    private final List<MethodInfo> allMethodInfos;
    private final List<FieldInfo> allFieldInfos;

    private ClassInfo(Class<?> clazz) {
        this.raw = clazz;
        this.name = raw.getName();
        this.modifiers = raw.getModifiers();
        this.isInterface = raw.isInterface();
        this.superClass = ofCache(raw.getSuperclass());
        this.superType = TypeResolver.resolveType(clazz.getGenericSuperclass());

        this.interfaces = new ArrayList<>(0);
        this.constructorInfos = new ArrayList<>(0);
        this.typeParameters = new ArrayList<>(0);
        this.methodInfos = new ArrayList<>(0);
        this.fieldInfos = new ArrayList<>(0);
        this.allMethodInfos = new ArrayList<>(0);
        this.allFieldInfos = new ArrayList<>(0);
        try {
            interfaces.addAll(
            Arrays.stream(clazz.getGenericInterfaces()).map(TypeResolver::resolveType).collect(Collectors.toList())
            );
            constructorInfos.addAll(
                Arrays
                    .stream(raw.getConstructors())
                    .map(ConstructorInfo::new)
                    .collect(Collectors.toList())
            );
            typeParameters.addAll(
                Arrays
                    .stream(raw.getTypeParameters())
                    .map(TypeInfoVariable::new)
                    .collect(Collectors.toList())
            );
            //methods
            Arrays
                .stream(raw.getMethods())
                .map(m -> new MethodInfo(m, clazz))
                .peek(allMethodInfos::add)
                .filter(mInfo -> {
                    if (!ProbeJS.CONFIG.trimming) {
                        return true;
                    }
                    return !hasIdenticalParentMethod(mInfo.getRaw(), clazz);
                })
                .filter(m -> ClazzFilter.acceptMethod(m.getName()))
                .filter(m -> !m.shouldHide())
                .forEach(methodInfos::add);
            //fields
            Arrays
                .stream(raw.getFields())
                .map(f -> new FieldInfo(f, clazz))
                .peek(allFieldInfos::add)
                .filter(fInfo -> !ProbeJS.CONFIG.trimming || fInfo.getRaw().getDeclaringClass() == raw)
                .filter(f -> ClazzFilter.acceptField(f.getName()))
                .filter(f -> !f.shouldHide())
                .forEach(fieldInfos::add);
        } catch (NoClassDefFoundError e) {
            // https://github.com/ZZZank/ProbeJS-Forge/issues/2
            ProbeJS.LOGGER.error("Unable to fetch infos for class '{}'", raw.getName());
            e.printStackTrace();
        }
        //Resolve types - rollback everything till Object
        applySuperGenerics(methodInfos, fieldInfos);
        //Functional Interfaces
        List<MethodInfo> abstracts =
            this.methodInfos.stream().filter(MethodInfo::isAbstract).collect(Collectors.toList());
        this.isFunctionalInterface = isInterface && abstracts.size() == 1;
        if (this.isFunctionalInterface) {
            NameResolver.addSpecialAssignments(
                this.raw,
                () -> {
                    FormatterMethod formatterLmbda = new FormatterMethod(abstracts.get(0));
                    String lmbda = String.format(
                        "((%s)=>%s)",
                        formatterLmbda.formatParams(new HashMap<>(0), true),
                        formatterLmbda.formatReturn()
                    );
                    return Collections.singletonList(lmbda);
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
                .map(TypeResolver::resolveType)
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
            ITypeInfo typeInfo = TypeResolver.resolveType(raw.getGenericSuperclass());
            Map<String, ITypeInfo> internalGenericMap = resolveTypeOverrides(typeInfo);
            applyGenerics(internalGenericMap, methodsToMutate, fieldsToMutate);
            Arrays
                .stream(raw.getGenericInterfaces())
                .map(TypeResolver::resolveType)
                .map(ClassInfo::resolveTypeOverrides)
                .forEach(m -> applyGenerics(m, methodsToMutate, fieldsToMutate));
            //Step to next level
            superClass.applySuperGenerics(methodsToMutate, fieldsToMutate);
            //Rewind
            applyGenerics(internalGenericMap, methodsToMutate, fieldsToMutate);
            Arrays
                .stream(raw.getGenericInterfaces())
                .map(TypeResolver::resolveType)
                .map(ClassInfo::resolveTypeOverrides)
                .forEach(m -> applyGenerics(m, methodsToMutate, fieldsToMutate));
        }
        applyInterfaceGenerics(methodsToMutate, fieldsToMutate);
    }

    private void applyInterfaceGenerics(List<MethodInfo> methodsToMutate, List<FieldInfo> fieldsToMutate) {
        //Apply current level changes
        Arrays
            .stream(raw.getGenericInterfaces())
            .map(TypeResolver::resolveType)
            .map(ClassInfo::resolveTypeOverrides)
            .forEach(m -> applyGenerics(m, methodsToMutate, fieldsToMutate));
        //Step to next level
        interfaces.forEach(i -> ofCache(i.getResolvedClass()).applyInterfaceGenerics(methodsToMutate, fieldsToMutate));
        //Rewind
        Arrays
            .stream(raw.getGenericInterfaces())
            .map(TypeResolver::resolveType)
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
                .forEach(v -> maskedNames.put(v.getTypeName(), v));

            method.setReturnType(TypeResolver.mutateTypeMap(method.getReturnType(), maskedNames));
            method
                .getParams()
                .forEach(p -> p.setType(TypeResolver.mutateTypeMap(p.getType(), maskedNames)));

            method.setReturnType(TypeResolver.mutateTypeMap(method.getReturnType(), internalGenericMap));
            method
                .getParams()
                .forEach(p -> p.setType(TypeResolver.mutateTypeMap(p.getType(), internalGenericMap)));
        }
        for (FieldInfo field : fieldInfo) {
            field.setTypeInfo(TypeResolver.mutateTypeMap(field.getType(), internalGenericMap));
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

    public boolean isEnum() {
        return raw.isEnum();
    }

    /**
     * seems not working for parameterized interfaces
     */
    private static boolean hasIdenticalParentMethod(Method method, Class<?> clazz) {
        if (method.isDefault()) {
            return false;
        }
        for (Class<?> parent = clazz.getSuperclass(); parent != null; parent = parent.getSuperclass()) {
            try {
                Method parentMethod = parent.getMethod(method.getName(), method.getParameterTypes());
                // seems not working for interfaces, e.g. RecipeFilter
                return parentMethod.getGenericReturnType().equals(method.getGenericReturnType());
            } catch (NoSuchMethodException ignored) {}
        }
        return false;
    }

    @Override
    public int compareTo(ClassInfo o) {
        return this.name.compareTo(o.name);
    }
}
