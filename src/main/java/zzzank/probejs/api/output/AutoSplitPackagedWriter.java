package zzzank.probejs.api.output;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.utils.CollectUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author ZZZank
 */
public class AutoSplitPackagedWriter extends AbstractWriter {

    protected Map<String, List<TypeScriptFile>> packaged = new HashMap<>();

    public final int minPackageCount;
    public final int maxPackageCount;
    public final int splitThreshold;

    public final String fallbackFileNamePrefix;
    protected int accepted = 0;

    public AutoSplitPackagedWriter(
        int minPackageCount,
        int maxPackageCount,
        int splitThreshold,
        String fallbackFileNamePrefix
    ) {
        if (minPackageCount <= 0) {
            throw new IllegalArgumentException(
                "'minPackageCount' must be a positive number, but got " + minPackageCount);
        }
        if (maxPackageCount < minPackageCount) {
            throw new IllegalArgumentException(
                "'maxPackageCount' must be no less than 'minPackageCount', but got max " + maxPackageCount + " and min "
                    + minPackageCount);
        }
        if (splitThreshold <= 0) {
            throw new IllegalArgumentException(
                "'splitThreshold' must be a positive number, but got " + splitThreshold);
        }
        this.minPackageCount = minPackageCount;
        this.fallbackFileNamePrefix = fallbackFileNamePrefix;
        this.maxPackageCount = maxPackageCount;
        this.splitThreshold = splitThreshold;
    }

    @Override
    public void accept(@NotNull TypeScriptFile file) {
        val cPath = file.path;
        val fileName = cPath.getParts().size() > minPackageCount
            ? String.join(".", cPath.getParts().subList(0, minPackageCount))
            : fallbackFileNamePrefix;
        packaged.computeIfAbsent(fileName, k -> new ArrayList<>())
            .add(file);
        accepted += 1;
    }

    @Override
    protected void preWriting() {
        packaged = trySpread(packaged);
    }

    protected Map<String, List<TypeScriptFile>> trySpread(Map<String, List<TypeScriptFile>> map) {
        if (shouldSpread(map)) {
            val spread = new HashMap<String, List<TypeScriptFile>>();
            for (val entry : packaged.entrySet()) {
                spread.putAll(spreadEntry(entry, minPackageCount + 1));
            }
            return spread;
        }
        return map;
    }

    protected boolean shouldSpread(Map<String, List<TypeScriptFile>> map) {
        for (val files : map.values()) {
            if (files.size() >= splitThreshold) {
                return true;
            }
        }
        return false;
    }

    protected Map<String, List<TypeScriptFile>> spreadEntry(
        Map.Entry<String, List<TypeScriptFile>> entry,
        int packageCount
    ) {
        val files = entry.getValue();
        if (files.size() < splitThreshold
            || packageCount > maxPackageCount
            || fallbackFileNamePrefix.equals(entry.getKey())
        ) {
            return Collections.singletonMap(entry.getKey(), files);
        }

        val spread = new HashMap<String, List<TypeScriptFile>>();
        for (val file : files) {
            val path = file.path;
            val fileName = path.getParts().size() > packageCount
                ? String.join(".", path.getParts().subList(0, packageCount))
                : fallbackFileNamePrefix;
            spread.computeIfAbsent(fileName, CollectUtils.keyIgnoredNewArrayList())
                .add(file);
        }

        return trySpread(spread);
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
            try (val writer = Files.newBufferedWriter(filePath)) {
                for (val file : files) {
                    writeFile(file, writer);
                    writer.write('\n');
                }
            }
        }
    }

    @Override
    protected void writeIndex(Path base) throws IOException {
        try (val writer = Files.newBufferedWriter(base.resolve(INDEX_FILE_NAME + suffix))) {
            for (val key : packaged.keySet()) {
                val refPath = key + suffix;
                writer.write(String.format("/// <reference path=%s />\n", ProbeJS.GSON.toJson(refPath)));
            }
        }
    }
}
