package zzzank.probejs.lang.typescript.tree;

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
    /// Node(parent=null, self="*", files=[], children={"java": ... , "org": ... , ...})
    ///     Node(parent="*", self="java.*")
    ///         Node(parent="java.*", self="java.lang.*", files=[TypeScriptFile("java.lang.String"), TypeScriptFile("java.lang.Number")])
    ///         Node(parent="java.*", self="java.util.*", files=[...])
    ///     Node(parent="*", self="org.*")
    /// ```
    public static TreeNode buildTree(List<TypeScriptFile> files) {
        var rootSelf = ClassPath.ofArtificial("*");
        var root = new TreeNode(null, rootSelf);

        for (var tsFile : files) {
            var classPath = tsFile.path;
            if (classPath == null) {
                continue;
            }

            var packagePath = ClassPath.ofArtificial(classPath.getFirstValidPackage());
            var parts = packagePath.viewParts();

            var current = root;
            var parentPath = rootSelf;
            var accumulated = new StringBuilder();

            for (var part : parts) {
                if (!accumulated.isEmpty()) {
                    accumulated.append(".");
                }
                accumulated.append(part);

                var selfPath = ClassPath.ofArtificial(accumulated + ".*");
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

    public static String formatPackage(ClassPath path) {
        return "java:" + path.getFirstValidPackage().replace('.', '/');
    }

    public Stream<TreeNode> stream() {
        return Stream.concat(Stream.of(this), children.values().stream().flatMap(TreeNode::stream));
    }
}
