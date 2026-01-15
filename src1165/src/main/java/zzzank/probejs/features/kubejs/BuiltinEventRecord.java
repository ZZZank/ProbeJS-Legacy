package zzzank.probejs.features.kubejs;

import dev.latvian.kubejs.KubeJSEvents;
import dev.latvian.kubejs.block.*;
import dev.latvian.kubejs.client.ClientEventJS;
import dev.latvian.kubejs.client.ClientLoggedInEventJS;
import dev.latvian.kubejs.client.ClientTickEventJS;
import dev.latvian.kubejs.client.DebugInfoEventJS;
import dev.latvian.kubejs.client.painter.PaintEventJS;
import dev.latvian.kubejs.client.painter.screen.ScreenPaintEventJS;
import dev.latvian.kubejs.command.CommandRegistryEventJS;
import dev.latvian.kubejs.entity.EntitySpawnedEventJS;
import dev.latvian.kubejs.entity.LivingEntityAttackEventJS;
import dev.latvian.kubejs.entity.LivingEntityDeathEventJS;
import dev.latvian.kubejs.event.StartupEventJS;
import dev.latvian.kubejs.item.*;
import dev.latvian.kubejs.item.custom.ItemArmorTierEventJS;
import dev.latvian.kubejs.item.custom.ItemToolTierEventJS;
import dev.latvian.kubejs.loot.*;
import dev.latvian.kubejs.net.NetworkEventJS;
import dev.latvian.kubejs.net.PainterUpdatedEventJS;
import dev.latvian.kubejs.player.*;
import dev.latvian.kubejs.recipe.CompostablesRecipeEventJS;
import dev.latvian.kubejs.recipe.RecipeTypeRegistryEventJS;
import dev.latvian.kubejs.recipe.RecipesAfterLoadEventJS;
import dev.latvian.kubejs.recipe.special.SpecialRecipeSerializerManager;
import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.script.data.DataPackEventJS;
import dev.latvian.kubejs.server.CommandEventJS;
import dev.latvian.kubejs.server.ServerEventJS;
import dev.latvian.kubejs.world.ExplosionEventJS;
import dev.latvian.kubejs.world.SimpleWorldEventJS;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public record BuiltinEventRecord(Class<?> eventClass, ScriptType type, String id) {

    /// Collected by scanning lines with `KubeJSEvents.` in KubeJS source files
    public static final Map<String, BuiltinEventRecord> RECORDS = Stream.of(
        of(OldItemTooltipEventJS.class, ScriptType.CLIENT, "client.item_tooltip"),
        of(GenericLootEventJS.class, ScriptType.SERVER, "generic.loot_tables"),
        of(BlockLootEventJS.class, ScriptType.SERVER, "block.loot_tables"),
        of(EntityLootEventJS.class, ScriptType.SERVER, "entity.loot_tables"),
        of(GiftLootEventJS.class, ScriptType.SERVER, "gift.loot_tables"),
        of(FishingLootEventJS.class, ScriptType.SERVER, "fishing.loot_tables"),
        of(ChestLootEventJS.class, ScriptType.SERVER, "chest.loot_tables"),
        of(DataPackEventJS.class, ScriptType.SERVER, "server.datapack.last"),
        of(DataPackEventJS.class, ScriptType.SERVER, "server.datapack.first"),
        of(BlockRightClickEventJS.class, null, KubeJSEvents.BLOCK_RIGHT_CLICK),
        of(BlockLeftClickEventJS.class, null, KubeJSEvents.BLOCK_LEFT_CLICK),
        of(BlockBreakEventJS.class, null, KubeJSEvents.BLOCK_BREAK),
        of(BlockPlaceEventJS.class, null, KubeJSEvents.BLOCK_PLACE),
        of(BlockDropsEventJS.class, null, KubeJSEvents.BLOCK_DROPS),
        of(ItemToolTierEventJS.class, ScriptType.STARTUP, KubeJSEvents.ITEM_REGISTRY_TOOL_TIERS),
        of(ItemArmorTierEventJS.class, ScriptType.STARTUP, KubeJSEvents.ITEM_REGISTRY_ARMOR_TIERS),
        of(ClientEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_INIT),
        of(NetworkEventJS.class, null, KubeJSEvents.PLAYER_DATA_FROM_SERVER),
        of(DebugInfoEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_DEBUG_INFO_LEFT),
        of(DebugInfoEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_DEBUG_INFO_RIGHT),
        of(ItemTooltipEventJS.class, ScriptType.CLIENT, KubeJSEvents.ITEM_TOOLTIP),
        of(ClientTickEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_TICK),
        of(ClientLoggedInEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_LOGGED_IN),
        of(ClientLoggedInEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_LOGGED_OUT),
        of(PaintEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_PAINT_WORLD),
        of(PainterUpdatedEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_PAINTER_UPDATED),
        of(ScreenPaintEventJS.class, ScriptType.CLIENT, KubeJSEvents.CLIENT_PAINT_SCREEN),
        of(ItemFoodEatenEventJS.class, ScriptType.SERVER, KubeJSEvents.ITEM_FOOD_EATEN),
        of(CompostablesRecipeEventJS.class, ScriptType.SERVER, KubeJSEvents.RECIPES_COMPOSTABLES),
        of(LivingEntityDeathEventJS.class, null, KubeJSEvents.ENTITY_DEATH),
        of(LivingEntityAttackEventJS.class, null, KubeJSEvents.ENTITY_ATTACK),
        of(EntitySpawnedEventJS.class, ScriptType.SERVER, KubeJSEvents.ENTITY_SPAWNED),
        of(ItemRightClickEventJS.class, null, KubeJSEvents.ITEM_RIGHT_CLICK),
        of(ItemRightClickEmptyEventJS.class, null, KubeJSEvents.ITEM_RIGHT_CLICK_EMPTY),
        of(ItemLeftClickEventJS.class, null, KubeJSEvents.ITEM_LEFT_CLICK),
        of(ItemPickupEventJS.class, null, KubeJSEvents.ITEM_PICKUP),
        of(ItemTossEventJS.class, null, KubeJSEvents.ITEM_TOSS),
        of(ItemEntityInteractEventJS.class, null, KubeJSEvents.ITEM_ENTITY_INTERACT),
        of(ItemCraftedEventJS.class, null, KubeJSEvents.ITEM_CRAFTED),
        of(InventoryChangedEventJS.class, null, KubeJSEvents.PLAYER_INVENTORY_CHANGED),
        of(ItemSmeltedEventJS.class, null, KubeJSEvents.ITEM_SMELTED),
        of(StartupEventJS.class, ScriptType.STARTUP, KubeJSEvents.INIT),
        of(StartupEventJS.class, ScriptType.STARTUP, KubeJSEvents.POSTINIT),
        of(CheckPlayerLoginEventJS.class, ScriptType.SERVER, KubeJSEvents.PLAYER_CHECK_LOGIN),
        of(NetworkEventJS.class, null, KubeJSEvents.PLAYER_DATA_FROM_CLIENT),
        of(SimplePlayerEventJS.class, null, KubeJSEvents.PLAYER_LOGGED_IN),
        of(SimplePlayerEventJS.class, null, KubeJSEvents.PLAYER_LOGGED_OUT),
        of(SimplePlayerEventJS.class, null, KubeJSEvents.PLAYER_TICK),
        of(PlayerChatEventJS.class, null, KubeJSEvents.PLAYER_CHAT),
        of(PlayerAdvancementEventJS.class, null, KubeJSEvents.PLAYER_ADVANCEMENT),
        of(InventoryEventJS.class, null, KubeJSEvents.PLAYER_INVENTORY_OPENED),
        of(ChestEventJS.class, null, KubeJSEvents.PLAYER_CHEST_OPENED),
        of(InventoryEventJS.class, null, KubeJSEvents.PLAYER_INVENTORY_CLOSED),
        of(ChestEventJS.class, null, KubeJSEvents.PLAYER_CHEST_CLOSED),
        of(SpecialRecipeSerializerManager.class, ScriptType.SERVER, KubeJSEvents.RECIPES_SERIALIZER_SPECIAL_FLAG),
        of(RecipesAfterLoadEventJS.class, ScriptType.SERVER, KubeJSEvents.RECIPES_AFTER_LOAD),
        of(CommandRegistryEventJS.class, ScriptType.SERVER, KubeJSEvents.COMMAND_REGISTRY),
        of(ServerEventJS.class, ScriptType.SERVER, KubeJSEvents.SERVER_LOAD),
        of(ServerEventJS.class, ScriptType.SERVER, KubeJSEvents.SERVER_UNLOAD),
        of(ServerEventJS.class, ScriptType.SERVER, KubeJSEvents.SERVER_TICK),
        of(CommandEventJS.class, ScriptType.SERVER, KubeJSEvents.COMMAND_RUN),
        of(DataPackEventJS.class, ScriptType.SERVER, KubeJSEvents.SERVER_DATAPACK_LOW_PRIORITY),
        of(DataPackEventJS.class, ScriptType.SERVER, KubeJSEvents.SERVER_DATAPACK_HIGH_PRIORITY),
        of(RecipeTypeRegistryEventJS.class, ScriptType.SERVER, KubeJSEvents.RECIPES_TYPE_REGISTRY),
        of(BlockModificationEventJS.class, ScriptType.STARTUP, KubeJSEvents.BLOCK_MODIFICATION),
        of(ItemModificationEventJS.class, ScriptType.STARTUP, KubeJSEvents.ITEM_MODIFICATION),
        of(SimpleWorldEventJS.class, ScriptType.SERVER, KubeJSEvents.WORLD_LOAD),
        of(SimpleWorldEventJS.class, ScriptType.SERVER, KubeJSEvents.WORLD_UNLOAD),
        of(SimpleWorldEventJS.class, ScriptType.SERVER, KubeJSEvents.WORLD_TICK),
        of(ExplosionEventJS.Pre.class, null, KubeJSEvents.WORLD_EXPLOSION_PRE),
        of(ExplosionEventJS.Post.class, null, KubeJSEvents.WORLD_EXPLOSION_POST)
    ).collect(Collectors.toMap(
        BuiltinEventRecord::id,
        Function.identity(),
        (a, b) -> {
            throw new IllegalArgumentException(String.format("found two record with the same id: %s and %s", a, b));
        },
        HashMap::new
    ));

    private static BuiltinEventRecord of(Class<?> eventClass, ScriptType type, String id) {
        return new BuiltinEventRecord(eventClass, type, id);
    }
}
