package zzzank.probejs.docs.assignments;

import dev.latvian.kubejs.block.MaterialJS;
import dev.latvian.kubejs.block.MaterialListJS;
import dev.latvian.kubejs.fluid.FluidStackJS;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.item.ingredient.IngredientJS;
import dev.latvian.kubejs.text.Text;
import dev.latvian.mods.rhino.mod.util.color.Color;
import me.shedaniel.architectury.fluid.FluidStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import zzzank.probejs.docs.Primitives;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Locale;

/**
 * @author ZZZank
 */
public class KubeWrappers implements ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        scriptDump.assignType(Text.class, Types.type(Text.class).asArray());
        scriptDump.assignType(Text.class, "ComponentObject", Types.object()
            .member("text", true, Types.STRING)
            .member("translate", true, Types.primitive("Special.LangKey"))
            .member("with", true, Types.ANY.asArray())
            .member("color", true, Types.type(Color.class))
            .member("bold", true, Types.BOOLEAN)
            .member("italic", true, Types.BOOLEAN)
            .member("underlined", true, Types.BOOLEAN)
            .member("strikethrough", true, Types.BOOLEAN)
            .member("obfuscated", true, Types.BOOLEAN)
            .member("insertion", true, Types.STRING)
            .member("font", true, Types.STRING)
            .member("click", true, Types.type(ClickEvent.class))
            .member("hover", true, Types.type(Text.class))
            .member("extra", true, Types.type(Text.class).asArray())
            .build());
        scriptDump.assignType(Text.class, Types.STRING);
        scriptDump.assignType(Text.class, Types.NUMBER);
        scriptDump.assignType(Text.class, Types.BOOLEAN);
        scriptDump.assignType(Text.class, Types.type(Component.class).contextShield(BaseType.FormatType.RETURN));

        scriptDump.assignType(ItemStackJS.class, Types.type(Item.class));
        scriptDump.assignType(ItemStackJS.class, Types.type(ItemStack.class).contextShield(BaseType.FormatType.RETURN));
        scriptDump.assignType(ItemStackJS.class, "ItemWithCount", Types.object()
            .member("item", Types.primitive("Special.Item"))
            .member("count", true, Primitives.INTEGER)
            .build());

        scriptDump.assignType(IngredientJS.class, Types.type(ItemStack.class));
        scriptDump.assignType(IngredientJS.class, Types.type(IngredientJS.class).asArray());
        scriptDump.assignType(IngredientJS.class, Types.type(Ingredient.class).contextShield(BaseType.FormatType.RETURN));

        scriptDump.assignType(IngredientJS.class, Types.primitive("RegExp"));
        scriptDump.assignType(IngredientJS.class, Types.literal("*"));
        scriptDump.assignType(IngredientJS.class, Types.literal("-"));
        scriptDump.assignType(IngredientJS.class, Types.primitive("`#${Special.ItemTag}`"));
        scriptDump.assignType(IngredientJS.class, Types.primitive("`@${Special.Mod}`"));
        scriptDump.assignType(IngredientJS.class, Types.primitive("`%${Special.CreativeModeTab}`"));

        scriptDump.assignType(FluidStackJS.class, Types.type(Fluid.class));
        scriptDump.assignType(FluidStackJS.class, Types.type(FluidStack.class));
        scriptDump.assignType(FluidStackJS.class, Types.literal("-"));
        scriptDump.assignType(FluidStackJS.class, Types.primitive("`${integer}x ${Special.Fluid}`"));
        scriptDump.assignType(FluidStackJS.class, "FluidWithAmount", Types.object()
            .member("fluid", Types.primitive("Special.Fluid"))
            .member("amount", true, Primitives.INTEGER)
            .member("nbt", true, Types.or(Primitives.CHAR_SEQUENCE, Types.EMPTY_OBJECT))
            .build());

        scriptDump.assignType(MaterialJS.class, Types.or(MaterialListJS.INSTANCE.map.keySet()
            .stream()
            .map(s -> s.toLowerCase(Locale.ROOT))
            .map(Types::literal)
            .toArray(BaseType[]::new))
        );

        //unwrap
        scriptDump.assignType(Component.class, Types.type(Text.class));
        scriptDump.assignType(MutableComponent.class, Types.type(Text.class));
        scriptDump.assignType(Ingredient.class, Types.type(IngredientJS.class));
        scriptDump.assignType(ItemStack.class, Types.type(ItemStackJS.class));
        scriptDump.assignType(ItemLike.class, Types.type(Item.class));
    }
}
