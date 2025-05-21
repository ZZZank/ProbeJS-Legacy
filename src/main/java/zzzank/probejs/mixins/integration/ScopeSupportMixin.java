package zzzank.probejs.mixins.integration;

import dev.latvian.mods.kubejs.script.ScriptFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import zzzank.probejs.features.kubejs.ScriptTransformer;

/**
 * @author ZZZank
 */
@Mixin(value = ScriptFile.class, remap = false)
abstract class ScopeSupportMixin implements Comparable<ScriptFile> {

    @ModifyArg(
        method = "load",
        at = @At(
            value = "INVOKE",
            target = "Ldev/latvian/mods/rhino/Context;evaluateString(Ldev/latvian/mods/rhino/Scriptable;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/Object;"
        ),
        index = 1
    )
    public String pjs$load(String source) {
        return String.join("\n", new ScriptTransformer(source.split("\\n")).transform());
    }
}
