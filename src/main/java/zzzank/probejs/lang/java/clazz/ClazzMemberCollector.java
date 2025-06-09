package zzzank.probejs.lang.java.clazz;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.val;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.VariableType;
import zzzank.probejs.utils.ReflectUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class ClazzMemberCollector implements MemberCollector {

    private final TypeReplacementCollector typeReplacementCollector = new TypeReplacementCollector();

    private Set<String> names;
    private Class<?> clazz;
    private Map<VariableType, TypeDescriptor> typeReplacement;

    @Override
    public void accept(Class<?> clazz) {
        this.clazz = clazz;
        this.names = new HashSet<>();
        this.typeReplacement = typeReplacementCollector.getTypeReplacement(clazz);
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
            .filter(m -> filterInherited(m, clazz))
            .filter(m -> !m.getName().startsWith("jvmdowngrader$")) // remove JVMDG stub
            .sorted(Comparator.comparing(Method::getName))
            .map(method -> new MethodInfo(clazz, method, typeReplacement));
    }

    @Override
    public Stream<? extends FieldInfo> fields() {
        // those not declared by it will be inherited from super
        return Arrays.stream(ReflectUtils.declaredFieldsSafe(clazz))
            .filter(f -> Modifier.isPublic(f.getModifiers()))
            .filter(NO_HIDE_FROM_JS)
            .filter(f -> !names.contains(RemapperBridge.remapField(clazz, f)))
            .map(f -> new FieldInfo(clazz, f))
            .sorted(Comparator.comparing(f -> f.name));
    }

    public static final Predicate<AnnotatedElement> NO_HIDE_FROM_JS =
        element -> !element.isAnnotationPresent(HideFromJS.class);

    static boolean filterInherited(Method method, Class<?> clazz) {
        if (clazz.isInterface() || method.getDeclaringClass().isInterface()) {
            // interface method is TS cannot be inherited
            return true;
        } else if (method.getDeclaringClass() != clazz) {
            // declared by super, and no override
            return false;
        }
        var parent = clazz.getSuperclass();
        while (parent != null) {
            try {
                val parentMethod = parent.getMethod(method.getName(), method.getParameterTypes());
                // If there is one, return type from "this class" is the same as or the subclass of "super class"
                return !method.getGenericReturnType().equals(parentMethod.getGenericReturnType());
            } catch (NoSuchMethodException ignored) {
            }
            parent = parent.getSuperclass();
        }
        // no such method in super -> unique method, keep it
        return true;
    }
}
