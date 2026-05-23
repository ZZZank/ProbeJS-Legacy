package zzzank.probejs.api.dump;

import com.google.gson.JsonObject;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.FileUtils;
import zzzank.probejs.utils.JsonUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
                "compilerOptions", CollectUtils.ofLinkedMap(
                    Map.entry("allowJs", true),
                    Map.entry("checkJs", false),
                    Map.entry("lib", List.of("ES2015")),
                    Map.entry("module", "commonjs"),
                    Map.entry("rootDir", "."),
                    Map.entry("skipDefaultLibCheck", true),
                    Map.entry("skipLibCheck", true),
                    Map.entry("target", "ES2015"),
                    Map.entry("types", List.of()),
                    Map.entry("paths", Map.of(
                        "java:*", typingProviders.stream()
                            .map(TSDump::writeTo)
                            .map(p -> FileUtils.relativePathStr(scriptFolder, p) + "/*")
                            .toList()
                    ))
                ),
                "include", Stream.concat(
                    Stream.of("./**/*.js"),
                    typingProviders.stream()
                        .map(TSDump::writeTo)
                        .map(p -> FileUtils.relativePathStr(scriptFolder, p) + "/**/*.d.ts"))
                    .toList()
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
