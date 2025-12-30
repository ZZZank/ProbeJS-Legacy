package zzzank.probejs.dump;

import zzzank.probejs.api.dump.TSDump;
import zzzank.probejs.api.dump.TSDumpBase;
import zzzank.probejs.utils.FileUtils;
import zzzank.probejs.utils.JsonUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author ZZZank
 */
public class JSConfigDump extends TSDumpBase {
    private final List<TSDump> typingProviders = new ArrayList<>();
    public final Path scriptFolder;

    public JSConfigDump(Path writeTo, Path scriptFolder) {
        super(null, writeTo);
        this.scriptFolder = scriptFolder;
    }

    public void addTypingProvider(TSDump... dumps) {
        Collections.addAll(typingProviders, dumps);
    }

    public List<TSDump> viewTypingProviders() {
        return Collections.unmodifiableList(typingProviders);
    }

    @Override
    protected void dumpImpl() throws IOException {
        var config = JsonUtils.parseObject(
            Map.of(
                "compilerOptions", Map.ofEntries(
                    Map.entry("module", "commonjs"),
                    Map.entry("moduleResolution", "classic"),
                    Map.entry("isolatedModules", true),
                    Map.entry("composite", true),
                    Map.entry("incremental", true),
                    Map.entry("allowJs", true),
                    Map.entry("target", "ES2015"),
                    Map.entry("lib", List.of("ES5", "ES2015")),
                    Map.entry("rootDir", "."),
                    Map.entry("types", typingProviders.stream()
                        .map(TSDump::writeTo)
                        .map(p -> FileUtils.relativePathStr(scriptFolder, p))
                        .toList()
                    ),
                    Map.entry("skipLibCheck", true),
                    Map.entry("skipDefaultLibCheck", true)
                ),
                "include", List.of("./**/*.js")
            )
        );
        FileUtils.writeMergedConfig(writeTo(), config, j -> true);
    }

    @Override
    public void cleanOldDumps() throws IOException {
    }

    @Override
    public void open() throws IOException {
    }
}
