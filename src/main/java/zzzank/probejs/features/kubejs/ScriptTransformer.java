package zzzank.probejs.features.kubejs;

import dev.latvian.mods.rhino.CompilerEnvirons;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Parser;
import dev.latvian.mods.rhino.ast.*;
import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.utils.GameUtils;
import zzzank.probejs.utils.NameUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ScriptTransformer {
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
                case "function" -> parts[1].split("\\(")[0];
                case "var", "let", "const" -> parts[1].split(" ")[0];
                default -> null;
            };
            if (identifier == null) {
                continue;
            }

            exportedSymbols.add(identifier);
            lines.set(i, trimmed);
        }
    }

    // Wraps the code in let {...} = (()=>{...;return {...};})()
    public void wrapScope() {
        val exported = exportedSymbols
            .stream()
            .map(s -> String.format("%s: %s", s, s))
            .collect(Collectors.joining(", "));
        lines.set(0, String.format("const {%s} = (()=>{ %s", exported, lines.get(0)));
        lines.set(lines.size() - 1, String.format("%s; return {%s};})()", lines.get(lines.size() - 1), exported));
    }

    public String[] transform() {
        try {
            processExport();
            processRequire();
            // If there's no symbol to be exported, and no `require()` call, it will not be marked as CommonJS module
            if (ProbeConfig.isolatedScopes.get() && (!exportedSymbols.isEmpty() || requireCounts != 0)) {
                wrapScope();
            }
        } catch (Throwable t) {
            GameUtils.logThrowable(t);
        }

        return lines.toArray(new String[0]);
    }
}