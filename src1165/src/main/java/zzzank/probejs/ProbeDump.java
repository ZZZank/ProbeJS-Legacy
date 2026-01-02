package zzzank.probejs;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.api.dump.TSDump;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.features.forge_scan.ClassScanner;
import zzzank.probejs.features.kubejs.EventJSInfos;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.schema.SchemaDump;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.ProbeNamedDump;
import zzzank.probejs.lang.typescript.SharedDump;
import zzzank.probejs.plugin.ProbeJSPlugins;
import zzzank.probejs.utils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProbeDump {
    public static final Path SNIPPET_PATH = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.code-snippets");
    public static final Path CLASS_CACHE = ProbePaths.PROBE.resolve("classes.txt");
    public static final Path EVENT_CACHE = ProbePaths.PROBE.resolve("kube_event.json");

    final SharedDump sharedDump = new SharedDump(ProbePaths.PROBE.resolve("shared"), ProbeJSPlugins.buildTranspiler());
    final SchemaDump schemaDump = new SchemaDump();
    final SnippetDump snippetDump = new SnippetDump();
    final Collection<ScriptDump> scriptDumps = new ArrayList<>();
    public final Consumer<ProbeText> messageSender;

    public ProbeDump(Consumer<ProbeText> messageSender) {
        this.messageSender = messageSender;
    }

    public void addScript(@NotNull ScriptDump dump) {
        scriptDumps.add(Objects.requireNonNull(dump));
    }

    private void onModChange() throws IOException {
        // Decompile stuffs - here we scan mod classes even if we don't decompile
        // So we have all classes without needing to decompile
        ClassRegistry.REGISTRY.addClasses(ClassScanner.scanForge());
        ClassRegistry.REGISTRY.addClasses(ClassScanner.scanMods(ProbeConfig.fullScanMods.get()));

        report(ProbeText.pjs("dump.cleaning"));
        sharedDump.cleanOldDumps();
        report(ProbeText.pjs("removed_script", sharedDump.pjsDumpName()));
        for (ScriptDump scriptDump : scriptDumps) {
            scriptDump.cleanOldDumps();
            report(ProbeText.pjs("removed_script", scriptDump.pjsDumpName()));
        }

//        SchemaDownloader downloader = new SchemaDownloader();
//        try (var zipStream = downloader.openSchemaStream()) {
//            downloader.processFile(zipStream);
//        } catch (Throwable err) {
//            ProbeJS.LOGGER.error(err.getMessage());
//        }
    }

    private void onRegistryChange() throws IOException {

    }

    private synchronized void report(ProbeText text) {
        messageSender.accept(text);
    }

    public void trigger() throws Exception {
        report(ProbeText.pjs("dump.start").green());

        // Create the snippets
        ProbeJSPlugins.forEachPlugin(plugin1 -> plugin1.addVSCodeSnippets(snippetDump));
        snippetDump.writeTo(SNIPPET_PATH);

        // And schemas
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.addJsonSchema(schemaDump));
        schemaDump.writeTo(ProbePaths.WORKSPACE_SETTINGS);
        writeVSCodeConfig();
        appendGitIgnore();

        report(ProbeText.pjs("dump.snippets_generated"));

        EventJSInfos.loadFrom(EVENT_CACHE);
        EventJSInfos.writeTo(EVENT_CACHE);

        val modHash = GameUtils.modHash();
        if (modHash != ProbeConfig.modHash.get()) {
            report(ProbeText.pjs("dump.mod_changed").aqua());
            onModChange();
            ProbeJS.LOGGER.info("mod hash updating from {} to {}", ProbeConfig.modHash.get(), modHash);
            ProbeConfig.modHash.set(GameUtils.modHash());
        }

        val registryHash = GameUtils.registryHash();
        if (registryHash != ProbeConfig.registryHash.get()) {
            onRegistryChange();
            ProbeJS.LOGGER.info("registry hash updating from {} to {}", ProbeConfig.registryHash.get(), registryHash);
            ProbeConfig.registryHash.set(registryHash);
        }

        // Fetch classes that will be used in the dump
        ClassRegistry.REGISTRY.loadFrom(CLASS_CACHE);
        for (ScriptDump scriptDump : scriptDumps) {
            ClassRegistry.REGISTRY.addClasses(scriptDump.retrieveClasses());
        }

        ClassRegistry.REGISTRY.walkClass();
        ClassRegistry.REGISTRY.writeTo(CLASS_CACHE);
        report(ProbeText.pjs("dump.class_discovered", ClassRegistry.REGISTRY.foundClasses.size()));

        val reporters = Collections.synchronizedList(new ArrayList<ProbeNamedDump>());
        reporters.addAll(scriptDumps);
        reporters.add(sharedDump);

        val index = new AtomicInteger();
        val executor = Executors.newCachedThreadPool(
            runnable -> new Thread(runnable, "ProbeDumpWorker-" + index.getAndIncrement())
        );

        // monitor
        var timer = new Timer("ProbeDumpWatcher", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                val dumpProgress = reporters.stream()
                    .filter(TSDump::running)
                    .map(dump -> {
                        val written = dump.writers().mapToInt(TSFileWriter::countWrittenFiles).sum();
                        val total = dump.writers().mapToInt(TSFileWriter::countAcceptedFiles).sum();
                        return String.format("%s[%d/%d]", dump.pjsDumpName(), written, total);
                    })
                    .collect(Collectors.joining(", "));
                if (dumpProgress.isEmpty()) {
                    report(ProbeText.pjs("dump.end"));
                    this.cancel();
                    return;
                }
                report(ProbeText.pjs("dump.report_progress").append(ProbeText.literal(dumpProgress).blue()));
            }
        }, 2000);

        // per script
        val scriptDumpFutures = scriptDumps.stream()
            .map(d -> CompletableFuture.runAsync(createDumpAction(d), executor))
            .toArray(CompletableFuture[]::new);

        // shared
        CompletableFuture.allOf(scriptDumpFutures)
            .thenRunAsync(createDumpAction(sharedDump), executor)
            .join();

        executor.shutdown();
        timer.cancel();
    }

    private Runnable createDumpAction(ProbeNamedDump dump) {
        return () -> {
            try {
                dump.open();
                dump.dump();
                report(ProbeText.pjs("dump.dump_finished", dump.pjsDumpName()).green());
            } catch (Throwable e) {
                val error = ProbeText.pjs("dump.dump_error", dump.pjsDumpName()).red();
                report(error);
                ProbeJS.LOGGER.error(error.unwrap().getString(), e);
            }
        };
    }

    private void writeVSCodeConfig() throws IOException {
        val config = JsonUtils.parseObject(Map.of(
            "json.schemas", List.of(
                Map.of(
                    "fileMatch", List.of("/recipe_schemas/*.json"),
                    "url", "./.vscode/recipe.json"
                )
            )
        ));
        FileUtils.writeMergedConfig(ProbePaths.VSCODE_JSON, config);
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
