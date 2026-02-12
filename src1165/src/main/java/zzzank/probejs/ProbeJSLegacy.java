package zzzank.probejs;

import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import zzzank.probejs.features.forge_scan.BuiltinScanners;
import zzzank.probejs.utils.ProbeText;
import zzzank.probejs.utils.config.struct.ConfigEntry;
import zzzank.probejs.utils.config.struct.ConfigRootImpl;

import java.io.IOException;

@Mod(ProbeJS.MOD_ID)
public class ProbeJSLegacy {

    public static final ConfigEntry<BuiltinScanners> CONFIG_CLASS_SCANNER = ProbeConfig.INSTANCE.define("Class Scanner")
        .bindDefault(BuiltinScanners.EVENTS)
        .comment("""
            can be one of these:
            NONE -> no class scanner
            EVENTS (default) -> scan all forge event subclasses
            FULL -> scan all classes recorded by ForgeModLoader""")
        .buildAutoSave();

    public ProbeJSLegacy() {
        EventBuses.registerModEventBus(ProbeJS.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        BuiltinScanners.PREDEFINED_BASECLASS.add(Event.class.getName());

        ((ConfigRootImpl) ProbeConfig.INSTANCE).setFilePath(ProbePaths.SETTINGS_JSON);
        try {
            // make sure config data is valid before any usage
            ProbeConfig.INSTANCE.read();
        } catch (IOException ignored) {
        }
    }

    public static ProbeText refreshConfig() {
        try {
            ProbeConfig.INSTANCE.read();
            ProbeConfig.INSTANCE.save();
            return ProbeText.pjs("config_refreshed");
        } catch (IOException e) {
            ProbeJS.LOGGER.error("Unable to refresh config", e);
            return ProbeText.literal("Unable to refresh config: " + e);
        }
    }
}
