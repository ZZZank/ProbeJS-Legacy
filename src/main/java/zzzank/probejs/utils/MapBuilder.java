package zzzank.probejs.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author ZZZank
 */
@SuppressWarnings("unused")
public final class MapBuilder<K, V> {
    public static <K, V> MapBuilder<K, V> of(Map<K, V> toWrap) {
        return new MapBuilder<>(Asser.tNotNull(toWrap, "map to wrap into MapBuilder"));
    }

    public static <K, V> MapBuilder<K, V> ofHash(int initialCapacity) {
        return new MapBuilder<>(new HashMap<>(initialCapacity));
    }

    public static <K, V> MapBuilder<K, V> ofHash() {
        return new MapBuilder<>(new HashMap<>());
    }

    public static <K, V> MapBuilder<K, V> ofHash(Class<K> keyType, Class<V> valueType) {
        return new MapBuilder<>(new HashMap<>());
    }

    public Map<K, V> build() {
        return internal;
    }

    private final Map<K, V> internal;

    private MapBuilder(Map<K, V> internal) {
        this.internal = internal;
    }

    public MapBuilder<K, V> put(K key, V value) {
        internal.put(key, value);
        return this;
    }

    public MapBuilder<K, V> putIfAbsent(K key, V value) {
        internal.putIfAbsent(key, value);
        return this;
    }

    public MapBuilder<K, V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remapper) {
        internal.compute(key, remapper);
        return this;
    }

    public MapBuilder<K, V> computeIfAbsent(K key, Function<? super K, ? extends V> mapper) {
        internal.computeIfAbsent(key, mapper);
        return this;
    }

    public MapBuilder<K, V> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remapper) {
        internal.computeIfPresent(key, remapper);
        return this;
    }
}