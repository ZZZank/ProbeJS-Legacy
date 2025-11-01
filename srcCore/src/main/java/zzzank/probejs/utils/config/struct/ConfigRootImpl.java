package zzzank.probejs.utils.config.struct;

import lombok.Getter;
import lombok.experimental.Accessors;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.io.ConfigIO;
import zzzank.probejs.utils.config.prop.ConfigProperties;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
@Getter
@Accessors(fluent = true, makeFinal = true)
public class ConfigRootImpl extends ConfigCategoryImpl implements ConfigRoot {
    private final ConfigIO io;
    private Path filePath;

    public ConfigRootImpl(ConfigIO io, Path path) {
        this(LinkedHashMap::new, new ConfigProperties(), io, path);
    }

    public ConfigRootImpl(
        Supplier<Map<String, ConfigEntry<?>>> provider,
        ConfigProperties properties,
        ConfigIO io,
        Path path
    ) {
        super("", provider, properties, null);
        this.io = Asser.tNotNull(io, "config io");
        this.filePath = path;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }
}
