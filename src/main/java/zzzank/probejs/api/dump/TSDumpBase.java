package zzzank.probejs.api.dump;

import zzzank.probejs.api.output.TSFileWriter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public abstract class TSDumpBase implements TSDump {
    protected final TSFileWriter writer;
    protected final Path writeTo;
    protected final ReporterImpl reporter;

    public TSDumpBase(TSFileWriter writer, Path writeTo) {
        this.writer = writer;
        this.writeTo = writeTo;
        reporter = new ReporterImpl(this.writer);
    }

    protected abstract void dumpImpl() throws IOException;

    @Override
    public Path writeTo() {
        return writeTo;
    }

    @Override
    public void dump() throws IOException {
        reporter.running = true;
        try {
            ensureFolder();
            dumpImpl();
        } catch (IOException io) {
            throw io;
        } catch (Exception e) {
            throw (e instanceof RuntimeException runtime ? runtime : new RuntimeException(e));
        } finally {
            reporter.running = false;
        }
    }

    @Override
    public ReporterImpl reporter() {
        return reporter;
    }

    public static final class ReporterImpl implements Reporter {
        private final TSFileWriter writer;
        private boolean running = false;

        public ReporterImpl(TSFileWriter writer) {
            this.writer = writer;
        }

        @Override
        public boolean running() {
            return running;
        }

        @Override
        public int countTotal() {
            return writer.countAcceptedFiles();
        }

        @Override
        public int countWritten() {
            return writer.countWrittenFiles();
        }
    }
}
