package zzzank.probejs.utils.config.struct;

import lombok.Getter;
import lombok.experimental.Accessors;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.binding.ConfigBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
@Getter
@Accessors(fluent = true, makeFinal = true)
class ConfigEntryImpl<T> implements ConfigEntry<T> {
    private final String name;
    private final ConfigBinding<T> binding;
    private final ConfigProperties properties;
    private final ConfigCategory parent;

    public ConfigEntryImpl(String name, ConfigBinding<T> binding, ConfigProperties properties, ConfigCategory parent) {
        this.name = Asser.tNotNull(name, "name");
        this.binding = Asser.tNotNull(binding, "binding");
        this.properties = Asser.tNotNull(properties, "properties");
        this.parent = parent;

        if (!this.isRoot()) {
            Asser.t(!name.isEmpty(), "non-root config entry should have not-empty name");
            Asser.tNotNull(parent, "parent for non-root config entry");
        }
        Asser.t(
            name.indexOf(ConfigCategory.PATH_SPLITTER_CHAR) < 0,
            String.format(
                "There should not be path splitter (%s) in config entry name",
                ConfigCategory.PATH_SPLITTER_CHAR
            )
        );
    }

    @Override
    public Stream<ConfigEntry<?>> streamEntries() {
        return Stream.of(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConfigEntry<?> other
            && Objects.equals(parent, other.parent())
            && this.name.equals(other.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, name);
    }
}
