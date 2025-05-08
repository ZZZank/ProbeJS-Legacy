package zzzank.probejs.utils.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class RegistryInfo implements Comparable<RegistryInfo> {

    private final ForgeRegistry<? extends IForgeRegistryEntry<?>> forgeRaw;
    private final ResourceKey<? extends Registry<?>> resKey;
    @Nullable
    private final StaticTagHelper<?> tagHelper;

    public RegistryInfo(ForgeRegistry<? extends IForgeRegistryEntry<?>> forgeRegistry) {
        this.forgeRaw = forgeRegistry;
        this.resKey = forgeRaw.getRegistryKey();
        this.tagHelper = StaticTags.get(id());
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
        return forgeRaw.getKeys().stream();
    }

    public Stream<ResourceLocation> tagIds() {
        if (tagHelper == null) {
            return Stream.empty();
        }
        return tagHelper.getAllTags()
            .getAvailableTags()
            .stream();
    }

    public Collection<? extends Map.Entry<? extends ResourceKey<?>, ?>> entries() {
        return forgeRaw.getEntries();
    }

    public Class<?> objectBaseType() {
        return forgeRaw.getRegistrySuperType();
    }
}