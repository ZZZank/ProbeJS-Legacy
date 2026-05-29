package test;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.parchmentmc.feather.io.gson.MDCGsonAdapterFactory;
import org.parchmentmc.feather.io.gson.NamedAdapter;
import org.parchmentmc.feather.io.gson.OffsetDateTimeAdapter;
import org.parchmentmc.feather.io.gson.SimpleVersionAdapter;
import org.parchmentmc.feather.io.gson.metadata.MetadataAdapterFactory;
import org.parchmentmc.feather.mapping.ImmutableMappingDataContainer;
import org.parchmentmc.feather.mapping.MappingDataContainer;
import org.parchmentmc.feather.mapping.VersionedMappingDataContainer;
import org.parchmentmc.feather.named.Named;
import org.parchmentmc.feather.util.SimpleVersion;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author ZZZank
 */
public class ReplaceSerdeTest {

    @Test
    public void test() throws IOException {
        Gson gsonRead = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapterFactory(new MDCGsonAdapterFactory())
            .registerTypeAdapter(SimpleVersion.class, new SimpleVersionAdapter())
            .registerTypeAdapterFactory(new MetadataAdapterFactory())
            .registerTypeAdapter(Named.class, new NamedAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();

        MappingDataContainer container;
        try (InputStream is = getClass().getResourceAsStream("/parchment-1165.json")) {
            assertNotNull(is, "parchment-example.json not found on classpath");
            container = gsonRead.fromJson(new InputStreamReader(is), VersionedMappingDataContainer.class);
        }

        Gson gsonWrite = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(new MDCGsonAdapterFactory())
            .registerTypeAdapter(SimpleVersion.class, new SimpleVersionAdapter())
            .registerTypeAdapterFactory(new MetadataAdapterFactory())
            .registerTypeAdapter(Named.class, new NamedAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            // later overrides
            .registerTypeHierarchyAdapter(MappingDataContainer.ParameterData.class, new CompressedParamAdapter())
            .create();

        try (var writer = Files.newBufferedWriter(Path.of("./param-parchment.json"))) {
            var json = gsonWrite.toJsonTree(container);
            json = json.getAsJsonObject().get("classes");
            gsonWrite.toJson(json, writer);
        }
    }

    public static class CompressedParamAdapter extends TypeAdapter<MappingDataContainer.ParameterData> {

        @Override
        public void write(JsonWriter out, MappingDataContainer.ParameterData value) throws IOException {
            var builder = new StringBuilder().append(value.getIndex());
            if (value.getName() != null) {
                builder.append(":").append(value.getName());
            }
            if (value.getJavadoc() != null) {
                builder.append(":").append(value.getJavadoc());
            }
            out.value(builder.toString());
        }

        @Override
        public MappingDataContainer.ParameterData read(JsonReader in) throws IOException {
            val split = in.nextString().split(":", 3);
            return new ImmutableMappingDataContainer.ImmutableParameterData(
                Byte.parseByte(split[0]),
                split.length > 1 ? split[1] : null,
                split.length > 2 ? split[2] : null
            );
        }
    }
}
