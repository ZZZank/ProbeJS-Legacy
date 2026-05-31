package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.parchmentmc.feather.io.gson.MDCGsonAdapterFactory;
import org.parchmentmc.feather.io.gson.NamedAdapter;
import org.parchmentmc.feather.io.gson.OffsetDateTimeAdapter;
import org.parchmentmc.feather.io.gson.SimpleVersionAdapter;
import org.parchmentmc.feather.io.gson.metadata.MetadataAdapterFactory;
import org.parchmentmc.feather.mapping.MappingDataContainer;
import org.parchmentmc.feather.mapping.VersionedMappingDataContainer;
import org.parchmentmc.feather.named.Named;
import org.parchmentmc.feather.util.SimpleVersion;
import test.impl.VirtualFileSystem;
import zzzank.probejs.features.parchment.data.IndexedMappingData;
import test.impl.IndexedMappingDataBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ZZZank
 */
public class IndexedMappingGenTest {

    private static final boolean WRITE_FILE = true;

    /// glad that Parchment is licensed CC0, so we can modify it for smaller file size
    ///
    /// [LICENSE.txt](https://github.com/ParchmentMC/Parchment/blob/versions/1.21.x/LICENSE.txt)
    @Test
    public void test() throws Exception {
        var timestamp = "parchment-1.20.1-2023.09.03";
        InputStream is = getClass().getResourceAsStream("/" + timestamp + ".json");
        if (is == null) {
            System.err.print("parchment json not found, exiting");
            return;
        }

        Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapterFactory(new MDCGsonAdapterFactory())
            .registerTypeAdapter(SimpleVersion.class, new SimpleVersionAdapter())
            .registerTypeAdapterFactory(new MetadataAdapterFactory())
            .registerTypeAdapter(Named.class, new NamedAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();
        MappingDataContainer container;
        try (var reader = new BufferedReader(new InputStreamReader(is))) {
            container = gson.fromJson(reader, VersionedMappingDataContainer.class);
        } finally {
            is.close();
        }
        assertNotNull(container, "Failed to parse MappingDataContainer from JSON");

        // 3. Convert to IndexedMappingData via builder
        IndexedMappingData indexed = IndexedMappingDataBuilder.build(container);
        indexed.timestamp = timestamp;

        FileSystem fs = WRITE_FILE ? FileSystems.getDefault() : new VirtualFileSystem();
        var path = fs.getPath("./reindexed-parchment.json");

        try (var writer = Files.newBufferedWriter(path)) {
            gson.toJson(indexed, writer);
        }

        try (var reader = Files.newBufferedReader(path)) {
            var restored = new GsonBuilder()
                .disableHtmlEscaping()
                .create()
                .fromJson(reader, IndexedMappingData.class);
            restored.restoreAfterDeserialization();
            assertEquals(indexed.indexer, restored.indexer);
        }
    }

}
