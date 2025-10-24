package zzzank.probejs.lang.linter;

import dev.latvian.kubejs.KubeJSPaths;
import lombok.val;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.linter.rules.NoNamespacePollution;
import zzzank.probejs.lang.linter.rules.RespectPriority;
import zzzank.probejs.lang.linter.rules.Rule;
import zzzank.probejs.utils.ProbeText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Linter {

    public static final Supplier<Linter> SERVER_SCRIPT = () ->
        new Linter(KubeJSPaths.SERVER_SCRIPTS).defaultRules();
    public static final Supplier<Linter> CLIENT_SCRIPT = () ->
        new Linter(KubeJSPaths.CLIENT_SCRIPTS).defaultRules();
    public static final Supplier<Linter> STARTUP_SCRIPT = () ->
        new Linter(KubeJSPaths.STARTUP_SCRIPTS).defaultRules();

    @FunctionalInterface
    public interface RuleFactory {
        Rule get();
    }

    public static final RuleFactory[] RULES = new RuleFactory[]{
        RespectPriority::new,
        NoNamespacePollution::new
    };

    public final Path scriptPath;
    public final List<Rule> rules = new ArrayList<>();

    public Linter(Path scriptPath) {
        this.scriptPath = scriptPath;
    }

    public Linter defaultRules() {
        for (RuleFactory rule : RULES) {
            rules.add(rule.get());
        }
        return this;
    }

    public Linter exclude(Class<?>... rule) {
        for (Class<?> aClass : rule) {
            rules.removeIf(aClass::isInstance);
        }
        return this;
    }

    public List<LintingWarning> lint() throws IOException {
        ArrayList<LintingWarning> warnings = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(scriptPath)) {
            for (Path path : (Iterable<? extends Path>) stream::iterator) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }
                if (!path.toString().endsWith("js")) {
                    continue;
                }
                List<String> content = Files.readAllLines(path);
                for (Rule rule : rules) {
                    rule.acceptFile(path, content);
                }
            }
        }

        for (Rule rule : rules) {
            warnings.addAll(rule.lint(scriptPath));
        }
        return warnings;
    }


    public static void defaultLint(Consumer<ProbeText> report) {
        try {
            val warnings = new ArrayList<ProbeText>();

            val startup = Linter.STARTUP_SCRIPT.get();
            for (val lintingWarning : startup.lint()) {
                warnings.add(lintingWarning.defaultFormatting(startup.scriptPath));
            }
            val server = Linter.SERVER_SCRIPT.get();
            for (val lintingWarning : server.lint()) {
                warnings.add(lintingWarning.defaultFormatting(server.scriptPath));
            }
            val client = Linter.CLIENT_SCRIPT.get();
            for (val lintingWarning : client.lint()) {
                warnings.add(lintingWarning.defaultFormatting(client.scriptPath));
            }

            for (val warning : warnings) {
                report.accept(warning);
            }
            if (warnings.isEmpty()) {
                report.accept(ProbeText.pjs("lint_passed").green());
            }
        } catch (Throwable e) {
            ProbeJS.LOGGER.error(e.getMessage());
        }
    }
}
