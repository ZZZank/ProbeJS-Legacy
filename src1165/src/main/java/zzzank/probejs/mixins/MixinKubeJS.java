package zzzank.probejs.mixins;

import dev.latvian.kubejs.KubeJS;
import me.shedaniel.architectury.platform.Mod;
import me.shedaniel.architectury.platform.Platform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fix KubeJS loading plugin multiple times when multiple mods are declared within one mod file
 *
 * @author ZZZank
 */
@Mixin(value = KubeJS.class, remap = false)
public abstract class MixinKubeJS {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lme/shedaniel/architectury/platform/Platform;getMods()Ljava/util/Collection;"), require = 0)
    private static Collection<Mod> deduplicateModFile() {
        return Platform.getMods()
            .stream()
            .collect(Collectors.toMap(
                Mod::getFilePath,
                Function.identity(),
                (a, b) -> a,
                LinkedHashMap::new
            ))
            .values();
    }
}
