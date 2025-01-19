package zzzank.probejs.utils.config.io;

import lombok.val;
import zzzank.probejs.utils.config.ConfigImpl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public interface ConfigIO {

    void read(ConfigImpl config, Reader reader) throws IOException;

    void save(ConfigImpl config, Writer writer) throws IOException;

    default void read(ConfigImpl config, Path path) throws IOException {
        try (val reader = Files.newBufferedReader(path)) {
            read(config, reader);
        }
    }

    default void save(ConfigImpl config, Path path) throws IOException {
        try (val writer = Files.newBufferedWriter(path)) {
            save(config, writer);
        }
    }
}
