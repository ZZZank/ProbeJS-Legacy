package zzzank.probejs.utils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ZZZank
 */
@FunctionalInterface
public interface UnsafeFunction<I, O, E extends Throwable> {

    O apply(I i) throws E;

    default Map.Entry<O, Throwable> applySafe(I i) {
        try {
            return new AbstractMap.SimpleImmutableEntry<>(apply(i), null);
        } catch (Throwable ex) {
            return new AbstractMap.SimpleImmutableEntry<>(null, ex);
        }
    }

    default <O2> UnsafeFunction<I, O2, E> andThen(UnsafeFunction<? super O, O2, E> then) {
        Objects.requireNonNull(then);
        return (i) -> then.apply(apply(i));
    }
}
