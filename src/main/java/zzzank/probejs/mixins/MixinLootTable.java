package zzzank.probejs.mixins;

import com.google.gson.JsonElement;
import lombok.val;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zzzank.probejs.GlobalStates;
import zzzank.probejs.ProbeConfig;

import java.util.Map;

@Mixin(LootTables.class)
public abstract class MixinLootTable {
    @Inject(method = "apply*", at = @At("RETURN"))
    public void apply(
        Map<ResourceLocation, JsonElement> object,
        ResourceManager resourceManager,
        ProfilerFiller profiler,
        CallbackInfo ci
    ) {
        if (!ProbeConfig.enabled.get()) {
            return;
        }
        for (val resourceLocation : object.keySet()) {
            GlobalStates.LOOT_TABLES.add(resourceLocation.toString());
        }
    }
}
