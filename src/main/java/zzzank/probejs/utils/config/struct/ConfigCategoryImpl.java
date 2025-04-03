package zzzank.probejs.utils.config.struct;

import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.config.binding.ReadOnlyBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;

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
        super(name, new ReadOnlyBinding<>(mapStructure.get(), Cast.to(Map.class), name), properties, parent);
    }
}
