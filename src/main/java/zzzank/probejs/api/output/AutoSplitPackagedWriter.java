package zzzank.probejs.api.output;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.utils.CollectUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author ZZZank
 */
public class AutoSplitPackagedWriter extends AbstractWriter {

    public static final String SIMPLE_PACKAGE = "simple_package_classes";

    public static AutoSplitPackagedWriter defaultSetup() {
        return new AutoSplitPackagedWriter(
            2,
            Integer.MAX_VALUE,
            200,
            SIMPLE_PACKAGE
        );
    }

    protected Map<String, List<TypeScriptFile>> packaged = Collections.singletonMap(null, new ArrayList<>());

    public final int minPackageCount;
    public final int maxPackageCount;
    public final int splitThreshold;

    public final String fallbackFileName;
    protected int accepted = 0;

    public AutoSplitPackagedWriter(
        int minPackageCount,
        int maxPackageCount,
        int splitThreshold,
        String fallbackFileName
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
        this.fallbackFileName = fallbackFileName;
        this.maxPackageCount = maxPackageCount;
        this.splitThreshold = splitThreshold;
    }

    @Override
    public void accept(@NotNull TypeScriptFile file) {
        packaged.get(null).add(file);
        accepted += 1;
    }

    @Override
    protected void preWriting() {
        packaged = trySpread(packaged, minPackageCount);
    }

    protected Map<String, List<TypeScriptFile>> trySpread(Map<String, List<TypeScriptFile>> map, int packageCount) {
        if (shouldSpread(map) && packageCount <= maxPackageCount) {
            val spread = new HashMap<String, List<TypeScriptFile>>();
            for (val entry : map.entrySet()) {
                spread.putAll(spreadEntry(entry, packageCount));
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
            || fallbackFileName.equals(entry.getKey())
        ) {
            return Collections.singletonMap(entry.getKey(), files);
        }

        val mapper = CollectUtils.<String, List<TypeScriptFile>>ignoreInput(ArrayList::new);
        val spread = new HashMap<String, List<TypeScriptFile>>();
        for (val file : files) {
            val parts = file.path.getParts();
            val fileName = String.join(".", parts.subList(0, Integer.min(packageCount, parts.size() - 1)));
            spread.computeIfAbsent(fileName, mapper)
                .add(file);
        }

        ProbeJS.LOGGER.info(
            "{} TS files with common key {} spread into {} entries",
            files.size(),
            entry.getKey(),
            spread.size()
        );

        if (spread.size() == 1 && Objects.equals(entry.getKey(), spread.keySet().iterator().next())) {
            ProbeJS.LOGGER.info(
                "{} TS files with common key {} can't be spread further, skipping spreading",
                files.size(),
                entry.getKey()
            );
            return spread;
        }

        return trySpread(spread, packageCount + 1);
    }

    @Override
    protected void postWriting() {
        accepted = 0;
        packaged = Collections.singletonMap(null, new ArrayList<>());
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
