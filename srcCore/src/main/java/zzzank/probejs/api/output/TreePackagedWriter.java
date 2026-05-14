package zzzank.probejs.api.output;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.tree.TreeIndexFile;
import zzzank.probejs.lang.typescript.tree.TreeNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class TreePackagedWriter extends AbstractWriter {
    private final List<TypeScriptFile> files = new ArrayList<>();

    @Override
    protected void postWriting() {
        files.clear();
    }

    @Override
    protected void writeClasses(Path base) throws IOException {
        var root = TreeNode.buildTree(this.files);

        for (var node : root.stream().toList()) {
            var parts = node.self().viewParts();

            var dir = base;
            for (var part : parts) {
                dir = dir.resolve(part);
            }
            Files.createDirectories(dir);

            var writeTo = dir.resolve("index" + suffix);
            try (var writer = writerProvider.apply(writeTo)) {
                var file = new TreeIndexFile(node);
                for (var line : file.format()) {
                    writer.write(line);
                    writer.write('\n');
                }
            }
        }
    }

    @Override
    protected void writeIndex(Path base) throws IOException {
    }

    @Override
    public void accept(@NotNull TypeScriptFile file) {
        files.add(file);
    }

    @Override
    public int countAcceptedFiles() {
        return files.size();
    }
}
