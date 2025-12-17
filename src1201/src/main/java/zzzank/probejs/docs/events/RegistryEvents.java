package zzzank.probejs.docs.events;

import dev.latvian.mods.kubejs.registry.BuilderType;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.val;
import net.minecraft.resources.ResourceKey;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.NameUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class RegistryEvents implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.STARTUP) {
            return;
        }

        val groupNamespace = new Wrapped.Namespace("StartupEvents");
        for (val entry : RegistryInfo.MAP.entrySet()) {
            val key = entry.getKey();

            val registryPath = getRegistryClassPath(key.location().getNamespace(), key.location().getPath());
            val extraName = key.location().getNamespace().equals("minecraft") ?
                key.location().getPath() :
                key.location().toString();

            val declaration = Statements.func("registry")
                .param("extra", Types.literal(extraName))
                .param(
                    "handler", Types.lambda()
                        .param("event", Types.type(registryPath))
                        .build()
                )
                .build();
            groupNamespace.addCode(declaration);
        }

        scriptDump.addGlobal("registry_events", groupNamespace);
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        val scriptDump = files.scriptDump();
        if (scriptDump.scriptType != ScriptType.STARTUP) {
            return;
        }

        for (val entry : RegistryInfo.MAP.entrySet()) {
            val key = entry.getKey();
            val info = entry.getValue();

            val registryPath = getRegistryClassPath(key.location().getNamespace(), key.location().getPath());
            val registryClass = generateRegistryClass(key, info);

            files.requestOrCreate(registryPath).addCodes(registryClass);
        }

        // Let createCustom to use Supplier<T> instead of object
        val registryEvent = files.request(ClassPath.ofJava(RegistryEventJS.class));
        val eventClass = registryEvent.findCode(ClassDecl.class).orElse(null);
        if (eventClass == null) {
            return;
        }

        eventClass.methods.stream()
            .filter(method -> method.name.equals("createCustom") && method.params.size() == 2)
            .findAny()
            .ifPresent(method -> method.params.get(1).type = Types.lambda().returnType(Types.generic("T")).build());
    }

    private static ClassPath getRegistryClassPath(String namespace, String location) {
        return ClassPath.ofArtificial("zzzank.probejs.generated.registry.%s.%s".formatted(
            namespace,
            NameUtils.rlToTitle(location)
        ));
    }

    private static ClassDecl generateRegistryClass(ResourceKey<?> key, RegistryInfo<?> info) {
        val builder = Statements.clazz(NameUtils.rlToTitle(key.location().getPath()))
            .superClass(Types.parameterized(Types.type(RegistryEventJS.class), Types.type(info.objectBaseClass)));

        for (val entry : info.types.entrySet()) {
            val extra = entry.getKey();
            val type = entry.getValue();

            if (extra.equals("basic")) {
                builder.method(
                    "create", method -> method
                        .returnType(Types.type(type.builderClass()))
                        .param("name", Types.STRING)
                );
            }

            builder.method(
                "create", method -> method
                    .returnType(Types.type(type.builderClass()))
                    .param("name", Types.STRING)
                    .param("type", Types.literal(extra))
            );
        }

        return builder.build();
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return RegistryInfo.MAP.values()
            .stream()
            .map(value -> value.types.values())
            .flatMap(Collection::stream)
            .map(BuilderType::builderClass)
            .collect(Collectors.toSet());
    }
}
