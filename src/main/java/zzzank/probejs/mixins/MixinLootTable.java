package zzzank.probejs.mixins;

import lombok.val;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zzzank.probejs.GlobalStates;
import zzzank.probejs.ProbeConfig;

import java.util.Map;

@Mixin(LootDataManager.class)
public abstract class MixinLootTable {
    @Inject(method = "apply*", at = @At("RETURN"))
    public void apply(
        Map<LootDataType<?>, Map<ResourceLocation, ?>> collectedElements, CallbackInfo ci
    ) {
        if (!ProbeConfig.enabled.get()) {
            return;
        }
        for (val resourceLocations : collectedElements.values()) {
            for (val resourceLocation : resourceLocations.keySet()) {
                GlobalStates.LOOT_TABLES.add(resourceLocations.toString());
            }
        }
    }
}
