package zzzank.probejs.docs;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.features.SpecialData;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.snippet.parts.Variable;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.util.Arrays;

public class Snippets implements ProbeJSPlugin {
    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        dump.snippet("uuid")
            .prefix("#uuid")
            .description("Generates a random version 4 UUID.")
            .literal("\"")
            .variable(Variable.UUID)
            .literal("\"");

        defineHeader(dump, "priority", "0");
        defineHeader(dump, "packmode", null);

        dump.snippet("ignored")
            .prefix("#ignored")
            .description("Creates the file header for `ignored`.")
            .literal("// ignored: ")
            .choices(Arrays.asList("true", "false"));

        dump.snippet("requires")
            .prefix("#requires")
            .description("Creates the file header for `requires`.")
            .literal("// requires: ")
            .choices(SpecialData.MODS.get());

        dump.snippet("itemstack")
            .prefix("#itemstack")
            .description("Creates a `nx item` string.")
            .literal("\"")
            .tabStop(1, "1")
            .literal("x ")
            .choices(RegistryInfos.get(Registry.ITEM_REGISTRY).objectIds().map(ResourceLocation::toString))
            .literal("\"");
    }

    private static void defineHeader(SnippetDump dump, String symbol, String defaultValue) {
        dump.snippet(symbol)
            .prefix("#" + symbol)
            .description(String.format("Creates the file header for `%s`.", symbol))
            .literal(String.format("// %s: ", symbol))
            .tabStop(0, defaultValue);
    }
}
