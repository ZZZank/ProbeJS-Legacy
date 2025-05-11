package zzzank.probejs.features.forge_scan;

import lombok.val;
import me.shedaniel.architectury.platform.Platform;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.ReflectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author ZZZank
 */
public class ClassScanner {

    public static List<Class<?>> scanForge() {
        val allScanData = ModList.get().getAllScanData();
        val scanner = ProbeConfig.classScanner.get();
        return scanner.scan(
                allScanData
                    .stream()
                    .map(ModFileScanData::getClasses)
                    .flatMap(Collection::stream)
            )
            .stream()
            .<Class<?>>map(ReflectUtils::classOrNull)
            .filter(Objects::nonNull)
            .toList();
    }

    public static @NotNull List<Class<?>> scanMods(Collection<String> modids) {
        return modids.stream()
            .map(Platform::getMod)
            .map(mod -> {
                try {
                    return mod.getFilePath().toFile();
                } catch (Exception e) {
                    ProbeJS.LOGGER.error("unable to locate file for mod '{}'", mod.getModId(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .map(ModJarClassScanner::scanFile)
            .flatMap(Collection::stream)
            .toList();
    }
}
