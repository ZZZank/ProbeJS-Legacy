package zzzank.probejs;

import dev.latvian.kubejs.util.UtilsJS;
import me.shedaniel.architectury.platform.Platform;

import java.nio.file.Files;
import java.nio.file.Path;

public class ProbePaths {

    public static final Path GAMEDIR = Platform.getGameFolder();
    public static final Path PROBE = GAMEDIR.resolve(".probe");
    public static final Path WORKSPACE_SETTINGS = GAMEDIR.resolve(".vscode");
    public static final Path SETTINGS_JSON = PROBE.resolve("probe-settings.json");
    public static final Path VSCODE_JSON = WORKSPACE_SETTINGS.resolve("settings.json");
    public static final Path GIT_IGNORE = GAMEDIR.resolve(".gitignore");
    public static final Path DECOMPILED = PROBE.resolve("decompiled");

    public static void init() {
        if (Files.notExists(PROBE)) {
            UtilsJS.tryIO(() -> Files.createDirectories(PROBE));
        }
        if (Files.notExists(WORKSPACE_SETTINGS)) {
            UtilsJS.tryIO(() -> Files.createDirectories(WORKSPACE_SETTINGS));
        }
        if (Files.notExists(ProbePaths.DECOMPILED)) {
            UtilsJS.tryIO(() -> Files.createDirectories(ProbePaths.DECOMPILED));
        }
    }

    static {
        init();
    }
}
