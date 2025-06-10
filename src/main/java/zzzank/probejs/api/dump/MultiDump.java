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
public class MultiDump implements TSDump {
    private final Path base;
    private final List<TSDump> dumps = new ArrayList<>();

    public MultiDump(Path base) {
        this.base = base;
    }

    public <T extends TSDump> T addChild(T dump) {
        dumps.add(dump);
        return dump;
    }

    public <T extends TSDump> T addChild(String relativePath, Function<Path, T> dump) {
        return addChild(dump.apply(base.resolve(relativePath)));
    }

    public List<TSDump> dumps() {
        return Collections.unmodifiableList(dumps);
    }

    @Override
    public Path writeTo() {
        return base;
    }

    @Override
    public void dump() throws IOException {
        for (val tsDump : dumps) {
            tsDump.dump();
        }
    }

    @Override
    public boolean running() {
        return dumps.stream().anyMatch(TSDump::running);
    }

    @Override
    public Stream<TSFileWriter> writers() {
        return dumps.stream().flatMap(TSDump::writers);
    }
}
