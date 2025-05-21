package zzzank.probejs.mixins;

import dev.latvian.mods.kubejs.item.custom.ItemType;
import dev.latvian.mods.kubejs.item.custom.ItemTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import zzzank.probejs.utils.ShouldNotHappenException;

import java.util.Map;

/**
 * @author ZZZank
 */
@Mixin(value = ItemTypes.class, remap = false)
public interface AccessItemTypes {

    @Accessor
    static Map<String, ItemType> getMAP() {
        throw new ShouldNotHappenException("mixin method called");
    }
}
