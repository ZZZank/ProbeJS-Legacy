package zzzank.probejs.mixins;

import com.google.common.collect.BiMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author ZZZank
 */
@Mixin(RegistryManager.class)
public interface AccessForgeRegistryManager {

    @Accessor(value = "registries", remap = false)
    BiMap<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> getRegistries();
}
