package zzzank.probejs.utils.config.struct;

import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.io.ConfigIO;
import zzzank.probejs.utils.config.prop.ConfigProperties;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class ConfigRootImpl extends ConfigCategoryImpl implements ConfigRoot {
    private final ConfigIO io;

    public ConfigRootImpl(
        String name,
        Supplier<Map<String, ConfigEntry<?>>> provider,
        ConfigProperties properties,
        ConfigIO io
    ) {
        super(name, provider, properties, null);
        this.io = Asser.tNotNull(io, "config io");
    }

    @Override
    public ConfigIO io() {
        return io;
    }
}
