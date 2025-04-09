package zzzank.probejs;

import zzzank.probejs.features.forge_scan.BuiltinScanners;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.config.io.JsonConfigIO;
import zzzank.probejs.utils.config.prop.ConfigProperties;
import zzzank.probejs.utils.config.serde.gson.GsonSerdeFactory;
import zzzank.probejs.utils.config.serde.gson.PatternSerde;
import zzzank.probejs.utils.config.struct.ConfigEntry;
import zzzank.probejs.utils.config.struct.ConfigRoot;
import zzzank.probejs.utils.config.struct.ConfigRootImpl;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author ZZZank
 */
public interface ProbeConfig {

    ConfigRoot INSTANCE = new ConfigRootImpl(
        ProbeJS.MOD_ID,
        HashMap::new,
        new ConfigProperties(),
        JsonConfigIO.make(ProbeJS.GSON_WRITER, io -> {
            io.registerSerdeFactory(new GsonSerdeFactory(ProbeJS.GSON));
            io.registerSerde(Pattern.class, new PatternSerde());
        })
    );

    static void refresh() {
        INSTANCE.read(ProbePaths.SETTINGS_JSON);
        INSTANCE.save(ProbePaths.SETTINGS_JSON);
    }

    ConfigEntry<Integer> configVersion = INSTANCE.define("configVersion")
        .bindDefault(3)
        .comment(String.format("""
            welcome to ProbeJS Legacy config file
            remember to use '/probejs refresh_config' to refresh your config after changing config values
            sub-entry and keys: comments->'%s', current values->'%s', default values->'%s'
            
            comments and default values are provided, but not modifiable, changes to them will not be kept
            for changing certain config value, change sub-entry whose key is '%s'""",
            JsonConfigIO.COMMENTS_KEY, JsonConfigIO.VALUE_KEY, JsonConfigIO.DEFAULT_VALUE_KEY,
            JsonConfigIO.VALUE_KEY
        ))
        .build();
    ConfigEntry<Boolean> enabled = INSTANCE.define("enabled")
        .bindDefault(true)
        .comment("""
            enable or disable ProbeJS Legacy
            note that `require()` function in script are always available""")
        .build();
    ConfigEntry<Boolean> interactive = INSTANCE.define("interactive")
        .bindReadOnly(false)
        .comment("""
            use with ProbeJS VSCode Extension.
            Disabled due to many breaking changes from KubeJS/ProbeJS from higher version""")
        .build();
    ConfigEntry<Integer> interactivePort = INSTANCE.define("interactivePort")
        .bindReadOnly(7796)
        .comment("""
            use with ProbeJS VSCode Extension.
            Disabled due to many breaking changes from KubeJS/ProbeJS from higher version""")
        .build();
    ConfigEntry<Long> modHash = INSTANCE.define("modHash")
        .bindDefault(-1L)
        .comment("""
            internal config, used for tracking mod update and modlist change""")
        .build();
    ConfigEntry<Long> registryHash = INSTANCE.define("registryHash")
        .bindDefault(-1L)
        .comment("""
            internal config, used for tracking registry change""")
        .build();
    ConfigEntry<Boolean> isolatedScopes = INSTANCE.define("isolatedScopes")
        .bindDefault(false)
        .comment("""
            isolate scripts from different script file with certain exposure,
            used for making scripts actual running situation more in line with your coding""")
        .build();
    ConfigEntry<Boolean> complete = INSTANCE.define("complete")
        .bindDefault(true)
        .comment("""
            attach all registry names of each registry type to related JS types, for better code completion
            disabling this can help with performance of your code editor
            snippets for registry names are always available, regardless of this option""")
        .build();
    ConfigEntry<Boolean> publicClassOnly = INSTANCE.define("publicClassOnly")
        .bindDefault(false)
        .comment("""
            prevent classes that are not public and not referenced from being scanned""")
        .build();
    ConfigEntry<Boolean> resolveGlobal = INSTANCE.define("resolveGlobal")
        .bindDefault(true)
        .comment("""
            resolve defined values in `global`""")
        .build();
    ConfigEntry<Integer> globalResolvingDepth = INSTANCE.define("'global' Resolving Depth")
        .bindDefault(1)
        .comment("""
            how deep should ProbeJS Legacy dive into defined values in `global`""")
        .build();
    ConfigEntry<BuiltinScanners> classScanner = INSTANCE.define("Class Scanner")
        .bindDefault(BuiltinScanners.EVENTS)
        .comment("""
            can be one of these:
            NONE -> no class scanner
            EVENTS (default) -> scan all forge event subclasses
            FULL -> scan all classes recorded by ForgeModLoader""")
        .build();
    ConfigEntry<List<String>> fullScanMods = INSTANCE.define("Mods with forced Full Scanning")
        .bindDefault(CollectUtils.ofList("minecraft"))
        .comment("""
            mods described here will have ALL their classes scanned""")
        .build();
    ConfigEntry<Boolean> dumpCustomRecipeGenerator = INSTANCE.define("dumpCustomRecipeGenerator")
        .bindDefault(false)
        .comment("""
            KubeJS will generate custom recipe creation method in recipe event, these methods only accept one Json as its arg
            enabling this will allow ProbeJS to dump syntax these JsonSerializer-based recipe creating functions""")
        .build();
    ConfigEntry<Pattern> registryObjectFilter = INSTANCE.define("Registry Object Filter")
        .bindDefault(Pattern.compile("^minecraft:.+$"))
        .comment("""
            a string regex used for filtering registry objects.
            Registry objects whose id matches this pattern will always be dumped by ProbeJS Legacy""")
        .build();
    ConfigEntry<Boolean> autoParamRename = INSTANCE.define("Rename Parameter Automatically")
        .bindDefault(true)
        .comment("""
            automatically rename `arg123`-like names into some more human readable names""")
        .build();
    ConfigEntry<Boolean> simulateOldTyping = INSTANCE.define("Simulate Old Typing")
        .bindDefault(true)
        .comment("""
            Generate `Internal` namespace to simulate old typing structure before ProbeJS Legacy 4""")
        .build();
    ConfigEntry<Boolean> fieldAsBeaning = INSTANCE.define("Field As Beaning")
        .bindDefault(true)
        .comment("""
            Convert field to getter/setter if possible, this might help beaning generation be more accurate""")
        .build();
}
