package zzzank.probejs.mixins;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author ZZZank
 */
@Mixin(TextureAtlas.class)
public interface AccessTextureAtlas {

    @Accessor("texturesByName")
    Map<ResourceLocation, TextureAtlasSprite> texturesByName();
}
