package zzzank.probejs.features.forge_scan;

import lombok.val;
import me.shedaniel.architectury.platform.Mod;
import me.shedaniel.architectury.platform.Platform;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.utils.ReflectUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
                    .flatMap(data -> data.getClasses().stream())
            )
            .stream()
            .map(ReflectUtils::classOrNull)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static @NotNull List<Class<?>> scanMods(Collection<String> modids) {
        return modids.stream()
            .map(Platform::getMod)
            .map(Mod::getFilePath)
            .map(Path::toFile)
            .map(ModJarClassScanner::scanFile)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
