package zzzank.probejs.lang.schema;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import lombok.val;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SchemaDump {
    public Map<String, SchemaElement<?>> schemas = new HashMap<>();

    public void fromDocs() {
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.addJsonSchema(this));
    }

    public void writeTo(Path path) throws IOException {
        for (val entry : schemas.entrySet()) {
            String key = entry.getKey();
            SchemaElement<?> content = entry.getValue();

            try (var writer = Files.newBufferedWriter(path.resolve(key + ".json"))) {
                JsonWriter jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
                jsonWriter.setIndent("    ");
                ProbeJS.GSON_WRITER.toJson(content.getSchema(), JsonObject.class, jsonWriter);
            }
        }
    }

    public void newSchema(String name, SchemaElement<?> element) {
        schemas.put(name, element);
    }
}
