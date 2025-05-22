package zzzank.probejs.lang.java.clazz;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.rhino.JavaMembers;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.val;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class ClazzMemberCollector implements MemberCollector {

    private Class<?> clazz;
    private JavaMembers members;

    @Override
    public void accept(Class<?> clazz) {
        this.clazz = clazz;
        val scriptManager = KubeJS.getStartupScriptManager();
        this.members = JavaMembers.lookupClass(
            scriptManager.context,
            scriptManager.topLevelScope,
            clazz,
            clazz,
            false
        );
    }

    @Override
    public Stream<? extends ConstructorInfo> constructors() {
        return members.getAccessibleConstructors()
            .stream()
            .map(ConstructorInfo::new);
    }

    @Override
    public Stream<? extends MethodInfo> methods() {
        return members.getAccessibleMethods(KubeJS.getStartupScriptManager().context, false)
            .stream()
            .filter(m -> !m.method.isSynthetic())
            // interface in TS cannot skip declaring methods declared in super
            .filter(m -> noIdenticalParentMethod(m.method, clazz))
            .sorted(Comparator.comparing(m -> m.name))
            .map(info -> new MethodInfo(
                info.method,
                info.name.isEmpty() ? info.method.getName() : info.name,
                getGenericTypeReplacement(clazz, info.method)
            ));
    }

    @Override
    public Stream<? extends FieldInfo> fields() {
        return members.getAccessibleFields(KubeJS.getStartupScriptManager().context, false)
            .stream()
            // those not declared by it will be inherited from super
            .filter(info -> info.field.getDeclaringClass() == this.clazz)
            .map(info -> new FieldInfo(info.field, info.name.isEmpty() ? info.field.getName() : info.name))
            .sorted(Comparator.comparing(f -> f.name));
    }

    public static final Predicate<AnnotatedElement> NO_HIDE_FROM_JS =
        element -> !element.isAnnotationPresent(HideFromJS.class);

    static boolean noIdenticalParentMethod(Method method, Class<?> clazz) {
        if (clazz.isInterface() || method.getDeclaringClass().isInterface()) {
            // interface method is TS cannot be inherited
            return true;
        } else if (method.getDeclaringClass() != clazz) {
            // declared by super, of course there's an identical method in super
            return false;
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
