package zzzank.probejs.utils.config.serde;

import java.lang.reflect.*;

/**
 * @param <I> intermediate object type, used by {@link zzzank.probejs.utils.config.serde.ConfigSerde}
 * @author ZZZank
 */
public interface ConfigSerdeFactory<I> {

    <T> ConfigSerde<I, T> getSerde(Type type);

    static Class<?> asClass(Type type) {
        return switch (type) {
            case null -> null;
            case Class<?> clazz -> clazz;
            case GenericArrayType arrayType ->
                Array.newInstance(asClass(arrayType.getGenericComponentType()), 0).getClass();
            case ParameterizedType parameterized -> asClass(parameterized.getRawType());
            case TypeVariable<?> variable -> asClass(variable.getBounds()[0]);
            case WildcardType wildcard -> {
                var bounds = wildcard.getLowerBounds();
                yield asClass(bounds.length != 0 ? bounds[0] : wildcard.getUpperBounds()[0]);
            }
            default -> throw new IllegalArgumentException(String.format("Unknown type object '%s' with class '%s'", type, type.getClass()));
        };
    }
}
