package zzzank.probejs.features.forge_scan;

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
        try {
            var lookup = MethodHandles.privateLookupIn(ClassData.class, MethodHandles.lookup());

            HANDLE_clazz = lookup.findGetter(ClassData.class, "clazz", Type.class);
            HANDLE_parent = lookup.findGetter(ClassData.class, "parent", Type.class);
            HANDLE_interfaces = lookup.findGetter(ClassData.class, "interfaces", Set.class);
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

    public static Type parentNullWrapped(ClassData data) {
        var parent = parent(data);
        return parent == null ? Type.getType(void.class) : parent;
    }

    public static Set<Type> interfaces(ClassData data) {
        try {
            return (Set<Type>) HANDLE_interfaces.invoke(data);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
}
