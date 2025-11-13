package zzzank.probejs.mixins.integration;

import dev.latvian.mods.kubejs.forge.ForgeEventWrapper;
import dev.latvian.mods.rhino.NativeJavaClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author ZZZank
 */
@Mixin(value = ForgeEventWrapper.class, remap = false)
public abstract class KubeJSForgeEventRedirection {

    @ModifyVariable(
        method = "onEvent",
        at = @At("HEAD"),
        index = 1,
        argsOnly = true
    )
    private Object pjs$forgeEventRedirecting(Object value) {
        if (value instanceof NativeJavaClass clazz) {
            return clazz.getClassObject().getName();
        }
        return value;
    }

    @ModifyVariable(
        method = "onGenericEvent",
        at = @At("HEAD"),
        index = 1,
        argsOnly = true
    )
    private Object pjs$forgeGenericEventRedirecting(Object value) {
        if (value instanceof NativeJavaClass clazz) {
            return clazz.getClassObject().getName();
        }
        return value;
    }
}
