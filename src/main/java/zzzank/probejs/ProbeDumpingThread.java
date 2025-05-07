package zzzank.probejs;

import lombok.val;
import net.minecraft.network.chat.ClickEvent;
import zzzank.probejs.features.rhizo.RhizoClazzRemapper;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.lang.java.remap.RhinoDefault;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.utils.GameUtils;
import zzzank.probejs.utils.ProbeText;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public class ProbeDumpingThread extends Thread {

    public static ProbeDumpingThread INSTANCE;

    public final Consumer<ProbeText> messageSender;

    public static boolean exists() {
        return INSTANCE != null && INSTANCE.isAlive();
    }

    public static ProbeDumpingThread create(final Consumer<ProbeText> messageSender) {
        if (exists()) {
            throw new IllegalStateException("There's already a thread running");
        }
        INSTANCE = new ProbeDumpingThread(messageSender);
        return INSTANCE;
    }

    private ProbeDumpingThread(final Consumer<ProbeText> messageSender) {
        super("ProbeDumpingThread");
        this.messageSender = messageSender;
    }

    @Override
    public void run() {
        if (!RhizoState.MOD.get()) {
            messageSender.accept(ProbeText.pjs("rhizo_missing").red());
            messageSender.accept(ProbeText
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

        ProbeConfig.refresh();
        messageSender.accept(ProbeText.pjs("config_refreshed"));
        RemapperBridge.set(RhizoState.REMAPPER ? new RhizoClazzRemapper() : new RhinoDefault());
        RegistryInfos.refresh();

        val probeDump = new ProbeDump(messageSender);
        probeDump.addScript(ScriptDump.CLIENT_DUMP.apply(probeDump.codeDump));
        probeDump.addScript(ScriptDump.SERVER_DUMP.apply(probeDump.codeDump));
        probeDump.addScript(ScriptDump.STARTUP_DUMP.apply(probeDump.codeDump));
        try {
            probeDump.trigger();
        } catch (Throwable e) {
            messageSender.accept(ProbeText.pjs("error").red());
            messageSender.accept(ProbeText.literal(e.toString()).red());
            GameUtils.logThrowable(e);
        }
    }
}
