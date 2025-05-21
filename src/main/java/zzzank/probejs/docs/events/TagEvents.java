package zzzank.probejs.docs.events;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.tag.TagEventJS;
import dev.latvian.mods.kubejs.server.tag.TagWrapper;
import lombok.val;
import net.minecraftforge.server.ServerLifecycleHooks;
import zzzank.probejs.docs.assignments.SpecialTypes;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.NameUtils;

public class TagEvents implements ProbeJSPlugin {
    public static final ClassPath TAG_EVENT = ClassPath.fromRaw("moe.wolfgirl.probejs.generated.TagEventProbe");
    public static final ClassPath TAG_WRAPPER = ClassPath.fromRaw("moe.wolfgirl.probejs.generated.TagWrapperProbe");

    // Create TagEventProbe<T, I> and TagWrapperProbe<T, I>
    // Generate string overrides for all registry types
    // tags(extra: "item", handler: (event: TagEventProbe<Special.ItemTag, Special.Item>) => void)

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.SERVER) {
            return;
        }

        val eventType = Types.type(TAG_EVENT);
        val currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            return;
        }
        val registryAccess = currentServer.registryAccess();

        val groupNamespace = new Wrapped.Namespace("ServerEvents");
        for (val key : RegistryInfo.MAP.keySet()) {
            val registry = registryAccess.registry(key).orElse(null);
            if (registry == null) {
                continue;
            }

            val typeName = SpecialTypes.dot(NameUtils.registryName(key));
            val tagName = typeName + "Tag";
            val extraName = key.location().getNamespace().equals("minecraft")
                ? key.location().getPath()
                : key.location().toString();
            val declaration = Statements.func("tags")
                .param("extra", Types.literal(extraName))
                .param("handler", Types.lambda()
                    .param("event", eventType.withParams(tagName, typeName))
                    .build()
                )
                .build();
            groupNamespace.addCode(declaration);
        }

        scriptDump.addGlobal("tag_events", groupNamespace);
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        val scriptDump = files.scriptDump();
        if (scriptDump.scriptType != ScriptType.SERVER) {
            return;
        }

        val wrapperType = Types.type(TAG_WRAPPER);

        val tagEventProbe = Statements.clazz(TAG_EVENT.getName())
            .superClass(Types.type(TagEventJS.class))
            .typeVariables("T", "I")
            .method(
                "add", builder -> builder
                    .returnType(Types.parameterized(wrapperType, Types.generic("T"), Types.generic("I")))
                    .param("tag", Types.generic("T"))
                    .param("filters", Types.generic("I").asArray(), false, true)
            )
            .method(
                "remove", builder -> builder
                    .returnType(Types.parameterized(wrapperType, Types.generic("T"), Types.generic("I")))
                    .param("tag", Types.generic("T"))
                    .param("filters", Types.generic("I").asArray(), false, true)
            )
            .build();
        files.requestOrCreate(TAG_EVENT).addCodes(tagEventProbe);

        val tagWrapperProbe = Statements.clazz(TAG_WRAPPER.getName())
            .superClass(Types.type(TagWrapper.class))
            .typeVariables("T", "I")
            .method(
                "add", builder -> builder
                    .returnType(Types.THIS)
                    .param("filters", Types.generic("I").asArray(), false, true)
            )
            .method(
                "remove", builder -> builder
                    .returnType(Types.THIS)
                    .param("filters", Types.generic("I").asArray(), false, true)
            )
            .build();
        files.requestOrCreate(TAG_EVENT).addCodes(tagWrapperProbe);
    }
}
