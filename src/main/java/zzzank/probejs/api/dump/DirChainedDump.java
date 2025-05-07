package zzzank.probejs.api.dump;

import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author ZZZank
 */
public class DirChainedDump implements TSDump {
    private final Path base;
    private final Map<String, TSDump> children = new LinkedHashMap<>();
    private Reporter reporter = null;

    public DirChainedDump(Path base) {
        this.base = base;
    }

    public void addChild(String path, TSDump dump) {
        children.put(path, dump);
        reporter = null;
    }

    @Override
    public Path writeTo() {
        return base;
    }

    @Override
    public void dump() throws IOException {
        for (val tsDump : children.values()) {
            tsDump.dump();
        }
    }

    @Override
    public Reporter reporter() {
        if (reporter == null) {
            reporter = new ChainedReporter(
                this.children.values()
                    .stream()
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
