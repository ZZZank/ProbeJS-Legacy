package zzzank.probejs.lang.java.clazz;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.rhino.JavaMembers;
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

    private Class<?> clazz;
    private JavaMembers members;
    private Map<VariableType, TypeDescriptor> typeReplacement;

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
        this.typeReplacement = typeReplacementCollector.getTypeReplacement(clazz);
    }

    @Override
    public Stream<ConstructorInfo> constructors() {
        return members.getAccessibleConstructors()
            .stream()
            .map(ConstructorInfo::new);
    }

    @Override
    public Stream<MethodInfo> methods() {
        return members.getAccessibleMethods(KubeJS.getStartupScriptManager().context, false)
            .stream()
            .filter(m -> !m.method.isSynthetic())
            .filter(m -> filterInherited(m.method, clazz))
            .sorted(Comparator.comparing(m -> m.name))
            .map(info -> new MethodInfo(info.method, info.name, typeReplacement));
    }

    @Override
    public Stream<FieldInfo> fields() {
        return members.getAccessibleFields(KubeJS.getStartupScriptManager().context, false)
            .stream()
            // those not declared by it will be inherited from super
            .filter(info -> info.field.getDeclaringClass() == this.clazz)
            .map(info -> new FieldInfo(info.field, info.name))
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
