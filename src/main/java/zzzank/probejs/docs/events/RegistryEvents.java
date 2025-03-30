package zzzank.probejs.docs.events;

import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.NameUtils;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.util.HashSet;
import java.util.Set;

public class RegistryEvents implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.STARTUP) {
            return;
        }

        val groupNamespace = new Wrapped.Namespace("StartupEvents");

        for (val info : RegistryInfos.values()) {
            val key = info.resKey;
            val registryPath = getRegistryClassPath(key.location().getNamespace(), key.location().getPath());
            val extraName = key.location().getNamespace().equals("minecraft")
                ? key.location().getPath()
                : key.location().toString();

            val declaration = Statements.func("registry")
                .param("type", Types.literal(extraName))
                .param("handler", Types.lambda()
                    .param("event", Types.type(registryPath))
                    .build()
                )
                .build();
            groupNamespace.addCode(declaration);
        }

        scriptDump.addGlobal("registry_events", groupNamespace);
    }

//    @Override
//    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
//        if (scriptDump.scriptType != ScriptType.STARTUP) return;
//
//        for (ResourceKey<? extends Registry<?>> key : BuiltInRegistries.REGISTRY.registryKeySet()) {
//            RegistryInfo<?> info = RegistryInfo.of(RegistryUtils.castKey(key));
//            RegistryType<?> type = RegistryType.ofKey(key);
//            if (type == null) continue;
//
//            ClassPath registryPath = getRegistryClassPath(key.location().getNamespace(), key.location().getPath());
//            ClassDecl registryClass = generateRegistryClass(key, type.baseClass(), info);
//
//            TypeScriptFile registryFile = new TypeScriptFile(registryPath);
//            registryFile.addCode(registryClass);
//            globalClasses.put(registryPath, registryFile);
//        }
//
//        // Let createCustom to use Supplier<T> instead of object
//        TypeScriptFile registryEvent = globalClasses.get(ClassPath.fromJava(RegistryKubeEvent.class));
//        ClassDecl eventClass = registryEvent.findCode(ClassDecl.class).orElse(null);
//        if (eventClass == null) return;
//
//        eventClass.methods.stream()
//                .filter(method -> method.name.equals("createCustom") && method.params.size() == 2)
//                .findAny()
//                .ifPresent(method -> method.params.get(1).type = Types.lambda().returnType(Types.generic("T")).build());
//
//    }

    private static ClassPath getRegistryClassPath(String namespace, String location) {
        return ClassPath.fromRaw(String.format(
            "zzzank.probejs.generated.registry.%s.%s",
            namespace,
            NameUtils.rlToTitle(location)
        ));
    }
//
//    private static ClassDecl generateRegistryClass(ResourceKey<?> key, Class<?> baseClass, RegistryInfo<?> info) {
//        ClassDecl.Builder builder = Statements.clazz(NameUtils.rlToTitle(key.location().getPath()))
//                .superClass(Types.parameterized(Types.type(RegistryKubeEvent.class), Types.type(baseClass)));
//
//        BuilderType<?> defaultType = info.getDefaultType();
//        if (defaultType != null) {
//            builder.method("create", method -> method
//                    .returnType(Types.typeMaybeGeneric(defaultType.builderClass()))
//                    .param("name", Types.STRING));
//        }
//
//        for (BuilderType<?> type : info.getTypes()) {
//            builder.method("create", method -> method
//                    .returnType(Types.typeMaybeGeneric(type.builderClass()))
//                    .param("name", Types.STRING)
//                    .param("type", Types.literal(type.type())));
//        }
//
//        return builder.build();
//    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> classes = new HashSet<>();

        for (val info : RegistryInfos.values()) {
            val vanillaRegistry = info.raw;
            //dont use val here, it's unable to figure out the exact type
            var instance = CollectUtils.anyIn(vanillaRegistry.entrySet());
            if (instance != null) {
                classes.add(instance.getValue().getClass());
            }
        }

        return classes;
    }
}
