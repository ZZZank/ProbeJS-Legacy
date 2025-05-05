package zzzank.probejs.docs.events;

import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.server.TagEventJS;
import lombok.val;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.NameUtils;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.util.*;

public class TagEvents implements ProbeJSPlugin {
    public static final Set<String> POSTED = new HashSet<>();

    public static final ClassPath TAG_EVENT = ClassPath.fromRaw("zzzank.probejs.generated.TagEventProbe");
    public static final ClassPath TAG_WRAPPER = ClassPath.fromRaw("zzzank.probejs.generated.TagWrapperProbe");

    // Create TagEventProbe<T, I> and TagWrapperProbe<T, I>
    // Generate string overrides for all registry types
    // tags(extra: "item", handler: (event: TagEventProbe<Special.ItemTag, Special.Item>) => void)

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.SERVER) {
            return;
        }

        val eventType = Types.type(TAG_EVENT);
        val server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        val events = new ArrayList<Code>();
        for (val info : RegistryInfos.values()) {
            val forgeRegistry = info.forgeRaw;
            val tagFolder = forgeRegistry.getTagFolder();
            if (tagFolder == null) {
                continue;
            }

        }

        val groupNamespace = new Wrapped.Namespace("ServerEvents");
        for (val info : RegistryInfos.values()) {
            val key = info.resourceKey();

            val typeName = "Special." + NameUtils.rlToTitle(key.location().getPath());
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
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (scriptDump.scriptType != ScriptType.SERVER) {
            return;
        }

        val wrapperType = Types.type(TAG_WRAPPER);

        val genericT = Types.generic("T");
        val genericI = Types.generic("I");

        val tagEventProbe = Statements.clazz(TAG_EVENT.getName())
            .superClass(Types.type(TagEventJS.class))
            .typeVariables("T", "I")
            .method("add", builder -> builder
                .returnType(wrapperType.withParams(genericT, genericI))
                .param("tag", genericT)
                .param("filters", genericI.asArray(), false, true)
            )
            .method("remove", builder -> builder
                .returnType(wrapperType.withParams(genericT, genericI))
                .param("tag", genericT)
                .param("filters", genericI.asArray(), false, true)
            )
            .build();
        val eventFile = new TypeScriptFile(TAG_EVENT);
        eventFile.addCode(tagEventProbe);
        globalClasses.put(TAG_EVENT, eventFile);

        val tagWrapperProbe = Statements.clazz(TAG_WRAPPER.getName())
            .superClass(Types.type(TagEventJS.TagWrapper.class))
            .typeVariables("T", "I")
            .method("add", builder -> builder
                .returnType(Types.THIS)
                .param("filters", genericI.asArray(), false, true)
            )
            .method("remove", builder -> builder
                .returnType(Types.THIS)
                .param("filters", genericI.asArray(), false, true)
            )
            .build();
        val wrapperFile = new TypeScriptFile(TAG_WRAPPER);
        wrapperFile.addCode(tagWrapperProbe);
        globalClasses.put(TAG_WRAPPER, wrapperFile);
    }

    @Override
    public Set<String> disableEventDumps(ScriptDump dump) {
        return POSTED;
    }
}
