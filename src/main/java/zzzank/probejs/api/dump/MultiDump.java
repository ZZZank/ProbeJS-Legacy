package zzzank.probejs.api.dump;

import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public class MultiDump implements TSDump {
    private final Path base;
    private final List<TSDump> dumps = new ArrayList<>();
    private Reporter reporter = null;

    public MultiDump(Path base) {
        this.base = base;
    }

    public <T extends TSDump> T addChild(T dump) {
        dumps.add(dump);
        reporter = null;
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
    public Reporter reporter() {
        if (reporter == null) {
            reporter = new ChainedReporter(
                this.dumps.stream()
                    .map(TSDump::reporter)
                    .toArray(Reporter[]::new)
            );
        }
        return null;
    }

    public static final class ChainedReporter implements Reporter {
        private final Reporter[] reporters;

        public ChainedReporter(Reporter[] reporters) {
            this.reporters = reporters;
        }

        @Override
        public boolean running() {
            return Arrays.stream(reporters).anyMatch(Reporter::running);
        }

        @Override
        public int countTotal() {
            return Arrays.stream(reporters).mapToInt(Reporter::countTotal).sum();
        }

        @Override
        public int countWritten() {
            return Arrays.stream(reporters).mapToInt(Reporter::countWritten).sum();
        }
    }
}
