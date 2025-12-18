package zzzank.probejs.mixins;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.NativeJavaClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author ZZZank
 */
@Mixin(value = ScriptManager.class, remap = false)
public interface AccessScriptManager {

    @Accessor
    Map<String, Either<NativeJavaClass, Boolean>> getJavaClassCache();
}
