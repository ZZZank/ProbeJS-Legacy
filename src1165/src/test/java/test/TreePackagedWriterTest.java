package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zzzank.probejs.api.output.TreePackagedWriter;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.BasicMemberCollector;
import zzzank.probejs.plugin.ProbeJSPlugins;
import zzzank.probejs.utils.FileUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

        var root = Path.of("");
        var simulatedFS = new TreeMap<String, StringWriter>();

        var writer = new TreePackagedWriter();
        writer.setWriterProvider((path) -> simulatedFS.computeIfAbsent(
            FileUtils.relativePathStr(root, path),
            ignored -> new StringWriter()
        ));

        files.values().forEach(writer::accept);

        writer.write(root);

        var result = simulatedFS.entrySet()
            .stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().toString()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        result = new TreeMap<>(result);

        // files should have been written
        Assertions.assertFalse(simulatedFS.isEmpty());

        // java.lang package should have an index file covering our registered classes
        var langIndex = simulatedFS.get("java/lang/index.d.ts");
        Assertions.assertNotNull(langIndex, "java/lang/index.d.ts should exist");
        var content = langIndex.toString();
        Assertions.assertTrue(content.contains("declare \"java.lang\""));
    }
}
