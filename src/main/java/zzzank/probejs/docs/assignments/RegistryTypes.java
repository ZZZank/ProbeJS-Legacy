package zzzank.probejs.docs.assignments;

import lombok.val;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.server.ServerLifecycleHooks;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.FieldDecl;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.NameUtils;
import zzzank.probejs.utils.registry.RegistryInfo;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.util.*;

/**
 * Assign types to all the registry types
 */
public class RegistryTypes implements ProbeJSPlugin {
    public static final String LITERAL_FIELD = "probejsInternal$$Literal";
    public static final String TAG_FIELD = "probejsInternal$$Tag";
    public static final String OF_TYPE_DECL = "T extends { %s: infer U } ? U : never";

    public static final String SPECIAL_TAG_OF = SpecialTypes.dot("TagOf");
    public static final BaseType TYPE_SPECIAL_TAG_OF = Types.primitive(SPECIAL_TAG_OF);
    public static final String SPECIAL_LITERAL_OF = SpecialTypes.dot("LiteralOf");
    public static final BaseType TYPE_SPECIAL_LITERAL_OF = Types.primitive(SPECIAL_LITERAL_OF);

    @Override
    public void assignType(ScriptDump scriptDump) {
        List<BaseType> registryNames = new ArrayList<>();
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        for (val info : RegistryInfos.values()) {
            val key = info.resKey;
            val typeName = NameUtils.registryName(key);
            val assignmentType = info.assignmentType();
            if (assignmentType != null) {
                scriptDump.assignType(assignmentType, Types.primitive(SpecialTypes.dot(typeName)));
            }
            registryNames.add(Types.literal(key.location().toString()));
        }

        // ResourceKey<T> to Special.LiteralOf<T>
        scriptDump.assignType(ResourceKey.class, TYPE_SPECIAL_LITERAL_OF.withParams("T"));
        // Also holder
        scriptDump.assignType(Holder.class, TYPE_SPECIAL_LITERAL_OF.withParams("T"));
        // Registries (why?)
        scriptDump.assignType(Registry.class, Types.or(registryNames.toArray(BaseType[]::new)));
        // TagKey<T> to Special.TagOf<T>
        scriptDump.assignType(TagKey.class, TYPE_SPECIAL_TAG_OF.withParams("T"));
    }

    private static void assignRegistryType(ScriptDump scriptDump, Class<?> type, String literalType, String symbol) {
        scriptDump.assignType(type, Types.primitive(literalType).withParams(symbol));
        scriptDump.assignType(
            type,
            Types.type(type).withParams(Types.generic(symbol))
                .contextShield(BaseType.FormatType.RETURN)
        );
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        val special = new Wrapped.Namespace(SpecialTypes.NAMESPACE);
        final boolean enabled = ProbeConfig.complete.get();

        for (val info : RegistryInfos.values()) {
            createTypes(special, info, enabled);
        }
//        createTypes(special, new RegistryInfo(Registry.REGISTRY), enabled);

        // Expose LiteralOf<T> and TagOf<T>
        val literalOf = new TypeDecl("LiteralOf<T>", Types.primitive(String.format(OF_TYPE_DECL, LITERAL_FIELD)));
        val tagOf = new TypeDecl("TagOf<T>", Types.primitive(String.format(OF_TYPE_DECL, TAG_FIELD)));
        special.addCode(literalOf);
        special.addCode(tagOf);

        scriptDump.addGlobal("registry_type", special);
    }

    private static void createTypes(
        Wrapped.Namespace special,
        RegistryInfo info,
        boolean resolveAll
    ) {
        val key = info.resKey;

        val types = resolveAll
            ? Types.or(info.names.stream().map(ResourceLocation::toString).map(Types::literal).toArray(BaseType[]::new))
            : Types.STRING;
        val typeName = NameUtils.registryName(key);

        val typeDecl = new TypeDecl(typeName, types);
        special.addCode(typeDecl);

        val tagTypes = resolveAll
            ? Types.or(
            info.tagNames()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .map(Types::literal)
                .toArray(BaseType[]::new))
            : Types.STRING;
        val tagName = typeName + "Tag";

        val tagDecl = new TypeDecl(tagName, tagTypes);
        special.addCode(tagDecl);
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        // We inject literal and tag into registry types
        for (val info : RegistryInfos.values()) {
            val key = info.resKey;
            val assignmentType = info.assignmentType();
            if (assignmentType != null) {
                makeClassModifications(globalClasses, key, assignmentType);
            }
        }
        makeClassModifications(globalClasses, BuiltInRegistries.REGISTRY.key(), Registry.class);
//        makeClassModifications(globalClasses, BuiltInRegistries.DIMENSION_REGISTRY, Level.class);
    }

    private static void makeClassModifications(Map<ClassPath, TypeScriptFile> globalClasses, ResourceKey<? extends Registry<?>> key, Class<?> baseClass) {
        val typeScriptFile = globalClasses.get(ClassPath.fromJava(baseClass));
        if (typeScriptFile == null) {
            return;
        }
        val classDecl = typeScriptFile.findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) {
            return;
        }

        val typeName = NameUtils.registryName(key);
        val tagName = typeName + "Tag";

        val literalField = new FieldDecl(LITERAL_FIELD, Types.primitive(String.format("Special.%s", typeName)));
        literalField.addComment("This field is a type stub generated by ProbeJS and shall not be used in any sense.");
        classDecl.bodyCode.add(literalField);
        val tagField = new FieldDecl(TAG_FIELD, Types.primitive(String.format("Special.%s", tagName)));
        tagField.addComment("This field is a type stub generated by ProbeJS and shall not be used in any sense.");
        classDecl.bodyCode.add(tagField);
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return Collections.emptySet();
        }

        val filter = ProbeConfig.registryObjectFilter.get();

        val classes = new HashSet<Class<?>>();
        for (val info : RegistryInfos.values()) {
            for (var entry : info.entries()) { //don't use val, lombok is not smart enough to infer types here
                val location = entry.getKey().location().toString();
                if (filter.matcher(location).matches()) {
                    classes.add(entry.getValue().getClass());
                }
            }
        }
        return classes;
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        for (val info : RegistryInfos.values()) {
            val registry = info.raw;
            val key = info.resKey;
            if (registry == null) {
                continue;
            }

            val entries = CollectUtils.mapToList(
                registry.keySet(),
                ResourceLocation::toString
            );
            if (entries.isEmpty()) {
                continue;
            }

            val registryName = "minecraft".equals(key.location().getNamespace())
                ? key.location().getPath()
                : key.location().toString();

            val registrySnippet = dump.snippet("probejs$$" + key.location());
            registrySnippet.prefix(String.format("@%s", registryName))
                .description(String.format("All available items in the registry \"%s\"", key.location()))
                .literal("\"")
                .choices(entries)
                .literal("\"");

            val tags = info.tagNames()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .map("#"::concat)
                .toList();
            if (tags.isEmpty()) {
                continue;
            }

            val tagSnippet = dump.snippet("probejs_tag$$" + key.location());
            tagSnippet.prefix(String.format("@%s_tag", registryName))
                .description(String.format("All available tags in the registry \"%s\", no # is added", key.location()))
                .literal("\"")
                .choices(tags)
                .literal("\"");
        }
    }
}
