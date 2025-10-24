package zzzank.probejs.lang.schema;

import com.google.gson.JsonObject;
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
            val pathStr = entry.getKey();
            val content = entry.getValue();

            try (val writer = Files.newBufferedWriter(path.resolve(pathStr + ".json"))) {
                val jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
                jsonWriter.setIndent("    ");
                ProbeJS.GSON_WRITER.toJson(content.getSchema(), JsonObject.class, jsonWriter);
            }
        }
    }

    public void newSchema(String path, SchemaElement<?> element) {
        schemas.put(path, element);
    }

    public ObjectElement newObjectSchema(String path) {
        val obj = ObjectElement.of();
        schemas.put(path, obj);
        return obj;
    }
}
