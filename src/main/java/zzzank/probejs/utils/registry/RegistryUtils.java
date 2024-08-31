package zzzank.probejs.utils.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

import java.util.Collection;
import java.util.stream.Collectors;

public class RegistryUtils {

    public static Collection<ResourceKey<? extends Registry<?>>> getRegistries(RegistryAccess access) {
        return RegistryInfos.infos.values().stream().map((RegistryInfo registryInfo) -> registryInfo.resKey).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T> ResourceKey<Registry<T>> castKey(ResourceKey<?> key) {
        return (ResourceKey<Registry<T>>) key;
    }
}
