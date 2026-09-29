package zzzank.probejs.features;

import me.shedaniel.architectury.platform.Platform;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.features.forge_scan.ForgeScanSupport;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ForgeScanSupportImpl implements ForgeScanSupport {
    private static final MethodHandle HANDLE_clazz;
    private static final MethodHandle HANDLE_parent;
    private static final MethodHandle HANDLE_interfaces;

    static {
        try {
            var lookup = MethodHandles.privateLookupIn(ModFileScanData.ClassData.class, MethodHandles.lookup());

            HANDLE_clazz = lookup.findGetter(ModFileScanData.ClassData.class, "clazz", Type.class);
            HANDLE_parent = lookup.findGetter(ModFileScanData.ClassData.class, "parent", Type.class);
            HANDLE_interfaces = lookup.findGetter(ModFileScanData.ClassData.class, "interfaces", Set.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ProbeJS.LOGGER.error("accessing '{}' failed", ModFileScanData.ClassData.class, e);
            throw new IllegalStateException();
        }
    }

    @Override
    public Type getClazz(ModFileScanData.ClassData data) {
        try {
            return (Type) HANDLE_clazz.invokeExact(data);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public @Nullable Type getParent(ModFileScanData.ClassData data) {
        try {
            return (Type) HANDLE_parent.invokeExact(data);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Type> getInterfaces(ModFileScanData.ClassData data) {
        try {
            return (Set<Type>) HANDLE_interfaces.invokeExact(data);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
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
