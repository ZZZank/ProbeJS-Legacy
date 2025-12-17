package zzzank.probejs.mixins;

import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.NativeJavaClass;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zzzank.probejs.lang.java.ClassRegistry;

@Mixin(value = {ScriptManager.class}, remap = false)
public abstract class MixinScriptManager {

    @Inject(method = "loadJavaClass", at = @At("RETURN"))
    public void pjs$captureClass(String name, boolean error, CallbackInfoReturnable<NativeJavaClass> cir) {
        val result = cir.getReturnValue();
        if (result == null) {
            return;
        }
        ClassRegistry.REGISTRY.addClass(result.getClassObject());
    }
}
