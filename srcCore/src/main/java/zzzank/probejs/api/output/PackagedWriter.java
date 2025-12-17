package zzzank.probejs.api.output;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.utils.Asser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZZZank
 */
public class PackagedWriter extends AbstractWriter {

    protected final Map<String, List<TypeScriptFile>> packaged = new HashMap<>();
    public final int minPackageCount;
    public final String fallbackFileName;
    protected int accepted = 0;

    public PackagedWriter(int minPackageCount, String fallbackFileName) {
        Asser.f(minPackageCount <= 0, "minPackageCount <= 0");
        this.minPackageCount = minPackageCount;
        this.fallbackFileName = fallbackFileName;
    }

    @Override
    public void accept(@NotNull TypeScriptFile file) {
        val cPath = file.path;
        val fileName = cPath.viewParts().size() > minPackageCount
            ? String.join(".", cPath.viewParts().subList(0, minPackageCount))
            : fallbackFileName;
        packaged.computeIfAbsent(fileName, k -> new ArrayList<>())
            .add(file);
        accepted += 1;
    }

    @Override
    protected void postWriting() {
        accepted = 0;
        packaged.clear();
    }

    @Override
    public int countAcceptedFiles() {
        return accepted;
    }

    @Override
    protected void writeClasses(Path base) throws IOException {
        for (val entry : packaged.entrySet()) {
            val fileName = entry.getKey();
            val files = entry.getValue();
            val filePath = base.resolve(fileName + suffix);
            try (val writer = writerProvider.apply(filePath)) {
                for (val file : files) {
                    writeFile(file, writer);
                    writer.write('\n');
                }
            }
        }
    }

    @Override
    protected void writeIndex(Path base) throws IOException {
        try (val writer = writerProvider.apply(base.resolve(INDEX_FILE_NAME + suffix))) {
            for (val key : packaged.keySet()) {
                val refPath = key + suffix;
                writer.write(String.format("/// <reference path=%s />\n", ProbeJS.GSON.toJson(refPath)));
            }
        }
    }
}
