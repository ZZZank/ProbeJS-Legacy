package zzzank.probejs.utils.config.struct;

import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.io.ConfigIO;
import zzzank.probejs.utils.config.prop.ConfigProperties;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class ConfigRootImpl extends ConfigCategoryImpl implements ConfigRoot {
    private final ConfigIO io;
    private final Path path;

    public ConfigRootImpl(
        Supplier<Map<String, ConfigEntry<?>>> provider,
        ConfigProperties properties,
        ConfigIO io,
        Path path
    ) {
        super("", provider, properties, null);
        this.io = Asser.tNotNull(io, "config io");
        this.path = path;
    }

    @Override
    public ConfigIO io() {
        return io;
    }

    @Override
    public Path filePath() {
        return path;
    }
}
