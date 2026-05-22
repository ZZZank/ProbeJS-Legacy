package zzzank.probejs.utils.registry;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
@UtilityClass
public final class RegistryInfos {
    /**
     * not using {@link net.minecraft.resources.ResourceKey} as key, because ResourceKey for registries
     * will always use {@link net.minecraft.core.registries.BuiltInRegistries#ROOT_REGISTRY_NAME} as its parent
     */
    private Map<ResourceLocation, RegistryInfo> ALL = new HashMap<>();

    public void refresh() {
        val server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ALL = server.registryAccess()
            .registries()
            .map(RegistryAccess.RegistryEntry::value)
            .map(RegistryInfo::new)
            .collect(Collectors.toMap(RegistryInfo::id, Function.identity()));
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

    public RegistryInfo get(ResourceKey<? extends Registry<?>> key) {
        return ALL.get(key.location());
    }
}
