package zzzank.probejs.lang.typescript.tree;

import org.jetbrains.annotations.VisibleForTesting;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public record TreeNode(
    ClassPath parent,
    ClassPath self,
    ArrayList<TypeScriptFile> files,
    TreeMap<String, TreeNode> children
) {
    public TreeNode(ClassPath parent, ClassPath self) {
        this(parent, self, new ArrayList<>(), new TreeMap<>());
    }

    /// example:
    /// ```
    /// Node(parent=null, self=EMPTY, files=[], children={"java": ... , "org": ... , ...})
    ///     Node(parent=EMPTY, self="java")
    ///         Node(parent="java", self="java.lang", files=[TypeScriptFile("java.lang.String"), TypeScriptFile("java.lang.Number")])
    ///         Node(parent="java", self="java.util", files=[...])
    ///     Node(parent=EMPTY, self="org")
    /// ```
    @VisibleForTesting
    public static TreeNode buildTree(List<TypeScriptFile> files) {
        var root = new TreeNode(null, ClassPath.EMPTY);

        for (var tsFile : files) {
            var classPath = tsFile.path;
            if (classPath == null) {
                continue;
            }

            var packagePath = ClassPath.ofArtificial(classPath.getFirstValidPackage());
            var parts = packagePath.viewParts();

            var current = root;
            var parentPath = ClassPath.EMPTY;
            var accumulated = new StringBuilder();

            for (var part : parts) {
                if (!accumulated.isEmpty()) {
                    accumulated.append(".");
                }
                accumulated.append(part);

                var selfPath = ClassPath.ofArtificial(accumulated.toString());
                var finalParentPath = parentPath;
                current = current.children().computeIfAbsent(
                    part,
                    k -> new TreeNode(finalParentPath, selfPath)
                );
                parentPath = selfPath;
            }

            current.files().add(tsFile);
        }

        return root;
    }

    public Stream<TreeNode> stream() {
        return Stream.concat(Stream.of(this), children.values().stream().flatMap(TreeNode::stream));
    }
}
