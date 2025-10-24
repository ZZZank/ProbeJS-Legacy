package zzzank.probejs.docs.assignments;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.GlobalStates;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpecialTypes implements ProbeJSPlugin {
    public static final String NAMESPACE = "Special";

    @NotNull
    public static String dot(@NotNull String name) {
        return NAMESPACE + '.' + Objects.requireNonNull(name);
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        val special = new Wrapped.Namespace(NAMESPACE);

        // We define special types regardless of script type
        // because types might be sent to other scripts
        defineLiteralTypes(special, "LangKey", GlobalStates.LANG_KEYS.get());
        defineLiteralTypes(special, "RecipeId", GlobalStates.RECIPE_IDS);
        defineLiteralTypes(special, "LootTable", GlobalStates.LOOT_TABLES);
        defineLiteralTypes(special, "RawTexture", GlobalStates.RAW_TEXTURES.get());
        defineLiteralTypes(special, "Texture", GlobalStates.TEXTURES.get());
        defineLiteralTypes(special, "Mod", GlobalStates.MODS.get());

        scriptDump.addGlobal("special_types", special);
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        defineLiteralSnippets(dump, "lang_key", GlobalStates.LANG_KEYS.get());
        defineLiteralSnippets(dump, "recipe_id", GlobalStates.RECIPE_IDS);
        defineLiteralSnippets(dump, "loot_table", GlobalStates.LOOT_TABLES);
        defineLiteralSnippets(dump, "texture", GlobalStates.TEXTURES.get());
        defineLiteralSnippets(dump, "mod", GlobalStates.MODS.get());
    }

    private static void defineLiteralTypes(Wrapped.Namespace special, String symbol, Collection<String> literals) {
        val types = literals.stream().map(Types::literal).toArray(BaseType[]::new);
        val declaration = new TypeDecl(symbol, Types.or(types));
        special.addCode(declaration);
    }

    private static void defineLiteralSnippets(SnippetDump dump, String symbol, Collection<String> literals) {
        dump.snippet(symbol)
            .prefix("@" + symbol)
            .choices(literals.stream()
                .map(ProbeJS.GSON::toJson)
                .collect(Collectors.toSet())
            );
    }
}
