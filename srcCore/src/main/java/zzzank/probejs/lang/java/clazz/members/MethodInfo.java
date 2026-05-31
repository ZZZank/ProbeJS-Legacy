package zzzank.probejs.lang.java.clazz.members;

import zzzank.probejs.lang.java.base.TypeVariableHolder;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.VariableType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MethodInfo extends TypeVariableHolder {
    /// first compare by [MethodAttributes#isStatic], non-static < static
    ///
    /// then compare by [MethodInfo#name]
    public static Comparator<? super MethodInfo> commonComparator() {
        return (a, b) -> {
            var result = Boolean.compare(a.attributes.isStatic, b.attributes.isStatic);
            if (result != 0) {
                // prefer static over non-static
                return -result;
            }
            return a.name.compareTo(b.name);
        };
    }

    public final String name;
    public final List<ParamInfo> params;
    public final TypeDescriptor returnType;
    public final MethodAttributes attributes;

    public MethodInfo(Method method, String name, Map<VariableType, TypeDescriptor> typeRemapper) {
        super(method.getTypeParameters(), method.getAnnotations());
        this.attributes = new MethodAttributes(method);
        this.name = name;
        this.returnType = TypeAdapter.getTypeDescription(method.getAnnotatedReturnType()).consolidate(typeRemapper);

        var parameters = method.getParameters();
        this.params = new ArrayList<>();
        for (var i = 0; i < parameters.length; i++) {
            var paramInfo = new ParamInfo(parameters[i], i);
            paramInfo.type = paramInfo.type.consolidate(typeRemapper);
            this.params.set(i, paramInfo);
        }
    }

    @Override
    public String toString() {
        return String.format("MethodInfo(%s%s: %s)", name, params, returnType);
    }

    public static class MethodAttributes {
        public final boolean isStatic;
        /**
         * When this appears in a class, remember to translate its type variables because it is from an interface.
         */
        public final boolean isDefault;
        public final boolean isAbstract;
        public final Class<?> declaringClass;

        public MethodAttributes(Method method) {
            int modifiers = method.getModifiers();
            this.isStatic = Modifier.isStatic(modifiers);
            this.isDefault = method.isDefault();
            this.isAbstract = Modifier.isAbstract(modifiers);
            this.declaringClass = method.getDeclaringClass();
        }
    }
}
