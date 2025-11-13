package zzzank.probejs.lang.snippet;

import com.google.gson.JsonObject;
import lombok.val;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the generation of snippets.
 */
public class SnippetDump {
    public final List<Snippet> snippets = new ArrayList<>();

    public Snippet snippet(String name) {
        val snippet = new Snippet(name);
        snippets.add(snippet);
        return snippet;
    }

    public void fromDocs() {
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.addVSCodeSnippets(this));
    }

    public void writeTo(Path path) throws IOException {
        try (val writer = Files.newBufferedWriter(path)) {
            val jsonWriter = ProbeJS.GSON_WRITER.newJsonWriter(writer);
            jsonWriter.setIndent("    ");

            val compiled = new JsonObject();
            for (val snippet : snippets) {
                compiled.add(snippet.name, snippet.compile());
            }
            ProbeJS.GSON_WRITER.toJson(compiled, JsonObject.class, jsonWriter);
        } finally {
            snippets.clear();
        }
    }
}
