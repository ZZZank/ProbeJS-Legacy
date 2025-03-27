package zzzank.probejs;

import zzzank.probejs.features.forge_scan.BuiltinScanners;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.config.ConfigEntry;
import zzzank.probejs.utils.config.ConfigImpl;
import zzzank.probejs.utils.config.io.JsonConfigIO;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author ZZZank
 */
public interface ProbeConfig {

    ConfigImpl INSTANCE = new ConfigImpl(ProbePaths.SETTINGS_JSON, ProbeJS.MOD_ID);

    static void refresh() {
        INSTANCE.readFromFile();
        INSTANCE.save();
    }

    ConfigEntry<Integer> configVersion = INSTANCE.define("configVersion")
        .setDefault(3)
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
        .setDefault(true)
        .comment("""
            enable or disable ProbeJS Legacy
            note that `require()` function in script are always available""")
        .build();
    ConfigEntry<Boolean> interactive = INSTANCE.define("interactive")
        .readOnly(false)
        .comment("""
            use with ProbeJS VSCode Extension.
            Disabled due to many breaking changes from KubeJS/ProbeJS from higher version""")
        .build();
    ConfigEntry<Integer> interactivePort = INSTANCE.define("interactivePort")
        .readOnly(7796)
        .comment("""
            use with ProbeJS VSCode Extension.
            Disabled due to many breaking changes from KubeJS/ProbeJS from higher version""")
        .build();
    ConfigEntry<Long> modHash = INSTANCE.define("modHash")
        .setDefault(-1L)
        .comment("""
            internal config, used for tracking mod update and modlist change""")
        .build();
    ConfigEntry<Long> registryHash = INSTANCE.define("registryHash")
        .setDefault(-1L)
        .comment("""
            internal config, used for tracking registry change""")
        .build();
    ConfigEntry<Boolean> isolatedScopes = INSTANCE.define("isolatedScopes")
        .setDefault(false)
        .comment("""
            isolate scripts from different script file with certain exposure,
            used for making scripts actual running situation more in line with your coding""")
        .build();
    ConfigEntry<Boolean> complete = INSTANCE.define("complete")
        .setDefault(true)
        .comment("""
            attach all registry names of each registry type to related JS types, for better code completion
            disabling this can help with performance of your code editor
            snippets for registry names are always available, regardless of this option""")
        .build();
    ConfigEntry<Boolean> publicClassOnly = INSTANCE.define("publicClassOnly")
        .setDefault(false)
        .comment("""
            prevent classes that are not public and not referenced from being scanned""")
        .build();
    ConfigEntry<Boolean> resolveGlobal = INSTANCE.define("resolveGlobal")
        .setDefault(true)
        .comment("""
            resolve defined values in `global`""")
        .build();
    ConfigEntry<Integer> globalResolvingDepth = INSTANCE.define("'global' Resolving Depth")
        .setDefault(1)
        .comment("""
            how deep should ProbeJS Legacy dive into defined values in `global`""")
        .build();
    ConfigEntry<BuiltinScanners> classScanner = INSTANCE.define("Class Scanner")
        .setDefault(BuiltinScanners.EVENTS)
        .comment("""
            can be one of these:
            NONE -> no class scanner
            EVENTS (default) -> scan all forge event subclasses
            FULL -> scan all classes recorded by ForgeModLoader""")
        .build();
    ConfigEntry<List<String>> fullScanMods = INSTANCE.define("Mods with forced Full Scanning")
        .setDefault(CollectUtils.ofList("minecraft"))
        .comment("""
            mods described here will have ALL their classes scanned""")
        .build();
    ConfigEntry<Boolean> dumpCustomRecipeGenerator = INSTANCE.define("dumpCustomRecipeGenerator")
        .setDefault(false)
        .comment("""
            KubeJS will generate custom recipe creation method in recipe event, these methods only accept one Json as its arg
            enabling this will allow ProbeJS to dump syntax these JsonSerializer-based recipe creating functions""")
        .build();
    ConfigEntry<Pattern> registryObjectFilter = INSTANCE.define("Registry Object Filter")
        .setDefault(Pattern.compile("^minecraft:.+$"))
        .comment("""
            a string regex used for filtering registry objects.
            Registry objects whose id matches this pattern will always be dumped by ProbeJS Legacy""")
        .build();
    ConfigEntry<Boolean> autoParamRename = INSTANCE.define("Rename Parameter Automatically")
        .setDefault(true)
        .comment("""
            automatically rename `arg123`-like names into some more human readable names""")
        .build();
    ConfigEntry<Boolean> simulateOldTyping = INSTANCE.define("Simulate Old Typing")
        .setDefault(true)
        .comment("""
            Generate `Internal` namespace to simulate old typing structure before ProbeJS Legacy 4""")
        .build();
    ConfigEntry<Boolean> fieldAsBean = INSTANCE.define("Field As Bean")
        .setDefault(true)
        .comment("""
            Convert field to getter/setter if possible, this might help beaning generation be more accurate""")
        .build();
}
