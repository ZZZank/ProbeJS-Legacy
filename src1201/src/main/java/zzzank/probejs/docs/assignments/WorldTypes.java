package zzzank.probejs.docs.assignments;

import dev.latvian.mods.kubejs.BuiltinKubeJSPlugin;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.rhino.mod.util.color.Color;
import dev.latvian.mods.rhino.mod.wrapper.ColorWrapper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import zzzank.probejs.docs.Primitives;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

/**
 * @see BuiltinKubeJSPlugin
 */
public class WorldTypes implements ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        scriptDump.assignType(BlockStatePredicate.class, Types.type(BlockStatePredicate.class).asArray());
        scriptDump.assignType(
            BlockStatePredicate.class,
            "BlockStatePredicateObject",
            Types.object()
                .member("or", true, Types.type(BlockStatePredicate.class))
                .member("not", true, Types.type(BlockStatePredicate.class))
                .build()
        );
        scriptDump.assignType(BlockStatePredicate.class, Types.type(Block.class));
        scriptDump.assignType(BlockStatePredicate.class, Types.type(BlockState.class));
        scriptDump.assignType(BlockStatePredicate.class, Types.primitive("Special.BlockTag"));
        scriptDump.assignType(BlockStatePredicate.class, Types.primitive("RegExp"));
        scriptDump.assignType(BlockStatePredicate.class, Types.literal("*"));
        scriptDump.assignType(BlockStatePredicate.class, Types.literal("-"));

        scriptDump.assignType(RuleTest.class, Types.type(CompoundTag.class));
        scriptDump.assignType(Tier.class, Types.STRING);
        scriptDump.assignType(ArmorMaterial.class, Types.STRING);
        scriptDump.assignType(EntitySelector.class, Types.STRING);
        scriptDump.assignType(Stat.class, Types.STRING);
        scriptDump.assignType(SoundType.class, Types.STRING);
        scriptDump.assignType(ParticleOptions.class, Types.STRING);

        BaseType[] predefinedColors = ColorWrapper.MAP.keySet()
            .stream()
            .map(String::toLowerCase)
            .distinct()
            .map(Types::literal)
            .toArray(BaseType[]::new);
        scriptDump.assignType(Color.class, Types.or(predefinedColors));
        scriptDump.assignType(Color.class, Types.primitive("`#${string}`"));
        scriptDump.assignType(Color.class, Primitives.INTEGER);

        scriptDump.assignType(TextColor.class, Types.or(predefinedColors));
        scriptDump.assignType(TextColor.class, Types.primitive("`#${string}`"));
        scriptDump.assignType(TextColor.class, Primitives.INTEGER);

        BaseType[] actions = new BaseType[]{
            Types.literal("open_url"),
            Types.literal("open_file"),
            Types.literal("run_command"),
            Types.literal("suggest_command"),
            Types.literal("change_page"),
            Types.literal("copy_to_clipboard"),
        };
        scriptDump.assignType(ClickEvent.class, Types.object()
            .member("action", Types.or(actions))
            .member("value", Types.STRING)
            .build());
        scriptDump.assignType(ClickEvent.class, Types.STRING);
    }
}
