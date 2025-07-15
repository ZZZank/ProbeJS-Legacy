package zzzank.probejs.features.kubejs;

import dev.latvian.mods.rhino.CompilerEnvirons;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Parser;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ast.FunctionCall;
import dev.latvian.mods.rhino.ast.Name;
import dev.latvian.mods.rhino.ast.StringLiteral;
import dev.latvian.mods.rhino.ast.VariableDeclaration;
import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.utils.GameUtils;
import zzzank.probejs.utils.NameUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ScriptTransformer {

    public static Object transformedScriptEval(
        Context cx,
        Scriptable scope,
        String source,
        String sourceName,
        int lineno,
        Object securityDomain
    ) {
        val scriptTransformer = new ScriptTransformer(source.split("\\n"));

        source = String.join("\n", scriptTransformer.transform());
        if (scriptTransformer.shouldWrapScope()) {
            lineno -= 1; // first line will be export
        }

        return cx.evaluateString(scope, source, sourceName, lineno, securityDomain);
    }

    private static final String PLACEHOLDER = "!@#$%^"; // placeholder to not mutate original string length

    private static final Supplier<Parser> PARSER = () -> {
        val compilerEnvirons = new CompilerEnvirons();
        compilerEnvirons.initFromContext(Context.enterWithNewFactory());
        Context.exit();
        return new Parser(compilerEnvirons);
    };

    public final Set<String> exportedSymbols;
    private int requireCounts;
    private List<String> lines;

    public ScriptTransformer(String[] lines) {
        this.lines = Arrays.asList(lines);
        requireCounts = 0;
        this.exportedSymbols = new HashSet<>();
    }

    // process the const require
    public void processRequire() {
        String joined = String.join("\n", lines);
        val root = PARSER.get().parse(joined, "probejs_parse.js", 0);
        List<int[]> cuts = new ArrayList<>();

        for (val statement : root.getStatements()) {
            // declaring
            if (!(statement instanceof VariableDeclaration declaration)) {
                continue;
            }
            for (val variable : declaration.getVariables()) {
                // used require()
                if (variable.getInitializer() instanceof FunctionCall call
                    && call.getTarget() instanceof Name name
                    && name.getIdentifier().equals("require")
                    && !call.getArguments().isEmpty()
                    && call.getArguments().get(0) instanceof StringLiteral literal
                ) {
                    requireCounts++;
                    if (!literal.getValue().startsWith(ClassPath.TS_PATH_PREFIX)) {
                        // not java package, likely to be cross file imports, cut it
                        cuts.add(new int[]{
                            statement.getPosition(), statement.getPosition() + statement.getLength()
                        });
                    } else if (declaration.isConst()) {
                        // is class path used by require, transform if it's const
                        joined = NameUtils.replaceRegion(
                            joined,
                            statement.getPosition(),
                            statement.getPosition() + statement.getLength(),
                            "const ",
                            PLACEHOLDER
                        );
                    }
                }
            }
        }

        cuts.sort(Comparator.comparing(p -> p[0]));
        joined = NameUtils.cutOffStartEnds(joined, cuts);

        joined = joined.replace(PLACEHOLDER, "let ");
        lines = Arrays.asList(joined.split("\\n"));
    }

    // scans for the export function/let/var/const
    public void processExport() {
        for (int i = 0; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();
            if (!trimmed.startsWith("export ")) {
                continue;
            }

            trimmed = trimmed.substring("export ".length()).trim();
            val parts = trimmed.split(" ", 2);

            val identifier = switch (parts[0]) {
                case "function" -> parts[1].split("\\(", 2)[0];
                case "var", "let", "const" -> parts[1].split(" ", 2)[0];
                default -> null;
            };
            if (identifier == null) {
                continue;
            }

            exportedSymbols.add(identifier);
            lines.set(i, lines.get(i).replaceFirst("export ", ""));
        }
    }

    // Wraps the code in let {...} = (()=>{...;return {...};})()
    public void wrapScope() {
        val exported = exportedSymbols
            .stream()
            .map(s -> String.format("%s: %s", s, s))
            .collect(Collectors.joining(", "));

        val linesOriginal = lines;

        lines = new ArrayList<>(linesOriginal.size() + 2);
        lines.add(String.format("const { %s } = (() => {", exported));
        lines.addAll(linesOriginal);
        lines.add(String.format("return { %s }; })()", exported));
    }

    public String[] transform() {
        try {
            processExport();
            processRequire();
            // If there's no symbol to be exported, and no `require()` call, it will not be marked as CommonJS module
            if (shouldWrapScope()) {
                wrapScope();
            }
        } catch (Throwable t) {
            GameUtils.logThrowable(t);
        }

        return lines.toArray(new String[0]);
    }

    public boolean shouldWrapScope() {
        return ProbeConfig.isolatedScopes.get() && (!exportedSymbols.isEmpty() || requireCounts != 0);
    }
}