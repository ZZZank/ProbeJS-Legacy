package zzzank.probejs.api.dump;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class CustomDump implements TSDump {
    private final DummyReporter reporter = new DummyReporter();

    private final Path path;
    private final IOAction action;

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
        reporter.running = true;
        try {
            action.run(path);
        } finally {
            reporter.running = false;
        }
    }

    @Override
    public Reporter reporter() {
        return reporter;
    }

    public interface IOAction {
        void run(Path target) throws IOException;
    }

    private static final class DummyReporter implements Reporter {
        private boolean running = false;

        @Override
        public boolean running() {
            return running;
        }

        @Override
        public int countTotal() {
            return 1;
        }

        @Override
        public int countWritten() {
            return running ? 0 : 1;
        }
    }
}
