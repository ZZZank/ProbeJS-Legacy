package zzzank.probejs.features.forge_scan;

import lombok.val;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import zzzank.probejs.ProbeJS;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Set;

/**
 * @author ZZZank
 */
public class AccessClassData {

    public static final MethodHandle accessClazz;
    public static final MethodHandle accessParent;
    public static final MethodHandle accessInterfaces;

    static {
        val lookup = MethodHandles.lookup();
        try {
            var f = ModFileScanData.ClassData.class.getDeclaredField("clazz");
            f.setAccessible(true);
            accessClazz = lookup.unreflectGetter(f);

            f = ModFileScanData.ClassData.class.getDeclaredField("parent");
            f.setAccessible(true);
            accessParent = lookup.unreflectGetter(f);

            f = ModFileScanData.ClassData.class.getDeclaredField("interfaces");
            f.setAccessible(true);
            accessInterfaces = lookup.unreflectGetter(f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ProbeJS.LOGGER.error("accessing '{}' failed", ModFileScanData.ClassData.class, e);
            throw new IllegalStateException();
        }
    }

    private final ModFileScanData.ClassData raw;

    public AccessClassData(ModFileScanData.ClassData raw) {
        this.raw = raw;
    }

    public Type clazz() {
        try {
            return (Type) accessClazz.invoke(raw);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public String className() {
        return clazz().getClassName();
    }

    public Type parent() {
        try {
            return (Type) accessParent.invoke(raw);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public String parentClassName() {
        val p = parent();
        return p == null ? null : p.getClassName();
    }

    public Set<Type> interfaces() {
        try {
            return (Set<Type>) accessInterfaces.invoke(raw);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
}
