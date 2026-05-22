package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.BasicMemberCollector;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.tree.TreeNode;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author ZZZank
 */
public class PackageTreeTest {

    @Test
    public void test() {
        var transpiler = new Transpiler(new TypeConverter());

        var classRegistry = new ClassRegistry(new BasicMemberCollector());
        classRegistry.addClass(Number.class);
        classRegistry.addClass(Integer.class);
        classRegistry.addClass(Float.class);
        classRegistry.addClass(String.class);

        classRegistry.walkClass();

        var files = transpiler.dump(classRegistry.getFoundClasses());

        var tree = TreeNode.buildTree(List.copyOf(files.values()));

        // root
        Assertions.assertNull(tree.parent());
        Assertions.assertEquals(ClassPath.ofArtificial("*"), tree.self());

        // root -> "java"
        var javaNode = tree.children().get("java");
        Assertions.assertNotNull(javaNode);
        Assertions.assertEquals(ClassPath.ofArtificial("*"), javaNode.parent());
        Assertions.assertEquals(ClassPath.ofArtificial("java.*"), javaNode.self());

        // root -> "java" -> "lang"
        var langNode = javaNode.children().get("lang");
        Assertions.assertNotNull(langNode);
        Assertions.assertEquals(ClassPath.ofArtificial("java.*"), langNode.parent());
        Assertions.assertEquals(ClassPath.ofArtificial("java.lang.*"), langNode.self());

        // our 4 classes should be in the java.lang node (walkClass may add more)
        var classNamesInLang = langNode.files().stream()
            .map(f -> f.path)
            .filter(Objects::nonNull)
            .map(ClassPath::getSimpleName)
            .toList();

        var expectedClasses = Set.of("$Number", "$Integer", "$Float", "$String");
        for (var name : expectedClasses) {
            Assertions.assertTrue(
                classNamesInLang.contains(name),
                "java.lang node should contain '" + name + "', but got: " + classNamesInLang
            );
        }
    }
}
