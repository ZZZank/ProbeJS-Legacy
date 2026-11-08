package zzzank.probejs;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import zzzank.probejs.features.minecraft.ProbeJSConfigScreen;
import zzzank.probejs.features.minecraft.ProbeJSDumpScreen;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.linter.Linter;
import zzzank.probejs.utils.ProbeText;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GameEvents {
    private static final int MOD_LIMIT = 200;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerJoined(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!ProbeConfig.enabled.get()) {
            return;
        }

        var player = event.getPlayer();
        Consumer<ProbeText> sendMsg = msg -> player.sendSystemMessage(msg.unwrap());

        if (!RhizoState.MOD.get()) { // always off as of 1.20
            sendMsg.accept(ProbeText.pjs("rhizo_missing"));
            sendMsg.accept(ProbeText
                .pjs("download_rhizo_help")
                .append(ProbeText.literal("CurseForge")
                    .aqua()
                    .underlined(true)
                    .click(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/rhizo/files"))
                .append(" / ")
                .append(ProbeText.literal("Github")
                    .aqua()
                    .underlined(true)
                    .click(ClickEvent.Action.OPEN_URL, "https://github.com/ZZZank/Rhizo/releases/latest"))
            );
        }

        sendMsg.accept(
            ProbeText
                .pjs("enabled_warning")
                .append(ProbeText.literal("/probejs config")
                    .click(ClickEvent.Action.SUGGEST_COMMAND, "/probejs config")
                    .aqua()
                )
        );

        if (ModList.get().size() >= MOD_LIMIT) {
            if (ProbeConfig.complete.get()) {
                sendMsg.accept(ProbeText.pjs("performance", ModList.get().size()));
            }
        }
    }

    @SubscribeEvent
    public static void registerCommand(RegisterClientCommandsEvent event) {
        BiConsumer<CommandContext<CommandSourceStack>, ProbeText> sendMsg =
            (context, text) -> context.getSource().sendSuccess(text::unwrap, true);

        event.getDispatcher().register(
            Commands.literal("probejs")
                .executes(context -> {
                    Minecraft.getInstance().setScreen(new ProbeJSDumpScreen());
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("lint")
                    .executes(context -> {
                        Linter.defaultLint(msg -> sendMsg.accept(context, msg));
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(Commands.literal("config")
                    .executes(cx -> {
                        Minecraft.getInstance().setScreen(new ProbeJSConfigScreen(null));
                        return Command.SINGLE_SUCCESS;
                    }))
        );
    }
}
