package zzzank.probejs.api.dump;

import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.utils.DeleteDirectoryFileVisitor;

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
        var writeTo = writeTo();
        if (Files.exists(writeTo) && Files.isDirectory(writeTo)) {
            Files.walkFileTree(writeTo, new DeleteDirectoryFileVisitor());
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
