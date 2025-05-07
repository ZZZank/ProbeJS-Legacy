package zzzank.probejs.docs.assignments;

import lombok.val;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.snippet.SnippetDump;
import zzzank.probejs.lang.typescript.dump.TypeSpecificFiles;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.FieldDecl;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.NameUtils;
import zzzank.probejs.utils.registry.RegistryInfo;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Assign types to all the registry types
 */
public class RegistryTypes implements ProbeJSPlugin {
    public static final String LITERAL_FIELD = "probejsInternal$$Literal";
    public static final String TAG_FIELD = "probejsInternal$$Tag";
    public static final String OF_TYPE_DECL = "T extends { %s: infer U } ? U : never";

    public static final String SPECIAL_TAG_OF = SpecialTypes.dot("TagOf");
    public static final String SPECIAL_LITERAL_OF = SpecialTypes.dot("LiteralOf");

    @Override
    public void assignType(ScriptDump scriptDump) {
        List<BaseType> registryNames = new ArrayList<>();
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        for (val info : RegistryInfos.values()) {
            val key = info.resourceKey();
            val typeName = NameUtils.registryName(key);
            scriptDump.assignType(
                info.objectBaseType(),
                Types.primitive(SpecialTypes.dot(typeName))
            );
            registryNames.add(Types.literal(key.location().toString()));
        }

        // ResourceKey<T> to Special.LiteralOf<T>
        scriptDump.assignType(
            ResourceKey.class,
            Types.primitive(SPECIAL_LITERAL_OF).withParams("T")
        );
        //Registries (why?)
        scriptDump.assignType(Registry.class, Types.or(registryNames.toArray(new BaseType[0])));
        assignRegistryType(scriptDump, ResourceKey.class, SPECIAL_LITERAL_OF, "T");
        //TagKey<T> to Special.TagOf<T>
//        scriptDump.assignType(Tag.class, Types.parameterized(Types.primitive("Special.TagOf"), Types.generic("T")));
        assignRegistryType(scriptDump, Tag.class, SPECIAL_TAG_OF, "T");

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
        val key = info.resourceKey();

        val types = resolveAll
            ? Types.or(info.objectIds().map(ResourceLocation::toString).map(Types::literal).toArray(BaseType[]::new))
            : Types.STRING;
        val typeName = NameUtils.registryName(key);

        val typeDecl = new TypeDecl(typeName, types);
        special.addCode(typeDecl);

        val tagNames = info.tagIds()
            .map(ResourceLocation::toString)
            .map(Types::literal)
            .toArray(BaseType[]::new);

        val tagTypes = resolveAll ? Types.or(tagNames) : Types.STRING;
        val tagName = typeName + "Tag";

        val tagDecl = new TypeDecl(tagName, tagTypes);
        special.addCode(tagDecl);
    }

    public void modifyFiles(TypeSpecificFiles files) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        // We inject literal and tag into registry types
        for (val info : RegistryInfos.values()) {
            val key = info.resourceKey();
            makeClassModifications(files, key, info.objectBaseType());
        }
        makeClassModifications(files, Registry.REGISTRY.key(), Registry.class);
        makeClassModifications(files, Registry.DIMENSION_REGISTRY, Level.class);
    }

    private static void makeClassModifications(TypeSpecificFiles files, ResourceKey<? extends Registry<?>> key, Class<?> baseClass) {
        val typeScriptFile = files.globalFiles().get(ClassPath.fromJava(baseClass));
        if (typeScriptFile == null) {
            return;
        }
        val classDecl = typeScriptFile.findCodeNullable(ClassDecl.class);
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

        files.markRequested(typeScriptFile.path);
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return Collections.emptySet();
        }

        val filter = ProbeConfig.registryObjectFilter.get();

        val classes = new HashSet<Class<?>>();
        for (val info : RegistryInfos.values()) {

            classes.add(info.objectBaseType());

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
            val key = info.resourceKey();

            val entries =info.objectIds()
                .map(ResourceLocation::toString)
                .collect(Collectors.toList());
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

            val tags = info.tagIds()
                .map(ResourceLocation::toString)
                .map("#"::concat)
                .collect(Collectors.toList());
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
