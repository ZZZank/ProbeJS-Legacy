package zzzank.probejs.utils.config.struct;

import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.config.io.ConfigIO;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public interface ConfigRoot extends ConfigCategory {

    ConfigIO io();

    Path filePath();

    default void save() {
        try {
            io().save(this, filePath());
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error happened when writing configs to file", e);
        }
    }

    default void read() {
        if (!Files.exists(filePath())) {
            return;
        }
        try {
            io().read(this, filePath());
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error happened when reading configs from file", e);
        }
    }

    @Override
    default boolean isRoot() {
        return true;
    }

    @Override
    default ConfigRoot getRoot() {
        return this;
    }

    @Override
    default ConfigCategory parent() {
        return null;
    }

    @Override
    default String path() {
        return "";
    }
}
