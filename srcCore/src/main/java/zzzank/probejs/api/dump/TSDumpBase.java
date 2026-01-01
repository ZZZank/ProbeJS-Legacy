package zzzank.probejs.api.dump;

import zzzank.probejs.api.output.TSFileWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public abstract class TSDumpBase implements TSDump {
    protected final TSFileWriter writer;
    protected final Path writeTo;
    private volatile boolean running;

    public TSDumpBase(TSFileWriter writer, Path writeTo) {
        this.writer = writer;
        this.writeTo = writeTo;
    }

    protected abstract void dumpImpl() throws IOException;

    @Override
    public Path writeTo() {
        return writeTo;
    }

    @Override
    public void dump() throws IOException {
        running = true;
        try {
            dumpImpl();
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
        var writer = this.writer;
        return writer == null ? Stream.empty() : Stream.of(writer);
    }
}
