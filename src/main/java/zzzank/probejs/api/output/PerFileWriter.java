package zzzank.probejs.api.output;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class PerFileWriter extends AbstractWriter {
    private final List<TypeScriptFile> files = new ArrayList<>();

    @Override
    public void accept(@NotNull TypeScriptFile file) {
        this.files.add(file);
    }

    private String filePathOf(ClassPath path) {
        return path.getJavaStylePath() + suffix;
    }

    @Override
    protected void postWriting() {
        files.clear();
    }

    @Override
    public int countAcceptedFiles() {
        return files.size();
    }

    @Override
    protected void writeClasses(Path base) throws IOException {
        for (val file : files) {
            val filePath = base.resolve(filePathOf(file.path));
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
            }
            try (val writer = Files.newBufferedWriter(filePath)) {
                writeFile(file, writer);
            }
        }
    }

    @Override
    protected void writeIndex(Path base) throws IOException {
        try (val writer = Files.newBufferedWriter(base.resolve(INDEX_FILE_NAME + suffix))) {
            for (val file : files) {
                writer.write(String.format(
                    "/// <reference path=%s />\n",
                    ProbeJS.GSON.toJson(filePathOf(file.path))
                ));
            }
        }
    }
}
