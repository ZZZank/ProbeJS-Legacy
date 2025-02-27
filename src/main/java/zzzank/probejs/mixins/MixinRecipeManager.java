package zzzank.probejs.mixins;

import com.google.gson.JsonObject;
import lombok.val;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zzzank.probejs.GlobalStates;
import zzzank.probejs.ProbeConfig;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 900)
public abstract class MixinRecipeManager {

    @Inject(method = "apply*", at = @At("HEAD"))
    private void apply(
        Map<ResourceLocation, JsonObject> map,
        ResourceManager resourceManager,
        ProfilerFiller profiler,
        CallbackInfo ci
    ) {
        if (!ProbeConfig.enabled.get()) {
            return;
        }
        for (val resourceLocation : map.keySet()) {
            if (!resourceLocation.getPath().startsWith("kjs_")) {
                GlobalStates.RECIPE_IDS.add(resourceLocation.toString());
            }
        }
    }
}
