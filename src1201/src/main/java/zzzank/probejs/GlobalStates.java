package zzzank.probejs;

import lombok.val;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraftforge.server.ServerLifecycleHooks;
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
    public static final Supplier<Set<String>> LOOT_TABLES = () -> ServerLifecycleHooks.getCurrentServer()
        .getLootData()
        .getKeys(LootDataType.TABLE)
        .stream()
        .map(Objects::toString)
        .collect(Collectors.toSet());

    public static final Supplier<Set<String>> LANG_KEYS = () -> {
        val mc = Minecraft.getInstance();
        val english = mc.getLanguageManager().getLanguage(LanguageManager.DEFAULT_LANGUAGE_CODE);
        if (english == null) {
            return Set.of();
        }

        val clientLanguage = ClientLanguage.loadFrom(
            mc.getResourceManager(),
            List.of(LanguageManager.DEFAULT_LANGUAGE_CODE),
            english.bidirectional()
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
