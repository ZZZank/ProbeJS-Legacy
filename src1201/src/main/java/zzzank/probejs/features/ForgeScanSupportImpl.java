package zzzank.probejs.features;

import dev.architectury.platform.Platform;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.features.forge_scan.ForgeScanSupport;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ForgeScanSupportImpl implements ForgeScanSupport {

    @Override
    public Type getClazz(ModFileScanData.ClassData data) {
        return data.clazz();
    }

    @Override
    public @Nullable Type getParent(ModFileScanData.ClassData data) {
        return data.parent();
    }

    @Override
    public Set<Type> getInterfaces(ModFileScanData.ClassData data) {
        return data.interfaces();
    }

    @Override
    public List<ModFileScanData> getAllScanData() {
        return ModList.get().getAllScanData();
    }

    @Override
    public Collection<Path> getModFiles(String modId) {
        var mod = Platform.getMod(modId);
        if (mod == null) {
            return Collections.emptyList();
        }

        try {
            return mod.getFilePaths();
        } catch (Exception e) {
            ProbeJS.LOGGER.error("unable to locate file for mod '{}'", mod.getModId(), e);
            return Collections.emptyList();
        }
    }
}
