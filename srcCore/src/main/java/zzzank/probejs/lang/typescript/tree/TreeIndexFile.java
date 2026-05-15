package zzzank.probejs.lang.typescript.tree;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.DeclarationCode;
import zzzank.probejs.lang.typescript.refer.ImportType;
import zzzank.probejs.lang.typescript.refer.Reference;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/// Represents an index.d.ts under a package
/// ```
/// import { A } from "yyy";
/// export * as subpackage from "xxx.subpackage";
/// declare "xxx" {
///     export class Bar {
///         samePackageRefDoesNotNeedImport(foo: Foo): void
///     }
///     type Bar_ = Bar
///     export class Foo {
///         bar(a: A): void;
///     }
/// }
/// ```
public class TreeIndexFile {
    public final Declaration declaration;
    public final TreeNode node;

    public TreeIndexFile(TreeNode node) {
        this.declaration = new Declaration();
        this.node = node;
    }

    public List<String> format(boolean asModule) {
        // load codes & import
        var codes = new ArrayList<Code>();
        for (var file : node.files()) {
            codes.addAll(file.codes);
            declaration.references.putAll(file.declaration.references);
            declaration.excludedNames.addAll(file.declaration.excludedNames);
        }
        for (var code : codes) {
            if (code instanceof DeclarationCode decl) {
                decl.reportDeclaredNames(this.declaration.usedNames);
            }
        }
        for (var code : codes) {
            for (val info : code.getImportInfos()) {
                declaration.addImport(info);
            }
        }

        var formatted = new ArrayList<String>();
        var selfName = node.self().getFirstValidPath();

        // 1. import statements for references outside this package, grouped by module
        var grouped = declaration.references.values()
            .stream()
            .collect(Collectors.groupingBy(ref -> ref.info.path.getFirstValidPackage()));

        boolean hasImport = false;
        for (var entry : grouped.entrySet()) {
            var moduleName = entry.getKey();
            var references = entry.getValue();

            if (moduleName.equals(selfName)) {
                continue;
            }

            formatted.add(getImportStatement(references, moduleName));
            hasImport = true;
        }
        if (hasImport) {
            formatted.add("");
        } else {
            formatted.add("export {} // mark the file as module");
        }

        // 2. subpackage re-exports
        for (val child : node.children().entrySet()) {
            formatted.add(String.format(
                "export * as %s from %s",
                child.getKey(),
                ProbeJS.GSON.toJson(child.getValue().self().getFirstValidPath().replace('.', '/'))
            ));
        }
        if (!node.children().isEmpty()) {
            formatted.add("");
        }

        // 3. declare block containing this package's own codes
        var blockLines = new ArrayList<String>();
        for (var code : codes) {
            blockLines.addAll(code.format(declaration));
        }
        if (!blockLines.isEmpty()) {
            if (asModule) {
                formatted.add(String.format("declare module %s {", ProbeJS.GSON.toJson(selfName.replace('.', '/'))));
            }
            for (var line : blockLines) {
                formatted.add("    " + line);
            }
            if (asModule) {
                formatted.add("}");
            }
        }

        return formatted;
    }

    private static @NotNull String getImportStatement(List<Reference> references, String moduleName) {
        var names = new ArrayList<String>();

        for (var ref : references) {
            val original = ref.info.path.getSimpleName();
            Function<ImportType, String> nameMapper = original.equals(ref.deduped)
                ? type -> type.fmt(original)
                : type -> String.format("%s as %s", type.fmt(original), type.fmt(ref.deduped));

            ref.info.getTypes().map(nameMapper).forEach(names::add);
        }

        return String.format(
            "import { %s } from %s",
            String.join(", ", names),
            ProbeJS.GSON.toJson(moduleName.replace('.', '/'))
        );
    }
}
