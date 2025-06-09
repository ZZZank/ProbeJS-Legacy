package zzzank.probejs.lang.java.clazz;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZZZank
 */
public final class TypeReplacementCollector {
    private final Map<Class<?>, Map<TypeVariable<?>, Type>> replacementByClass = new HashMap<>();

    public Type replaceType(TypeVariable<?> original, Map<TypeVariable<?>, Type> mapping) {
        return mapping.getOrDefault(original, original);
    }

    public Type replaceType(Type original, Map<TypeVariable<?>, Type> mapping) {
        if (original instanceof TypeVariable<?> variable) {
            return replaceType(variable, mapping);
        }
        // ignore other types, not a good idea though, because of complex types like T[]
        return original;
    }

    public Map<TypeVariable<?>, Type> transformMapping(
        Map<TypeVariable<?>, Type> mapping,
        Map<TypeVariable<?>, Type> transformer
    ) {
        if (mapping.isEmpty()) {
            return Map.of();
        } else if (mapping.size() == 1) {
            val entry = mapping.entrySet().iterator().next();
            return Map.of(entry.getKey(), replaceType(entry.getValue(), transformer));
        }
        val transformed = new HashMap<>(mapping);
        for (var entry : transformed.entrySet()) {
            entry.setValue(replaceType(entry.getValue(), transformer));
        }
        return transformed;
    }

    @NotNull
    public Map<TypeVariable<?>, Type> getTypeReplacement(Class<?> type) {
        if (type == null || type == Object.class || type.isPrimitive()) {
            return Map.of();
        }
        return replacementByClass.computeIfAbsent(type, this::computeTypeReplacement);
    }

    private Map<TypeVariable<?>, Type> computeTypeReplacement(Class<?> type) {
        var mapping = new HashMap<TypeVariable<?>, Type>();

        /*
         * (classes are named as 'XXX': A, B, C, ...)
         * (type variables are named as 'Tx': Ta, Tb, Tc, ...)
         *
         * let's consider a rather extreme case:
         *
         * class A<Ta> {}
         * interface B<Tb> {}
         * class C<Tc> extends A<Tc> {}
         * class D<Td> extends C<Td> implements B<A<Td>> {}
         *
         * assuming that input 'type' is D.class
         */

        // in our D.class example, this will collect mapping from C<Td>, forming Tc -> Td
        extractSuperMapping(type.getGenericSuperclass(), mapping);

        // in our D.class example, this will collect mapping from B<A<Td>>, forming Tb -> A<Td>
        for (var genericInterface : type.getGenericInterfaces()) {
            extractSuperMapping(genericInterface, mapping);
        }

        // mapping from super
        // in our D.class example, super mapping will only include Ta -> Tc
        var superMapping = getTypeReplacement(type.getSuperclass());

        var interfaces = type.getInterfaces();
        var interfaceMappings = new ArrayList<Map<TypeVariable<?>, Type>>(interfaces.length);
        for (var interface_ : interfaces) {
            interfaceMappings.add(getTypeReplacement(interface_));
        }

        if (superMapping.isEmpty() && interfaceMappings.stream().allMatch(Map::isEmpty)) {
            return Map.copyOf(mapping);
        }

        // transform super mapping to make it able to directly map a type to types used by D.class
        // then merge them together
        var merged = new HashMap<>(transformMapping(superMapping, mapping));
        for (var interfaceMapping : interfaceMappings) {
            merged.putAll(transformMapping(interfaceMapping, mapping));
        }
        merged.putAll(mapping);

        // in our D.class example, our mapping will include Ta -> Td, Tb -> A<Td>, Tc -> Td.
        // This means that all related type (Ta, Tb, Tc) can be directly mapped to
        // the type used by D.class (Td), so we only need to apply the mapping ONCE, which will be
        // important for performance
        return Map.copyOf(merged);
    }

    private static void extractSuperMapping(Type superType, HashMap<TypeVariable<?>, Type> pushTo) {
        if (superType instanceof ParameterizedType parameterized
            && parameterized.getRawType() instanceof Class<?> parent) {

            final var params = parent.getTypeParameters(); // T
            final var args = parameterized.getActualTypeArguments(); // T is mapped to

            if (params.length != args.length) {
                throw new IllegalArgumentException(String.format(
                    "typeParameters.length != actualTypeArguments.length (%s != %s)",
                    params.length,
                    args.length
                ));
            }

            for (int i = 0; i < args.length; i++) {
                pushTo.put(params[i], args[i]);
            }
        }
    }
}
