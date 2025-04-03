package zzzank.probejs.utils.config.struct;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.config.binding.ReadOnlyBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class ConfigCategoryImpl extends ConfigEntryImpl<Map<String, ConfigEntry<?>>> implements ConfigCategory {

    public ConfigCategoryImpl(
        String name,
        Supplier<Map<String, ConfigEntry<?>>> mapStructure,
        ConfigProperties properties,
        ConfigCategory parent
    ) {
        super(name, new CategoryBinding(mapStructure.get(), Cast.to(Map.class), name), properties, parent);
    }

    @Override
    public <T> ConfigEntry<T> register(ConfigEntry<T> entry) {
        Asser.tNotNull(entry, "config entry");
        Asser.t(
            this.getEntry(entry.name()) == null,
            "a config entry with same namespace and name already exists"
        );
        Asser.t(
            entry.parent() == this,
            "config source in config entry not matching config source that accepts this entry"
        );
        val binding = (CategoryBinding) this.binding();
        binding.mutable.put(entry.name(), entry);
        return entry;
    }

    static final class CategoryBinding extends ReadOnlyBinding<Map<String, ConfigEntry<?>>> {
        private final Map<String, ConfigEntry<?>> mutable;

        public CategoryBinding(
            @NotNull Map<String, ConfigEntry<?>> defaultValue,
            @NotNull Class<Map<String, ConfigEntry<?>>> defaultType,
            @NotNull String name
        ) {
            super(Collections.unmodifiableMap(defaultValue), defaultType, name);
            mutable = defaultValue;
        }
    }
}
