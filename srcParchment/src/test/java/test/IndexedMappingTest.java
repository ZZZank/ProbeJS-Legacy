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
import zzzank.probejs.lang.parchment.data.IndexedMappingData;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ZZZank
 */
public class IndexedMappingTest {

    @Test
    public void test() throws Exception {
        // 1. Load parchment-example.json from test resources
        InputStream is = getClass().getResourceAsStream("/parchment-1165.json");
        if (is == null) {
            System.err.print("parchment-example.json not found, exiting");
        }
        assertNotNull(is, "parchment-example.json not found on classpath");
        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        is.close();

        // 2. Parse to MappingDataContainer (same Gson setup as InjectParchment.fromJson)
        Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(new MDCGsonAdapterFactory())
            .registerTypeAdapter(SimpleVersion.class, new SimpleVersionAdapter())
            .registerTypeAdapterFactory(new MetadataAdapterFactory())
            .registerTypeAdapter(Named.class, new NamedAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();
        MappingDataContainer container = gson.fromJson(json, VersionedMappingDataContainer.class);
        assertNotNull(container, "Failed to parse MappingDataContainer from JSON");

        // 3. Convert to IndexedMappingData
        IndexedMappingData indexed = new IndexedMappingData(container);

        var path = Path.of("./indexed-parchment.json");
        try (var writer = Files.newBufferedWriter(path)) {
            gson.toJson(indexed, writer);
        }

        try (var reader = Files.newBufferedReader(path)) {
            var restored = new GsonBuilder().disableHtmlEscaping().create().fromJson(reader, IndexedMappingData.class);
            restored.restoreAfterDeserialization();
            assertEquals(indexed.indexer, restored.indexer);
        }
    }

}
