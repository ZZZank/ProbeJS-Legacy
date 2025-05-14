package zzzank.probejs.lang.java.clazz;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.val;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.utils.ReflectUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class ClazzMemberCollector implements MemberCollector {

    private Set<String> names;
    private Class<?> clazz;

    @Override
    public void accept(Class<?> clazz) {
        this.clazz = clazz;
        this.names = new HashSet<>();
    }

    @Override
    public Stream<? extends ConstructorInfo> constructors() {
        return Arrays.stream(ReflectUtils.constructorsSafe(clazz))
            .filter(NO_HIDE_FROM_JS)
            .map(ConstructorInfo::new);
    }

    @Override
    public Stream<? extends MethodInfo> methods() {
        return Arrays.stream(ReflectUtils.methodsSafe(clazz))
            .peek(m -> names.add(RemapperBridge.remapMethod(clazz, m)))
            .filter(NO_HIDE_FROM_JS)
            .filter(m -> !m.isSynthetic())
            // interface in TS cannot skip declaring methods declared in super
            .filter(m -> clazz.isInterface() || !hasIdenticalParentMethod(m, clazz))
            .filter(m -> !m.getName().startsWith("jvmdowngrader$")) // remove JVMDG stub
            .sorted(Comparator.comparing(Method::getName))
            .map(method -> new MethodInfo(
                clazz,
                method,
                getGenericTypeReplacement(clazz, method)
            ));
    }

    @Override
    public Stream<? extends FieldInfo> fields() {
        return Arrays.stream(ReflectUtils.fieldsSafe(clazz))
            .filter(NO_HIDE_FROM_JS)
            .filter(f -> !names.contains(RemapperBridge.remapField(clazz, f)))
            .map(f -> new FieldInfo(clazz, f))
            .sorted(Comparator.comparing(f -> f.name));
    }

    public static final Predicate<AnnotatedElement> NO_HIDE_FROM_JS =
        element -> !element.isAnnotationPresent(HideFromJS.class);

    static boolean hasIdenticalParentMethod(Method method, Class<?> clazz) {
        if (method.getDeclaringClass() != clazz) {
            return true; // declared by super, of course there's an identical method in super
        }
        var parent = clazz.getSuperclass();
        while (parent != null) {
            try {
                val parentMethod = parent.getMethod(method.getName(), method.getParameterTypes());
                // If there is one, return type from "this class" is the same as or the subclass of "super class"
                return method.getGenericReturnType().equals(parentMethod.getGenericReturnType());
            } catch (NoSuchMethodException ignored) {
            }
            parent = parent.getSuperclass();
        }
        return false;
    }

    /**
     * getGenericTypeReplacementForParentInterfaceMethodsJustBecauseJavaDoNotKnowToReplaceThemWithGenericArgumentsOfThisClass
     */
    static Map<TypeVariable<?>, Type> getGenericTypeReplacement(
        Class<?> thisClass,
        Method thatMethod
    ) {
        val targetClass = thatMethod.getDeclaringClass();
        val interfaces = thisClass.getInterfaces();

        Map<TypeVariable<?>, Type> replacement = new HashMap<>();
        if (Arrays.asList(interfaces).contains(targetClass)) {
            return getInterfaceRemap(thisClass, targetClass);
        }
        val superInterface = Arrays
            .stream(interfaces)
            .filter(targetClass::isAssignableFrom)
            .findFirst()
            .orElse(null);
        if (superInterface == null) {
            return Collections.emptyMap();
        }
        val parentType = getGenericTypeReplacement(superInterface, thatMethod);
        val parentReplacement = getInterfaceRemap(thisClass, superInterface);

        for (val entry : parentType.entrySet()) {
            val variable = entry.getKey();
            val type = entry.getValue();

            replacement.put(variable,
                type instanceof TypeVariable<?> typeVariable
                    ? parentReplacement.getOrDefault(typeVariable, typeVariable)
                    : type
            );
        }
        return replacement;
    }

    static Map<TypeVariable<?>, Type> getInterfaceRemap(Class<?> thisClass, Class<?> thatInterface) {
        Map<TypeVariable<?>, Type> replacement = new HashMap<>();
        int indexOfInterface = -1;
        for (Type type : thisClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getRawType().equals(thatInterface)) {
                    indexOfInterface = 0;
                    for (TypeVariable<?> typeVariable : thatInterface.getTypeParameters()) {
                        replacement.put(typeVariable, parameterizedType.getActualTypeArguments()[indexOfInterface]);
                        indexOfInterface++;
                    }
                }
            } else if (type instanceof Class<?> clazz) {
                if (clazz.equals(thatInterface)) {
                    indexOfInterface = 0;
                    for (TypeVariable<?> typeVariable : thatInterface.getTypeParameters()) {
                        // Raw use of parameterized type, so we fill with Object.class
                        // Very bad programming practice, but we have to prepare for random people coding their stuffs bad
                        replacement.put(typeVariable, Object.class);
                    }
                }
            }
        }

        if (indexOfInterface == -1) {
            // throw new IllegalArgumentException("The class does not implement the target interface");
            return Collections.emptyMap();
        }

        return replacement;
    }
}
