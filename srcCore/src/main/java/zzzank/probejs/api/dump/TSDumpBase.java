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
            open();
            dumpImpl();
        } catch (IOException io) {
            throw io;
        } catch (Exception e) {
            throw (e instanceof RuntimeException runtime ? runtime : new RuntimeException(e));
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
        return Stream.of(writer);
    }
}
