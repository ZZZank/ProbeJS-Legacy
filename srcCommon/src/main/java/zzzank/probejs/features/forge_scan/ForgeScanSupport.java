package zzzank.probejs.features.forge_scan;

import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import zzzank.probejs.utils.ReflectUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface ForgeScanSupport {
    default List<Class<?>> scanForge(BuiltinScanners scanner) {
        return scanner.scan(
                this,
                getAllScanData()
                    .stream()
                    .map(ModFileScanData::getClasses)
                    .flatMap(Collection::stream)
            )
            .filter(ReflectUtils.NOT_ARTIFICIAL_CLASS)
            .<Class<?>>map(ReflectUtils::classOrNull)
            .filter(Objects::nonNull)
            .toList();
    }

    default List<Class<?>> scanMods(Collection<String> modIds) {
        return modIds.stream()
            .map(this::getModFiles)
            .flatMap(Collection::stream)
            .distinct()
            .map(ModJarClassScanner::scanJar)
            .flatMap(Collection::stream)
            .toList();
    }

    Type getClazz(ModFileScanData.ClassData data);

    @Nullable
    Type getParent(ModFileScanData.ClassData data);

    default Type getParentOrVoid(ModFileScanData.ClassData data) {
        var parent = getParent(data);
        return parent == null ? Type.getType(void.class) : parent;
    }

    Set<Type> getInterfaces(ModFileScanData.ClassData data);

    List<ModFileScanData> getAllScanData();

    Collection<Path> getModFiles(String modId);
}
