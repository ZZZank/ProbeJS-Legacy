package zzzank.probejs.docs.assignments;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.rhino.mod.util.color.Color;
import dev.latvian.mods.unit.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import zzzank.probejs.docs.GlobalClasses;
import zzzank.probejs.docs.Primitives;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.GameUtils;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.regex.Pattern;

public class JavaPrimitives implements ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        scriptDump.assignType(List.class, Types.generic("E").asArray());
        scriptDump.assignType(
            Map.class,
            Types.object().rawNameMember("[key: string]", Types.generic("V")).build()
        );
        scriptDump.assignType(Iterable.class, Types.generic("T").asArray());
        scriptDump.assignType(Collection.class, Types.generic("E").asArray());
        scriptDump.assignType(Set.class, Types.generic("E").asArray());
        scriptDump.assignType(UUID.class, Types.STRING);

        scriptDump.assignType(JsonObject.class, Types.OBJECT);
        scriptDump.assignType(JsonArray.class, Types.ANY.asArray());
        scriptDump.assignType(JsonPrimitive.class, Types.NUMBER);
        scriptDump.assignType(JsonPrimitive.class, Types.STRING);
        scriptDump.assignType(JsonPrimitive.class, Types.BOOLEAN);
        scriptDump.assignType(JsonPrimitive.class, Types.NULL);

        scriptDump.assignType(Pattern.class, Types.primitive("RegExp"));
        scriptDump.assignType(Unit.class, Types.STRING);
        scriptDump.assignType(Unit.class, Types.NUMBER);

        scriptDump.assignType(Duration.class, Types.type(TemporalAmount.class));
        scriptDump.assignType(TemporalAmount.class, Types.NUMBER);
        scriptDump.assignType(TemporalAmount.class, Types.primitive("`${number}${'y'|'M'|'d'|'w'|'h'|'m'|'s'|'ms'|'ns'|'t'}`"));

        scriptDump.assignType(Class.class, GlobalClasses.J_CLASS.withParams("T"));

        scriptDump.assignType(ResourceLocation.class, Types.STRING);
        scriptDump.assignType(CompoundTag.class, Types.OBJECT);
        scriptDump.assignType(CompoundTag.class, Types.STRING);
        scriptDump.assignType(CollectionTag.class, Types.ANY.asArray());
        scriptDump.assignType(ListTag.class, Types.ANY.asArray());
        scriptDump.assignType(Tag.class, Types.STRING);
        scriptDump.assignType(Tag.class, Types.NUMBER);
        scriptDump.assignType(Tag.class, Types.BOOLEAN);
        scriptDump.assignType(Tag.class, Types.OBJECT);
        scriptDump.assignType(Tag.class, Types.ANY.asArray());
        scriptDump.assignType(BlockPos.class, AssignmentHelper.xyzOf(Primitives.INTEGER));
        scriptDump.assignType(Vec3.class, AssignmentHelper.xyzOf(Primitives.DOUBLE));
        scriptDump.assignType(Vec3i.class, AssignmentHelper.xyzOf(Primitives.INTEGER));
        scriptDump.assignType(AABB.class, Types.EMPTY_ARRAY);
        scriptDump.assignType(AABB.class, AssignmentHelper.xyzOf(Primitives.DOUBLE));
        scriptDump.assignType(AABB.class, Types.tuple()
            .member("x1", Primitives.DOUBLE)
            .member("y1", Primitives.DOUBLE)
            .member("z1", Primitives.DOUBLE)
            .member("x2", Primitives.DOUBLE)
            .member("y2", Primitives.DOUBLE)
            .member("z2", Primitives.DOUBLE)
            .build());
        scriptDump.assignType(Component.class, Types.type(Component.class).asArray());
        scriptDump.assignType(Component.class, "ComponentObject", Types.object()
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
            .member("hover", true, Types.type(Component.class))
            .member("extra", true, Types.type(Component.class).asArray())
            .build());
        scriptDump.assignType(Component.class, Types.STRING);
        scriptDump.assignType(Component.class, Types.NUMBER);
        scriptDump.assignType(Component.class, Types.BOOLEAN);

        scriptDump.assignType(Item.class, Types.type(ItemLike.class));
        // kubejs RegistryInfo.ITEM is marked as `.noAutoWrap()`, with custom logic to restore support
        scriptDump.assignType(Item.class, SpecialTypes.dot(GameUtils.registryName(Registries.ITEM)));
    }
}
