package zzzank.probejs.utils.config.struct;

import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.config.io.ConfigIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public interface ConfigRoot extends ConfigCategory {

    ConfigIO io();

    Path filePath();

    default boolean inMemoryOnly() {
        return filePath() == null;
    }

    default void save() throws IOException {
        if (inMemoryOnly()) {
            return;
        }
        io().save(this, filePath());
    }

    default void read() throws IOException {
        if (inMemoryOnly() || !Files.exists(filePath())) {
            return;
        }
        io().read(this, filePath());
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
    default String name() {
        return "";
    }

    @Override
    default String path() {
        return "";
    }
}
