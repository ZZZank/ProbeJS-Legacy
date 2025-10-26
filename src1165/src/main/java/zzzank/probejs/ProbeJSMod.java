package zzzank.probejs;

import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;

@Mod(ProbeJS.MOD_ID)
public class ProbeJSMod {

    public ProbeJSMod() {
        EventBuses.registerModEventBus(ProbeJS.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        try {
            // make sure config data is valid before any usage
            ProbeConfig.INSTANCE.read();
        } catch (IOException ignored) {
        }
    }
}
