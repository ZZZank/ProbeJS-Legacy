package com.probejs.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.probejs.ProbeConfig;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.formatter.NameResolver;
import dev.latvian.kubejs.KubeJSPaths;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnippetCompiler {

    public static class KubeDump {

        public final Map<String, Map<String, List<String>>> tags;
        public final Map<String, List<String>> registries;

        public KubeDump(Map<String, Map<String, List<String>>> tags, Map<String, List<String>> registries) {
            this.tags = tags;
            this.registries = registries;
        }

        @Override
        public String toString() {
            return "KubeDump{" + "tags=" + tags + ", registries=" + registries + '}';
        }

        public JsonObject toSnippet() {
            JsonObject resultJson = new JsonObject();
            // Compile normal entries to snippet
            for (Map.Entry<String, List<String>> entry : this.registries.entrySet()) {
                String type = entry.getKey();
                Map<String, List<String>> byModMembers = new HashMap<>();
                entry
                    .getValue()
                    .stream()
                    .map(rl -> rl.split(":", 2))
                    .forEach(rl -> byModMembers.computeIfAbsent(rl[0], k -> new ArrayList<>()).add(rl[1]));
                byModMembers.forEach((mod, modMembers) -> {
                    JsonObject modMembersJson = new JsonObject();
                    JsonArray prefixes = new JsonArray();
                    if (ProbeConfig.INSTANCE.vanillaOrder) {
                        prefixes.add(String.format("@%s.%s", mod, type));
                    } else {
                        prefixes.add(String.format("@%s.%s", type, mod));
                    }
                    modMembersJson.add("prefix", prefixes);
                    modMembersJson.addProperty(
                        "body",
                        String.format("\"%s:${1|%s|}\"", mod, String.join(",", modMembers))
                    );
                    resultJson.add(String.format("%s_%s", type, mod), modMembersJson);
                });
            }

            // Compile tag entries to snippet
            for (Map.Entry<String, Map<String, List<String>>> entry : this.tags.entrySet()) {
                String type = entry.getKey();
                Map<String, List<String>> byModMembers = new HashMap<>();
                entry
                    .getValue()
                    .keySet()
                    .stream()
                    .map(rl -> rl.split(":", 2))
                    .forEach(rl -> byModMembers.computeIfAbsent(rl[0], k -> new ArrayList<>()).add(rl[1]));
                byModMembers.forEach((mod, modMembers) -> {
                    JsonObject modMembersJson = new JsonObject();
                    JsonArray prefixes = new JsonArray();
                    if (ProbeConfig.INSTANCE.vanillaOrder) {
                        prefixes.add(String.format("@%s.tags.%s", mod, type));
                    } else {
                        prefixes.add(String.format("@%s.tags.%s", type, mod));
                    }
                    modMembersJson.add("prefix", prefixes);
                    modMembersJson.addProperty(
                        "body",
                        String.format("\"#%s:${1|%s|}\"", mod, String.join(",", modMembers))
                    );
                    resultJson.add(String.format("%s_tag_%s", type, mod), modMembersJson);
                });
            }

            return resultJson;
        }
    }

    public static void compile() throws IOException {
        if (ProbeConfig.INSTANCE.exportClassNames) {
            compileClassNames();
        }
        writeDumpSnippets();
    }

    private static void writeDumpSnippets() throws IOException {
        Path kubePath = KubeJSPaths.EXPORTED.resolve("kubejs-server-export.json");
        if (!kubePath.toFile().canRead()) {
            ProbeJS.LOGGER.error("'kubejs-server-export.json' not found!");
            return;
        }
        Path codeFile = ProbePaths.SNIPPET.resolve("probe.code-snippets");
        Reader reader = Files.newBufferedReader(kubePath);
        KubeDump kubeDump = ProbeJS.GSON.fromJson(reader, KubeDump.class);

        BufferedWriter writer = Files.newBufferedWriter(codeFile);
        writer.write(ProbeJS.GSON.toJson(kubeDump.toSnippet()));
        writer.flush();
        writer.close();
    }

    private static void compileClassNames() throws IOException {
        JsonObject resultJson = new JsonObject();
        for (Map.Entry<String, NameResolver.ResolvedName> entry : NameResolver.resolvedNames.entrySet()) {
            String className = entry.getKey();
            NameResolver.ResolvedName resolvedName = entry.getValue();
            JsonObject classJson = new JsonObject();
            JsonArray prefix = new JsonArray();
            prefix.add(String.format("!%s", resolvedName.getFullName()));
            classJson.add("prefix", prefix);
            classJson.addProperty("body", className);
            resultJson.add(resolvedName.getFullName(), classJson);
        }

        Path codeFile = ProbePaths.SNIPPET.resolve("classNames.code-snippets");
        BufferedWriter writer = Files.newBufferedWriter(codeFile);
        ProbeJS.GSON.toJson(resultJson, writer);
        writer.flush();
    }
}
