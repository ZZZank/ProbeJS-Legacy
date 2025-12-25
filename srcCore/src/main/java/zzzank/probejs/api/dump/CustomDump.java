package zzzank.probejs.api.dump;

import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.utils.Asser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class CustomDump implements TSDump {
    private final Path path;
    private final DumpAction openAction;
    private final DumpAction dumpAction;
    private final DumpAction cleanAction;
    private volatile boolean running = false;

    public CustomDump(Path path, DumpAction openAction, DumpAction dumpAction, DumpAction cleanAction) {
        this.path = Asser.tNotNull(path, "path");
        this.openAction = Asser.tNotNull(openAction, "openAction");
        this.dumpAction = Asser.tNotNull(dumpAction, "dumpAction");
        this.cleanAction = Asser.tNotNull(cleanAction, "cleanAction");
    }

    public CustomDump(Path path, DumpAction dumpAction) {
        this(path, DumpAction.NO_OP, dumpAction, DumpAction.NO_OP);
    }

    @Override
    public Path writeTo() {
        return path;
    }

    @Override
    public void open() throws IOException {
        openAction.run(this);
    }

    @Override
    public void dump() throws IOException {
        running = true;
        try {
            dumpAction.run(this);
        } finally {
            running = false;
        }
    }

    @Override
    public void cleanOldDumps() throws IOException {
        this.cleanAction.run(this);
    }

    @Override
    public boolean running() {
        return running;
    }

    @Override
    public Stream<TSFileWriter> writers() {
        return Stream.empty();
    }

    public interface DumpAction {
        DumpAction NO_OP = d -> {};

        void run(TSDump dump) throws IOException;
    }
}
