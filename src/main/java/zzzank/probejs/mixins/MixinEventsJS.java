package zzzank.probejs.mixins;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.EventsJS;
import dev.latvian.mods.kubejs.script.ScriptManager;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.features.kubejs.EventJSInfo;
import zzzank.probejs.features.kubejs.EventJSInfos;

import java.util.List;

/**
 * @author ZZZank
 */
@Mixin(value = EventsJS.class, remap = false)
public abstract class MixinEventsJS {

    @Shadow
    @Final
    public ScriptManager scriptManager;

    @Inject(method = "postToHandlers", at = @At("HEAD"))
    public void pjs$captureEvent(String id, List<?> handlers, EventJS event, CallbackInfoReturnable<Boolean> cir) {
        if (!ProbeConfig.enabled.get()) {
            return;
        }
        val type = this.scriptManager.type;
        val info = EventJSInfos.KNOWN.get(id);
        if (info == null) {
            EventJSInfos.KNOWN.put(id, new EventJSInfo(type, event, ""));
        } else {
            info.scriptTypes().add(type);
        }
    }
}
