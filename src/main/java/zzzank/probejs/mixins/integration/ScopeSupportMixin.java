package zzzank.probejs.mixins.integration;

import dev.latvian.mods.kubejs.script.ScriptFile;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zzzank.probejs.features.kubejs.ScriptTransformer;

/**
 * @author ZZZank
 */
@Mixin(value = ScriptFile.class, remap = false)
abstract class ScopeSupportMixin implements Comparable<ScriptFile> {

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/rhino/Context;evaluateString(Ldev/latvian/mods/rhino/Scriptable;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/Object;"))
    public Object preEvalProcess(
        Context instance,
        Scriptable scope,
        String source,
        String sourceName,
        int lineno,
        Object securityDomain
    ) {
        return ScriptTransformer.transformedScriptEval(instance, scope, source, sourceName, lineno, securityDomain);
    }
}
