package zzzank.probejs.lang.java.clazz.members;

import zzzank.probejs.lang.java.base.TypeVariableHolder;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.VariableType;
import zzzank.probejs.utils.CollectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class MethodInfo extends TypeVariableHolder {
    public final String name;
    public final List<ParamInfo> params;
    public final TypeDescriptor returnType;
    public final MethodAttributes attributes;

    public MethodInfo(Method method, String name, Map<VariableType, TypeDescriptor> typeRemapper) {
        super(method.getTypeParameters(), method.getAnnotations());
        this.attributes = new MethodAttributes(method);
        this.name = name;
        this.params = CollectUtils.mapToList(method.getParameters(), ParamInfo::new);
        this.returnType = TypeAdapter.getTypeDescription(method.getAnnotatedReturnType()).consolidate(typeRemapper);

        for (var param : this.params) {
            param.type = param.type.consolidate(typeRemapper);
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

        public MethodAttributes(Method method) {
            int modifiers = method.getModifiers();
            this.isStatic = Modifier.isStatic(modifiers);
            this.isDefault = method.isDefault();
            this.isAbstract = Modifier.isAbstract(modifiers);
        }
    }
}
