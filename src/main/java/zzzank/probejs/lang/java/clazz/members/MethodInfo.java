package zzzank.probejs.lang.java.clazz.members;

import dev.latvian.mods.rhino.Context;
import zzzank.probejs.features.rhizo.RemapperBridge;
import zzzank.probejs.lang.java.base.TypeVariableHolder;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodInfo extends TypeVariableHolder {
    public final String name;
    public final List<ParamInfo> params;
    public TypeDescriptor returnType;
    public final MethodAttributes attributes;

    public MethodInfo(Class<?> from, Method method, Map<TypeVariable<?>, Type> typeRemapper) {
        super(method.getTypeParameters(), method.getAnnotations());
        this.attributes = new MethodAttributes(method);
        this.name = RemapperBridge.remapMethod(from, method);

        Parameter[] parameters = method.getParameters();
        this.params = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (i == 0 && Context.class.isAssignableFrom(parameter.getType())) continue;
            this.params.add(new ParamInfo(parameter));
        }

        this.returnType = TypeAdapter.getTypeDescription(method.getAnnotatedReturnType());

        for (Map.Entry<TypeVariable<?>, Type> entry : typeRemapper.entrySet()) {
            TypeVariable<?> symbol = entry.getKey();
            TypeDescriptor replacement = TypeAdapter.getTypeDescription(entry.getValue());

            for (ParamInfo param : this.params) {
                param.type = TypeAdapter.consolidateType(param.type, symbol.getName(), replacement);
            }
            this.returnType = TypeAdapter.consolidateType(this.returnType, symbol.getName(), replacement);
        }
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
