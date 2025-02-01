package zzzank.probejs.utils.config.prop;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.*;

/**
 * @author ZZZank
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigProperty<T> {

    private static final Map<String, ConfigProperty<?>> NAMED = new HashMap<>();
    private static final List<ConfigProperty<?>> INDEXED = new ArrayList<>();

    public static final ConfigProperty<List<String>> COMMENTS = register("comments", Collections.emptyList());
    public static final ConfigProperty<Collection<String>> ENUMS = register("enums", Collections.emptyList());
    public static final ConfigProperty<String> EXAMPLE = register("example", "");

    public static synchronized <T> ConfigProperty<T> register(String name, T defaultValue) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(defaultValue);
        if (NAMED.containsKey(name)) {
            throw new IllegalArgumentException("config property with name '" + name + "' already registered");
        }
        val prop = new ConfigProperty<>(name, INDEXED.size(), defaultValue);
        NAMED.put(name, prop);
        INDEXED.add(prop);
        return prop;
    }

    private final String name;
    private final int index;
    private final T defaultValue;
}
