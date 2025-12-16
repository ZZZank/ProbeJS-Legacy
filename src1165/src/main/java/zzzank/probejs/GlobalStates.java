package zzzank.probejs;

import lombok.val;
import me.shedaniel.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import zzzank.probejs.mixins.AccessTextureAtlas;
import zzzank.probejs.mixins.AccessTextureManager;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GlobalStates {
    public static final Supplier<Set<String>> RECIPE_IDS = () -> ServerLifecycleHooks.getCurrentServer()
        .getRecipeManager()
        .getRecipeIds()
        .filter(id -> !id.getPath().startsWith("kjs_"))
        .map(Objects::toString)
        .collect(Collectors.toSet());
    public static final Supplier<? extends Collection<String>> LOOT_TABLES = () -> ServerLifecycleHooks.getCurrentServer()
        .getLootTables()
        .getIds()
        .stream()
        .map(Objects::toString)
        .toList();

    public static final Supplier<Set<String>> LANG_KEYS = () -> {
        val mc = Minecraft.getInstance();
        val english = mc.getLanguageManager().getLanguage("en_us");
        if (english == null) {
            return Set.of();
        }

        val clientLanguage = ClientLanguage.loadFrom(
            mc.getResourceManager(),
            Collections.singletonList(english)
        );
        return clientLanguage.getLanguageData().keySet();
    };

    public static final Supplier<Set<String>> RAW_TEXTURES = () ->
        ((AccessTextureManager) Minecraft.getInstance().getTextureManager())
            .byPath()
            .keySet()
            .stream()
            .map(ResourceLocation::toString)
            .collect(Collectors.toSet());

    public static final Supplier<Set<String>> TEXTURES = () ->
        ((AccessTextureAtlas) Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS))
            .texturesByName()
            .keySet()
            .stream()
            .map(ResourceLocation::toString)
            .collect(Collectors.toSet());

    public static final Supplier<Collection<String>> MODS = Platform::getModIds;
}
