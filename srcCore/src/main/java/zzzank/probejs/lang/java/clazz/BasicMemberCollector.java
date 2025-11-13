package zzzank.probejs.lang.java.clazz;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.TypeReplacementCollector;
import zzzank.probejs.lang.java.type.impl.VariableType;
import zzzank.probejs.utils.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class BasicMemberCollector implements MemberCollector {
    private final TypeReplacementCollector typeReplacementCollector = new TypeReplacementCollector();

    private Class<?> clazz;
    private Set<String> names;
    private Map<VariableType, TypeDescriptor> typeReplacement;

    @Override
    public void accept(Class<?> clazz) {
        this.clazz = clazz;
        this.names = new HashSet<>();
        this.typeReplacement = typeReplacementCollector.getTypeReplacement(clazz);
    }

    @Override
    public Stream<ConstructorInfo> constructors() {
        return rawConstructors().map(ConstructorInfo::new);
    }

    protected Stream<Constructor<?>> rawConstructors() {
        return Arrays.stream(ReflectUtils.constructorsSafe(clazz));
    }

    @Override
    public Stream<MethodInfo> methods() {
        return rawMethods().map(method -> new MethodInfo(method, RemapperBridge.remapMethod(clazz, method), typeReplacement));
    }

    protected @NotNull Stream<Method> rawMethods() {
        return Arrays.stream(ReflectUtils.methodsSafe(clazz))
            .peek(m -> names.add(RemapperBridge.remapMethod(clazz, m)))
            .filter(m -> !m.isSynthetic())
            .filter(m -> filterInherited(m, clazz));
    }

    @Override
    public Stream<FieldInfo> fields() {
        return rawFields().map(f -> new FieldInfo(f, RemapperBridge.remapField(clazz, f)));
    }

    protected @NotNull Stream<Field> rawFields() {
        return Arrays.stream(ReflectUtils.declaredFieldsSafe(clazz))
            .filter(f -> Modifier.isPublic(f.getModifiers()))
            .filter(f -> !names.contains(RemapperBridge.remapField(clazz, f)));
    }


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
