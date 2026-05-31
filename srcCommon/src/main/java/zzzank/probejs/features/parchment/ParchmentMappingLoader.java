package zzzank.probejs.features.parchment;

import zzzank.probejs.ProbeJS;
import zzzank.probejs.features.parchment.data.IndexedMappingData;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author ZZZank
 */
public abstract class ParchmentMappingLoader {

    public static IndexedMappingData CACHED;

    public static IndexedMappingData getOrInit(Path path) throws IOException {
        if (CACHED != null) {
            return CACHED;
        }

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return null;
        }
        try (var reader = Files.newBufferedReader(path)) {
            CACHED = ProbeJS.GSON.fromJson(reader, IndexedMappingData.class);
            CACHED.restoreAfterDeserialization();
        }
        return CACHED;
    }

    public static Optional<ClassTransformer> getTransformerOrInit(Path path) throws IOException {
        var data = getOrInit(path);
        return Optional.ofNullable(data).map(InjectParchment::new);
    }
}
