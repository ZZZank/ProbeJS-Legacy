package zzzank.probejs.utils.registry;

import lombok.val;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.mixins.AccessForgeRegistryManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author ZZZank
 */
public final class RegistryInfos {
    /**
     * not using {@link net.minecraft.resources.ResourceKey} as key, because ResourceKey for registries
     * will always use {@link net.minecraft.core.Registry#ROOT_REGISTRY_NAME} as its parent
     */
    private static Map<ResourceLocation, RegistryInfo> ALL = new HashMap<>();

    public static @NotNull Collection<RegistryInfo> values() {
        return ALL.values();
    }

    public static Set<ResourceLocation> keys() {
        return ALL.keySet();
    }

    public static Map<ResourceLocation, RegistryInfo> all() {
        return ALL;
    }

    public static void refresh() {
        ALL = new HashMap<>();
        for (val entry : ((AccessForgeRegistryManager) RegistryManager.FROZEN).getRegistries().entrySet()) {
            ALL.put(entry.getKey(), new RegistryInfo(entry.getValue()));
        }
        for (val entry : ((AccessForgeRegistryManager) RegistryManager.ACTIVE).getRegistries().entrySet()) {
            ALL.put(entry.getKey(), new RegistryInfo(entry.getValue()));
        }
    }

    private RegistryInfos() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
