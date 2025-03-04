package zzzank.probejs.plugin;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.docs.ProbeBuiltinDocs;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.GameUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public class ProbeJSPlugins {

    private static final List<ProbeJSPlugin> ALL = CollectUtils.ofList(
        new BuiltinProbeJSPlugin(),
        new ProbeBuiltinDocs()
    );

    public static void register(@NotNull ProbeJSPlugin @NotNull ... plugins) {
        for (val plugin : plugins) {
            ALL.add(Objects.requireNonNull(plugin));
        }
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<ProbeJSPlugin> getAll() {
        return Collections.unmodifiableList(ALL);
    }

    @HideFromJS
    public static void forEachPlugin(@NotNull Consumer<@NotNull ProbeJSPlugin> action) {
        for (val plugin : ALL) {
            try {
                action.accept(plugin);
            } catch (Exception e) {
                ProbeJS.LOGGER.error("Error happened when applying ProbeJS plugin: {}", plugin.getClass().getName());
                GameUtils.logThrowable(e);
            }
        }
    }
}
