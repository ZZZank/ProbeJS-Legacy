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
        this.<T>castBackend().put(property, value);
    }

    public <T> T merge(ConfigProperty<T> property, T value, BiFunction<? super T, ? super T, ? extends T> merger) {
        return this.<T>castBackend().merge(property, value, merger);
    }

    public <T> T getOrDefault(ConfigProperty<T> property) {
        return this.<T>castBackend().getOrDefault(property, property.defaultValue());
    }

    public <T> Optional<T> get(ConfigProperty<T> property) {
        return Optional.ofNullable(getOrDefault(property));
    }

    public <T> boolean has(ConfigProperty<T> property) {
        return this.<T>castBackend().containsKey(property);
    }

    public <T> T remove(ConfigProperty<T> property) {
        return this.<T>castBackend().remove(property);
    }

    @SuppressWarnings("unchecked")
    private <T> Map<ConfigProperty<T>, T> castBackend() {
        return (Map<ConfigProperty<T>, T>) (Map<?, ?>) backend;
    }
}
