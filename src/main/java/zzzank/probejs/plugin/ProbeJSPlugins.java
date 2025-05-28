package zzzank.probejs.plugin;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.docs.*;
import zzzank.probejs.docs.assignments.*;
import zzzank.probejs.docs.bindings.Bindings;
import zzzank.probejs.docs.events.ForgeEvents;
import zzzank.probejs.docs.events.KubeEvents;
import zzzank.probejs.docs.recipes.RecipeEvents;
import zzzank.probejs.docs.recipes.doc.BuiltinRecipeDocs;
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
        //type
        new RegistryTypes(),
        new SpecialTypes(),
        new Primitives(),
        new JavaPrimitives(),
        new RecipeTypes(),
        new WorldTypes(),
        new EnumTypes(),
        new KubeWrappers(),
        new FunctionalInterfaces(),
        new TypeRedirecting(),
        //binding
        new Bindings(),
        new LoadClassFn(),
        //event
        new KubeEvents(),
        //      new TagEvents(),
        new RecipeEvents(),
        new BuiltinRecipeDocs(),
        new ForgeEvents(),
        //misc
        new KubeJSDenied(),
        new GlobalClasses(),
        new ParamFix(),
        new Snippets(),
        new SimulateOldTyping(),
        // js event
        new BuiltinProbeJSPlugin()
    );

    public static void register(@NotNull ProbeJSPlugin @NotNull ... plugins) {
        for (val plugin : plugins) {
            ALL.add(Objects.requireNonNull(plugin));
        }
    }

    public static void remove(Class<? extends ProbeJSPlugin> pluginType) {
        ALL.removeIf(pluginType::isInstance);
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<ProbeJSPlugin> getAll() {
        return Collections.unmodifiableList(ALL);
    }

    @HideFromJS
    public static void forEachPlugin(@NotNull Consumer<@NotNull ProbeJSPlugin> action) {
        Objects.requireNonNull(action);
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
