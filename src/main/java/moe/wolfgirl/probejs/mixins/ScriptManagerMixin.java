package moe.wolfgirl.probejs.mixins;

import com.google.gson.JsonNull;
import dev.latvian.kubejs.script.ScriptManager;
import moe.wolfgirl.probejs.GlobalStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {ScriptManager.class}, remap = false)
public abstract class ScriptManagerMixin {

    @Inject(method = "load", remap = false, at = @At("HEAD"))
    public void pjs$reloadStart(CallbackInfo ci) {
        if (GlobalStates.SERVER != null) {
            GlobalStates.SERVER.broadcast("clear_error", JsonNull.INSTANCE);
        }
    }
}
