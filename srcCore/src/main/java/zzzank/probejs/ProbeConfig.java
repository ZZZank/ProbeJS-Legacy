package zzzank.probejs;

import com.google.gson.JsonElement;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.config.io.JsonConfigIO;
import zzzank.probejs.utils.config.io.SchemaJsonConfigIO;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.serde.gson.GsonSerdeFactory;
import zzzank.probejs.utils.config.serde.gson.PatternSerde;
import zzzank.probejs.utils.config.struct.ConfigEntry;
import zzzank.probejs.utils.config.struct.ConfigRoot;
import zzzank.probejs.utils.config.struct.ConfigRootImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author ZZZank
 */
public interface ProbeConfig {

    ConfigRoot INSTANCE = createConfigRoot();

    private static ConfigRoot createConfigRoot() {
        var configIO = new SchemaJsonConfigIO(ProbeJS.GSON_WRITER, Path.of("./config-schema.json")) {
            @Override
            protected <T> void readEntryImpl(ConfigEntry<T> entry, JsonElement json) {
                if (json.isJsonObject() && json.getAsJsonObject().has(JsonConfigIO.VALUE_KEY)) {
                    json = json.getAsJsonObject().get(JsonConfigIO.VALUE_KEY);
                }
                super.readEntryImpl(entry, json);
            }
        };
        configIO.registerSerdeFactory(new GsonSerdeFactory(ProbeJS.GSON));
        configIO.registerDirectSerdeFactory(Pattern.class, PatternSerde.INSTANCE);

        var root = new ConfigRootImpl(configIO, null);
        root.properties().put(ConfigProperty.AUTO_SAVE, true);

        return root;
    }

    ConfigEntry<Integer> configVersion = INSTANCE.define("configVersion")
        .bindDefault(4)
        .comment("""
            Welcome to ProbeJS Legacy config file
            Use '/probejs refresh_config' to refresh your config after changing config values""")
        .build();
    ConfigEntry<Boolean> enabled = INSTANCE.define("enabled")
        .bindDefault(true)
        .comment("""
            enable or disable ProbeJS Legacy
            note that `require()` function in script are always available""")
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
        .bindDefault(true)
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
    ConfigEntry<List<String>> fullScanMods = INSTANCE.define("Mods with forced Full Scanning")
        .<List<String>>bindDefault(CollectUtils.ofList("minecraft"))
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
    ConfigEntry<Boolean> fieldAsBeaning = INSTANCE.define("Field As Beaning")
        .bindDefault(true)
        .comment("""
            Convert field to getter/setter if possible, this might help beaning generation be more accurate""")
        .build();
}
