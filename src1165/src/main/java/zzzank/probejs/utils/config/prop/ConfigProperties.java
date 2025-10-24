package zzzank.probejs.utils.config.prop;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @author ZZZank
 */
public class ConfigProperties {

    private final Map<ConfigProperty<?>, Object> backend;

    public ConfigProperties() {
        this(new ConcurrentHashMap<>());
    }

    public ConfigProperties(Map<ConfigProperty<?>, Object> backend) {
        this.backend = Objects.requireNonNull(backend);
    }

    public <T> void put(ConfigProperty<T> property, T value) {
        Objects.requireNonNull(property);
        Objects.requireNonNull(value);
        backend.put(property, value);
    }

    public <T> T merge(ConfigProperty<T> property, T value, BiFunction<? super T, ? super T, ? extends T> merger) {
        return (T) backend.merge(property, value, (BiFunction) merger);
    }

    public <T> T getOrDefault(ConfigProperty<T> property) {
        return (T) backend.getOrDefault(property, property.defaultValue());
    }

    public <T> Optional<T> get(ConfigProperty<T> property) {
        return Optional.ofNullable(getOrDefault(property));
    }

    public <T> boolean has(ConfigProperty<T> property) {
        return backend.containsKey(property);
    }

    public <T> T remove(ConfigProperty<T> property) {
        var removed = backend.remove(property);
        return (T) removed;
    }
}
