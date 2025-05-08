package zzzank.probejs.api.dump;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    Reporter reporter();

    interface Reporter {
        boolean running();

        int countTotal();

        int countWritten();
    }
}
