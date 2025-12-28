package zzzank.probejs.api.dump;

import lombok.val;
import zzzank.probejs.api.output.TSFileWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class MultiDump extends TSDumpBase {
    private final List<TSDump> dumps = new ArrayList<>();

    public MultiDump(Path base) {
        super(null, base);
    }

    public <T extends TSDump> T addChild(T dump) {
        dumps.add(dump);
        return dump;
    }

    public <T extends TSDump> T addChild(String relativePath, Function<? super Path, T> dump) {
        return addChild(dump.apply(writeTo.resolve(relativePath)));
    }

    public <T extends TSDump> T addChild(Path relativePath, Function<? super Path, T> dump) {
        return addChild(dump.apply(writeTo.resolve(relativePath)));
    }

    public List<TSDump> dumps() {
        return Collections.unmodifiableList(dumps);
    }

    @Override
    public void open() throws IOException {
        for (var dump : dumps) {
            dump.open();
        }
    }

    @Override
    public void dumpImpl() throws IOException {
        for (val tsDump : dumps) {
            tsDump.dump();
        }
    }

    @Override
    public void cleanOldDumps() throws IOException {
        for (var dump : dumps) {
            dump.cleanOldDumps();
        }
    }

    @Override
    public Stream<TSFileWriter> writers() {
        return dumps.stream().flatMap(TSDump::writers);
    }
}
