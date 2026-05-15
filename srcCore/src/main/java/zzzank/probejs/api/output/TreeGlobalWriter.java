package zzzank.probejs.api.output;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.tree.TreeIndexFile;
import zzzank.probejs.lang.typescript.tree.TreeNode;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class TreeGlobalWriter extends PerFileWriter {

    @Override
    protected void writeClasses(Path base) throws IOException {
        for (val file : files) {
            var node = new TreeNode(null, ClassPath.EMPTY);
            node.files().add(file);

            val filePath = base.resolve(file.path.getRemappedName() + this.suffix);
            try (val writer = writerProvider.apply(filePath)) {
                for (var line : new TreeIndexFile(node).format(writeAsModule)) {
                    writer.write(line);
                    writer.write('\n');
                }
            }
        }
    }
}
