package zzzank.probejs.features.forge_scan;

import lombok.val;
import net.minecraftforge.forgespi.language.ModFileScanData.ClassData;
import org.objectweb.asm.Type;
import zzzank.probejs.ProbeJS;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Set;

/**
 * @author ZZZank
 */
public abstract class AccessClassData {

    private static final MethodHandle HANDLE_clazz;
    private static final MethodHandle HANDLE_parent;
    private static final MethodHandle HANDLE_interfaces;

    static {
        val lookup = MethodHandles.lookup();
        try {
            var f = ClassData.class.getDeclaredField("clazz");
            f.setAccessible(true);
            HANDLE_clazz = lookup.unreflectGetter(f);

            f = ClassData.class.getDeclaredField("parent");
            f.setAccessible(true);
            HANDLE_parent = lookup.unreflectGetter(f);

            f = ClassData.class.getDeclaredField("interfaces");
            f.setAccessible(true);
            HANDLE_interfaces = lookup.unreflectGetter(f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ProbeJS.LOGGER.error("accessing '{}' failed", ClassData.class, e);
            throw new IllegalStateException();
        }
    }

    public static Type clazz(ClassData data) {
        try {
            return (Type) HANDLE_clazz.invoke(data);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public static Type parent(ClassData data) {
        try {
            return (Type) HANDLE_parent.invoke(data);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public static Set<Type> interfaces(ClassData data) {
        try {
            return (Set<Type>) HANDLE_interfaces.invoke(data);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
}
