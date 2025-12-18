package zzzank.probejs.mixins;

import dev.latvian.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.NativeJavaClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Optional;

/**
 * @author ZZZank
 */
@Mixin(value = ScriptManager.class, remap = false)
public interface AccessScriptManager {

    @Accessor
    Map<String, Optional<NativeJavaClass>> getJavaClassCache();
}
