package zzzank.probejs.utils.collect;

import zzzank.probejs.utils.Asser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author ZZZank
 */
@SuppressWarnings("unused")
public final class WrappedMap<K, V> {
    public static <K, V> WrappedMap<K, V> of(Map<K, V> toWrap) {
        return new WrappedMap<>(Asser.tNotNull(toWrap, "map to wrap into MapBuilder"));
    }

    public static <K, V> WrappedMap<K, V> ofHash(int initialCapacity) {
        return new WrappedMap<>(new HashMap<>(initialCapacity));
    }

    public static <K, V> WrappedMap<K, V> ofHash() {
        return new WrappedMap<>(new HashMap<>());
    }

    public static <K, V> WrappedMap<K, V> ofHash(Class<K> keyType, Class<V> valueType) {
        return new WrappedMap<>(new HashMap<>());
    }

    public Map<K, V> build() {
        return internal;
    }

    private final Map<K, V> internal;

    private WrappedMap(Map<K, V> internal) {
        this.internal = internal;
    }

    public WrappedMap<K, V> put(K key, V value) {
        internal.put(key, value);
        return this;
    }

    public WrappedMap<K, V> putIfAbsent(K key, V value) {
        internal.putIfAbsent(key, value);
        return this;
    }

    public WrappedMap<K, V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remapper) {
        internal.compute(key, remapper);
        return this;
    }

    public WrappedMap<K, V> computeIfAbsent(K key, Function<? super K, ? extends V> mapper) {
        internal.computeIfAbsent(key, mapper);
        return this;
    }

    public WrappedMap<K, V> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remapper) {
        internal.computeIfPresent(key, remapper);
        return this;
    }
}