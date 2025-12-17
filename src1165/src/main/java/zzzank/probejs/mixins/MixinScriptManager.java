package zzzank.probejs.mixins;

import dev.latvian.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.NativeJavaClass;
import dev.latvian.mods.rhino.Scriptable;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zzzank.probejs.lang.java.ClassRegistry;

@Mixin(value = {ScriptManager.class}, remap = false)
public abstract class MixinScriptManager {

    @Inject(method = "loadJavaClass", at = @At("RETURN"))
    public void pjs$captureClass(Scriptable scope, Object[] args, CallbackInfoReturnable<NativeJavaClass> cir) {
        val result = cir.getReturnValue();
        if (result == null) {
            return;
        }
        ClassRegistry.REGISTRY.addClass(result.getClassObject());
    }
}
