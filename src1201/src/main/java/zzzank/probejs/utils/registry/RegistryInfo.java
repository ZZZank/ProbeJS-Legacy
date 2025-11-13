package zzzank.probejs.utils.registry;

import lombok.val;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class RegistryInfo implements Comparable<RegistryInfo> {

    private final ResourceKey<? extends Registry<?>> resKey;
    private final Registry<?> registry;

    public RegistryInfo(Registry<?> registry) {
        this.registry = registry;
        this.resKey = registry.key();
    }

    @Override
    public int compareTo(@NotNull RegistryInfo o) {
        return resKey.compareTo(o.resKey);
    }

    public ResourceLocation id() {
        return resKey.location();
    }

    public ResourceKey<? extends Registry<?>> resourceKey() {
        return resKey;
    }

    public Stream<ResourceLocation> objectIds() {
        return registry.keySet().stream();
    }

    public Stream<ResourceLocation> tagIds() {
        return registry.getTagNames().map(TagKey::location);
    }

    public Collection<? extends Map.Entry<? extends ResourceKey<?>, ?>> entries() {
        return registry.entrySet();
    }

    public Class<?> objectBaseType() {
        val info = dev.latvian.mods.kubejs.registry.RegistryInfo.MAP.get(resKey);
        return info == null ? null : info.objectBaseClass;
    }
}