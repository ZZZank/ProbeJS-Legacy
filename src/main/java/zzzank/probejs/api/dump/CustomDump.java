package zzzank.probejs.api.dump;

import zzzank.probejs.api.output.TSFileWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class CustomDump implements TSDump {
    private final Path path;
    private final IOAction action;
    private volatile boolean running = false;

    public CustomDump(Path path, IOAction action) {
        this.path = path;
        this.action = action;
    }

    @Override
    public Path writeTo() {
        return path;
    }

    @Override
    public void dump() throws IOException {
        running = true;
        try {
            action.run(path);
        } finally {
            running = false;
        }
    }

    @Override
    public boolean running() {
        return running;
    }

    @Override
    public Stream<TSFileWriter> writers() {
        return Stream.empty();
    }

    public interface IOAction {
        void run(Path target) throws IOException;
    }
}
