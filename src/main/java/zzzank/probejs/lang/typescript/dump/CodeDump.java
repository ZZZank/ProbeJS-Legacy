package zzzank.probejs.lang.typescript.dump;

import com.google.common.collect.Maps;
import dev.latvian.kubejs.util.UtilsJS;
import lombok.val;
import org.apache.commons.io.FileUtils;
import zzzank.probejs.api.output.AutoSplitPackagedWriter;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.transpiler.Transpiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ZZZank
 */
public class CodeDump {

    public final Transpiler transpiler = new Transpiler();
    public final TSFileWriter writer = AutoSplitPackagedWriter.defaultSetup();
    public final Set<ClassPath> requestedBySubDump = new HashSet<>();
    public final Path path;

    public CodeDump(Path path) {
        this.path = path;
    }

    public void dump() throws IOException {
        val globalClasses = transpiler.dump(ClassRegistry.REGISTRY.getFoundClasses());
        val filtered = Maps.filterKeys(globalClasses, path -> !requestedBySubDump.contains(path));

        filtered.values().forEach(writer::accept);

        if (Files.notExists(path)) {
            UtilsJS.tryIO(() -> Files.createDirectories(path));
        }
        writer.write(path);
    }

    public void clearFiles() throws IOException {
        FileUtils.deleteDirectory(path.toFile());
    }
}
