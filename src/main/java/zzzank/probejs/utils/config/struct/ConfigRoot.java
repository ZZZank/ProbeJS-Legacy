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

    default void save(Path saveTo) {
        try {
            io().save(this, saveTo);
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error happened when writing configs to file", e);
        }
    }

    default void read(Path readFrom) {
        if (!Files.exists(readFrom)) {
            return;
        }
        try {
            io().read(this, readFrom);
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error happened when reading configs from file", e);
        }
    }

    @Override
    default boolean isRoot() {
        return true;
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
