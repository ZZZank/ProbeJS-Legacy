package zzzank.probejs.utils.registry;

import lombok.experimental.UtilityClass;
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
@UtilityClass
public final class RegistryInfos {
    /**
     * not using {@link net.minecraft.resources.ResourceKey} as key, because ResourceKey for registries
     * will always use {@link net.minecraft.core.Registry#ROOT_REGISTRY_NAME} as its parent
     */
    private Map<ResourceLocation, RegistryInfo> ALL = new HashMap<>();

    public void refresh() {
        val server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ALL.clear();
        server.registryAccess()
            .registries()
            .map(RegistryAccess.RegistryEntry::value)
            .map(RegistryInfo::new)
            .forEach(info -> ALL.put(info.id(), info));
    }

    public @NotNull Collection<RegistryInfo> values() {
        return ALL.values();
    }

    public Set<ResourceLocation> keys() {
        return ALL.keySet();
    }

    public Map<ResourceLocation, RegistryInfo> all() {
        return ALL;
    }
}
