package zzzank.probejs.utils.config.prop;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import zzzank.probejs.utils.Asser;

import java.util.*;

/**
 * @author ZZZank
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
public final class ConfigProperty<T> {

    private static final Map<String, ConfigProperty<?>> NAMED = new HashMap<>();
    private static final List<ConfigProperty<?>> INDEXED = new ArrayList<>();

    public static final ConfigProperty<List<String>> COMMENTS = register("comments", Collections.emptyList());
    public static final ConfigProperty<Collection<String>> ENUMS = register("enums", Collections.emptyList());
    public static final ConfigProperty<String> EXAMPLE = register("example", null);

    public static synchronized <T> ConfigProperty<T> register(String name, T defaultValue) {
        Asser.tNotNull(name, "name");
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

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public String toString() {
        return "ConfigProperty{" + name + "}";
    }
}
