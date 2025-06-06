package zzzank.probejs.utils.config.prop;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author ZZZank
 */
public class ConfigProperties {
    public static final int SIZE_THRESHOLD = 5;

    @NotNull
    private Int2ObjectMap<Object> internal;

    public ConfigProperties() {
        internal = new Int2ObjectArrayMap<>();
    }

    public <T> void put(ConfigProperty<T> property, T value) {
        Objects.requireNonNull(property);
        Objects.requireNonNull(value);
        synchronized (this) {
            if (internal.size() == SIZE_THRESHOLD && internal instanceof Int2ObjectArrayMap) {
                internal = new Int2ObjectOpenHashMap<>(internal);
            }
            internal.put(property.index(), value);
        }
    }

    public <T> T merge(ConfigProperty<T> property, T value, BiFunction<? super T, ? super T, ? extends T> merger) {
        synchronized (this) {
            return (T) internal.merge(property.index(), value, (BiFunction) merger);
        }
    }

    public <T> T getOrDefault(ConfigProperty<T> property) {
        synchronized (this) {
            return (T) internal.getOrDefault(property.index(), property.defaultValue());
        }
    }

    public <T> Optional<T> get(ConfigProperty<T> property) {
        return Optional.ofNullable(getOrDefault(property));
    }

    public <T> boolean has(ConfigProperty<T> property) {
        synchronized (this) {
            return internal.containsKey(property.index());
        }
    }

    public <T> T remove(ConfigProperty<T> property) {
        synchronized (this) {
            return (T) internal.remove(property.index());
        }
    }
}
