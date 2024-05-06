package com.probejs.compiler.special;

import com.probejs.ProbeJS;
import com.probejs.formatter.FormatterNamespace;
import com.probejs.formatter.FormatterRaw;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public abstract class TagCompiler {

    private static Map<String, Collection<ResourceLocation>> tags;

    public static void init(Map<String, Collection<ResourceLocation>> tags) {
        TagCompiler.tags = tags;
    }

    public static List<String> format(int indent, int stepIndent) {
        List<String> lines = tags
            .entrySet()
            .stream()
            .map(e ->
                String.format(
                    "type %s = %s",
                    e.getKey(),
                    e
                        .getValue()
                        .stream()
                        .map(rl -> ProbeJS.GSON.toJson(rl.toString()))
                        .collect(Collectors.joining("|"))
                )
            )
            .collect(Collectors.toList());
        return new FormatterNamespace("Tag", new FormatterRaw(lines, false))
            .formatLines(indent, stepIndent);
    }

    public static void compile(BufferedWriter writer) throws IOException {
        for (String line : TagCompiler.format(0, 4)) {
            writer.write(line);
            writer.write('\n');
        }
        writer.write('\n');
        tags.clear();
    }
}
