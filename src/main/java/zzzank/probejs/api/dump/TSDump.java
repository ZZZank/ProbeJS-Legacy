package zzzank.probejs.api.dump;

import org.apache.commons.io.FileUtils;
import zzzank.probejs.api.output.TSFileWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public interface TSDump {
    Path writeTo();

    default void clearFiles() throws IOException {
        if (Files.exists(writeTo()) && Files.isDirectory(writeTo())) {
            FileUtils.deleteDirectory(writeTo().toFile());
        }
    }

    default void ensureFolder() throws IOException {
        if (Files.notExists(writeTo())) {
            Files.createDirectories(writeTo());
        }
    }

    void dump() throws IOException;

    boolean running();

    Stream<TSFileWriter> writers();
}
