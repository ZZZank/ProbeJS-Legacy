package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.impl.VirtualFileSystem;
import zzzank.probejs.api.output.TreePackagedWriter;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.BasicMemberCollector;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.io.IOException;
import java.nio.file.Files;

/**
 * @author ZZZank
 */
public class TreePackagedWriterTest {

    @Test
    public void test() throws IOException {
        var transpiler = ProbeJSPlugins.buildTranspiler();

        var classRegistry = new ClassRegistry(new BasicMemberCollector());
        classRegistry.addClass(Number.class);
        classRegistry.addClass(Integer.class);
        classRegistry.addClass(Float.class);
        classRegistry.addClass(String.class);

        classRegistry.walkClass();

        var files = transpiler.dump(classRegistry.getFoundClasses());

        var fileSystem = new VirtualFileSystem();

        var root = fileSystem.getPath("");
        var writer = new TreePackagedWriter();

        files.values().forEach(writer::accept);

        writer.write(root);

        // files should have been written
        Assertions.assertFalse(fileSystem.viewFilePaths().isEmpty());

        // java.lang package should have an index file covering our registered classes
        var path = fileSystem.getPath("java/lang/index.d.ts");
        Assertions.assertTrue(Files.exists(path), "java/lang/index.d.ts should exist");
        var content = Files.readString(path);
        Assertions.assertTrue(content.contains("declare module \"java:java/lang\""));

        fileSystem.close();
    }
}
