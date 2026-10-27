package zzzank.probejs;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.features.forge_scan.ForgeScanSupport;
import zzzank.probejs.features.kubejs.EventJSInfos;
import zzzank.probejs.features.minecraft.DumpMessageList;
import zzzank.probejs.features.parchment.ParchmentMappingLoader;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClazzMemberCollector;
import zzzank.probejs.lang.schema.SchemaDump;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.typescript.ProbeNamedDump;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.SharedDump;
import zzzank.probejs.plugin.ProbeJSPlugins;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.ProbeText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProbeDump {
    public static final Path SNIPPET_PATH = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.code-snippets");
    public static final Path CLASS_CACHE = ProbePaths.PROBE.resolve("classes.txt");
    public static final Path EVENT_CACHE = ProbePaths.PROBE.resolve("kube_event.json");

    final ClassRegistry classRegistry = new ClassRegistry(new ClazzMemberCollector());
    final SharedDump sharedDump = new SharedDump(ProbePaths.PROBE.resolve("shared"), classRegistry, ProbeJSPlugins.buildTranspiler());
    final SchemaDump schemaDump = new SchemaDump();
    final SnippetDump snippetDump = new SnippetDump();
    final Collection<ScriptDump> scriptDumps = new ArrayList<>();
    private final boolean cleanOldDumps;

    public ProbeDump(boolean cleanOldDumps) {
        this.cleanOldDumps = cleanOldDumps;
    }

    public void addScript(@NotNull ScriptDump dump) {
        scriptDumps.add(Objects.requireNonNull(dump));
    }

    public void trigger(Function<ProbeText, DumpMessageList.MessageEntry> addEntry) throws Exception {
        var namedDumps = Stream.<ProbeNamedDump>concat(Stream.of(sharedDump), scriptDumps.stream()).toList();

        if (ParchmentMappingLoader.ENABLED) {
            var parchmentEntry = addEntry.apply(ProbeText.literal("PARCHMENT"));

            Path path;

            parchmentEntry.provideBody(ProbeText.literal("Downloading parchment mapping"));
            try {
                path = ParchmentMappingLoader.downloadMapping("1.16.5", "2022.03.06", ProbePaths.EXTERNAL);
                parchmentEntry.provideBody(ProbeText.literal("Parchment mapping downloaded"));
            } catch (IOException e) {
                parchmentEntry.provideBody(ProbeText.literal("Error when downloading mapping: " + e));
                throw e;
            }

            parchmentEntry.provideBody(ProbeText.literal("Loading parchment mapping"));
            try {
                ParchmentMappingLoader.getOrInitTransformer(path);
                parchmentEntry.provideBody(ProbeText.literal("Parchment mapping loaded"));
            } catch (IOException e) {
                parchmentEntry.provideBody(ProbeText.literal("Error when loading mapping: " + e));
                throw e;
            }
        }

        var workSpaceEntry = addEntry.apply(ProbeText.literal("WORKSPACE"));
        // Create the snippets
        ProbeJSPlugins.forEachPlugin(plugin1 -> plugin1.addVSCodeSnippets(snippetDump));
        snippetDump.writeTo(SNIPPET_PATH);

        // And schemas
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.addJsonSchema(schemaDump));
        schemaDump.writeTo(ProbePaths.WORKSPACE_SETTINGS);
        appendGitIgnore();

        workSpaceEntry.provideBody(ProbeText.pjs("dump.snippets_generated"));

        EventJSInfos.loadFrom(EVENT_CACHE);
        EventJSInfos.writeTo(EVENT_CACHE);

        if (cleanOldDumps) {
            var cleanEntry = addEntry.apply(ProbeText.literal("CLEAN"));
            cleanEntry.provideBody(ProbeText.pjs("dump.cleaning"));

            for (var i = 0; i < namedDumps.size(); i++) {
                var dump = namedDumps.get(i);
                dump.cleanOldDumps();
                cleanEntry.provideBody(ProbeText.pjs("removed_script", namedDumps.subList(0, i + 1)
                        .stream()
                        .map(ProbeNamedDump::pjsDumpName)
                        .collect(Collectors.joining(","))));
            }
        }

        var classEntry = addEntry.apply(ProbeText.literal("CLASS"));
        classEntry.provideBody(() -> ProbeText.pjs("dump.walking_classes", classRegistry.foundClasses.size()));

        {
            var forgeScanSupport = ServiceLoader.load(ForgeScanSupport.class)
                .findFirst()
                .orElseThrow();
            classRegistry.addClasses(forgeScanSupport.scanForge(ProbeJSLegacy.CONFIG_CLASS_SCANNER.get()));
            classRegistry.addClasses(forgeScanSupport.scanMods(ProbeConfig.fullScanMods.get()));
        }
        for (ScriptDump scriptDump : scriptDumps) {
            classRegistry.addClasses(scriptDump.retrieveClasses());
        }

        classRegistry.walkClass();

        classEntry.provideBody(ProbeText.pjs("dump.class_discovered", classRegistry.foundClasses.size()));

        val index = new AtomicInteger();
        val executor = Executors.newCachedThreadPool(
            runnable -> new Thread(runnable, "ProbeDumpWorker-" + index.getAndIncrement())
        );

        CompletableFuture.allOf(scriptDumps.stream()
                .map(d -> CompletableFuture.runAsync(createDumpAction(d, addEntry), executor))
                .toArray(CompletableFuture[]::new)
            )
            .thenRunAsync(createDumpAction(sharedDump, addEntry), executor)
            .join();

        executor.shutdown();
    }

    private Runnable createDumpAction(ProbeNamedDump dump, Function<ProbeText, DumpMessageList.MessageEntry> addEntry) {
        var entry = addEntry.apply(ProbeText.literal(dump.pjsDumpName()));
        return () -> {
            try {
                entry.provideBody(() -> ProbeText.pjs("dump.report_progress").append(String.format(
                    "[%s/%s]",
                    dump.writers().mapToInt(TSFileWriter::countWrittenFiles).sum(),
                    dump.writers().mapToInt(TSFileWriter::countAcceptedFiles).sum()
                )));
                dump.open();
                dump.dump();
                entry.provideBody(ProbeText.pjs("dump.dump_finished", dump.pjsDumpName()).green());
            } catch (Throwable e) {
                val error = ProbeText.pjs("dump.dump_error", dump.pjsDumpName()).red();
                entry.provideBody(error);
                ProbeJS.LOGGER.error(error.unwrap().getString(), e);
            }
        };
    }

    private void appendGitIgnore() throws IOException {
        val toAppends = CollectUtils.ofList(".probe/*", "!.probe/probe-settings.json");
        val toRemoves = CollectUtils.ofList(".probe");

        ArrayList<String> lines;
        try (val reader = Files.newBufferedReader(ProbePaths.GIT_IGNORE)) {
            lines = reader.lines().collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ignored) {
            lines = new ArrayList<>();
        }

        lines.removeIf(toRemoves::contains);
        toAppends.removeIf(lines::contains);
        lines.addAll(toAppends);

        try (val writer = Files.newBufferedWriter(ProbePaths.GIT_IGNORE)) {
            for (val line : lines) {
                writer.write(line);
                writer.write('\n');
            }
        }
    }
}
