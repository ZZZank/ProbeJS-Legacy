package zzzank.probejs.api.dump;

import com.google.gson.JsonObject;
import zzzank.probejs.utils.FileUtils;
import zzzank.probejs.utils.JsonUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
public class JSConfigDump extends TSDumpBase {
    private final List<TSDump> typingProviders = new ArrayList<>();
    public final Path scriptFolder;
    public final Predicate<JsonObject> replaceIf;

    public JSConfigDump(Path writeTo, Path scriptFolder, Predicate<JsonObject> replaceIf) {
        super(null, writeTo);
        this.scriptFolder = scriptFolder;
        this.replaceIf = replaceIf;
    }

    public JSConfigDump(Path writeTo, Path scriptFolder) {
        super(null, writeTo);
        this.scriptFolder = scriptFolder;
        this.replaceIf = json -> true;
    }

    public void addTypingProvider(TSDump... dumps) {
        Collections.addAll(typingProviders, dumps);
    }

    public List<TSDump> viewTypingProviders() {
        return Collections.unmodifiableList(typingProviders);
    }

    @Override
    protected void dumpImpl() throws IOException {
        var config = provideJsonConfig();
        FileUtils.writeMergedConfig(writeTo(), config, replaceIf);
    }

    protected JsonObject provideJsonConfig() {
        return JsonUtils.parseObject(
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
    }

    @Override
    public void cleanOldDumps() throws IOException {
    }

    @Override
    public void open() throws IOException {
    }
}
