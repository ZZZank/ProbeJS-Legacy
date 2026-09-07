package zzzank.probejs;

import lombok.val;
import zzzank.probejs.features.minecraft.DumpMessageList;
import zzzank.probejs.features.rhizo.RhizoClazzRemapper;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.lang.java.remap.RhinoDefault;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.utils.GameUtils;
import zzzank.probejs.utils.ProbeText;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public class ProbeDumpingThread extends Thread {

    public static ProbeDumpingThread INSTANCE;

    public final Consumer<ProbeText> messageSender;
    public final boolean cleanOldDumps;
    public final Function<ProbeText, DumpMessageList.MessageEntry> toEntry;

    public static boolean exists() {
        return INSTANCE != null && INSTANCE.isAlive();
    }

    public static ProbeDumpingThread create(
        Consumer<ProbeText> messageSender,
        Function<ProbeText, DumpMessageList.MessageEntry> toEntry,
        boolean cleanOldDumps
    ) {
        if (exists()) {
            throw new IllegalStateException("There's already a thread running");
        }
        INSTANCE = new ProbeDumpingThread(messageSender, toEntry, cleanOldDumps);
        return INSTANCE;
    }

    private ProbeDumpingThread(
        Consumer<ProbeText> messageSender,
        Function<ProbeText, DumpMessageList.MessageEntry> toEntry,
        boolean cleanOldDumps
    ) {
        super("ProbeDumpingThread");
        this.messageSender = messageSender;
        this.toEntry = toEntry;
        this.cleanOldDumps = cleanOldDumps;
    }

    @Override
    public void run() {
        RemapperBridge.set(RhizoState.REMAPPER ? new RhizoClazzRemapper() : new RhinoDefault());
        RegistryInfos.refresh();

        val probeDump = new ProbeDump(cleanOldDumps);
        probeDump.addScript(ScriptDump.CLIENT_DUMP.apply(probeDump.sharedDump));
        probeDump.addScript(ScriptDump.SERVER_DUMP.apply(probeDump.sharedDump));
        probeDump.addScript(ScriptDump.STARTUP_DUMP.apply(probeDump.sharedDump));
        try {
            probeDump.trigger(toEntry);
        } catch (Throwable e) {
            var errorEntry = toEntry.apply(ProbeText.literal("ERROR").red());
            errorEntry.provideBody(ProbeText.literal(e.toString()));
            messageSender.accept(ProbeText.pjs("error").red());
            messageSender.accept(ProbeText.literal(e.toString()).red());
            GameUtils.logThrowable(e);
        }
    }
}
