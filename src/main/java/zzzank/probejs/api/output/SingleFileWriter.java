package zzzank.probejs.api.output;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class SingleFileWriter extends AbstractWriter {

    public final String fileName;
    private final List<TypeScriptFile> files = new ArrayList<>();

    public SingleFileWriter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void accept(@NotNull TypeScriptFile file) {
        this.files.add(file);
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
        val filePath = base.resolve(this.fileName + suffix);
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
        try (val writer = Files.newBufferedWriter(filePath)) {
            for (val file : files) {
                writeFile(file, writer);
                writer.write('\n');
            }
        }
    }

    @Override
    protected void writeIndex(Path base) throws IOException {
        try (val writer = Files.newBufferedWriter(base.resolve(INDEX_FILE_NAME + suffix))) {
            writer.write(String.format("/// <reference path=%s />\n", ProbeJS.GSON.toJson(fileName + suffix)));
        }
    }
}
