package zzzank.probejs.features.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.ProbeText;
import zzzank.probejs.utils.ReflectUtils;
import zzzank.probejs.utils.config.io.SerdeHolder;
import zzzank.probejs.utils.config.report.AccessResult;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.struct.ConfigEntry;

import java.util.List;

/// Converts a [ConfigEntry] into an in-game [Button].
///
/// There're 3 kinds of config entries:
/// 1. `Boolean`: the button displays green `on` and red `off`, clicking toggles it.
/// 2. `Enum`: the button displays the current value name, clicking cycles to the next
///    value (shift-click cycles backwards). Hovering shows a value indicator as
///    tooltip, which is a list of values with the selected one marked by `> ` and
///    colored green. At most 5 lines are shown, with 2 values above and 2 values
///    below the selected one at most, the rest is summarized by a
///    `... (3 more values)` line. Example:
///    ```
///    VALUE_1
///    VALUE_2
///    > VALUE_3
///    VALUE_4
///    ... (3 more values)
///    ```
///    See [EnumValueWindow] for the exact windowing algorithm.
/// 3. everything else: a simple toggle is not possible, so the button shows `Edit`
///    and opens a screen with an edit box. See [EditConfigButton], [OptionEditScreen]
///    and [SerdeHolder#xmapToString] for details.
///
/// Entries that fit none of these cases (config categories, read-only entries, types
/// without a string serde) fall back to [UnsupportedConfigButton]: it is disabled, but
/// its tooltip still explains why.
///
/// The button itself renders no tooltip: [OptionList] collects lines through
/// [#appendTooltipLines(List)] / [#appendResultLines(List)] and draws them.
///
/// Use `ConfigButton.of(...)` to create the correct subtype for a config entry, and
/// `ConfigButton.preferredWidth(...)` to lay out a column of them.
public abstract class ConfigButton extends Button {

    public static final int MIN_WIDTH = 55;
    public static final int MAX_WIDTH = 120;

    protected static final int MAX_ENUM_VALUE_LINES = 5;

    protected static final Component LABEL_ON = ProbeText.pjs("option.on").green().unwrap();
    protected static final Component LABEL_OFF = ProbeText.pjs("option.off").red().unwrap();
    protected static final Component LABEL_EDIT = ProbeText.pjs("option.edit").unwrap();
    protected static final Component LABEL_UNSUPPORTED = ProbeText.pjs("option.unsupported").gray().unwrap();

    protected final ConfigEntry<?> entry;
    protected final Screen screen;

    /// report of the last non-good `ConfigEntry#set(Object)`, cleared by the next good one
    @Nullable
    private AccessResult<?> lastResult;

    /// width the button face was last cropped for, `Integer.MIN_VALUE` means "not computed yet"
    private int labelWidth = Integer.MIN_VALUE;

    protected ConfigButton(
        int x,
        int y,
        int width,
        int height,
        ConfigEntry<?> entry,
        Screen screen,
        OnPress onPress
    ) {
        super(x, y, width, height, TextComponent.EMPTY, Asser.tNotNull(onPress, "onPress"), NO_TOOLTIP);
        this.entry = Asser.tNotNull(entry, "config entry");
        this.screen = Asser.tNotNull(screen, "screen");
        this.active = !entry.isReadOnly();
    }

    /// Create a button presenting the given config entry.
    ///
    /// The returned button is always usable as a widget, even for unsupported config
    /// entries: in that case it is simply disabled.
    public static ConfigButton of(
        int x,
        int y,
        int width,
        int height,
        ConfigEntry<?> entry,
        Screen screen
    ) {
        Asser.tNotNull(entry, "config entry");
        Asser.tNotNull(screen, "screen");

        if (entry.isCategory()) {
            return new UnsupportedConfigButton(
                x, y, width, height, entry, screen,
                "config categories are edited through their entries"
            );
        }

        var type = ReflectUtils.box(entry.binding().getDefaultType());
        if (type == Boolean.class) {
            return BoolConfigButton.create(x, y, width, height, entry, screen);
        }
        if (type.isEnum()) {
            return EnumConfigButton.create(x, y, width, height, entry, screen);
        }
        return EditConfigButton.create(x, y, width, height, entry, screen);
    }

    /// Width needed to display the value of a config entry without cropping, clamped
    /// into `[MIN_WIDTH, MAX_WIDTH]`.
    public static int preferredWidth(Font font, ConfigEntry<?> entry) {
        Asser.tNotNull(font, "font");
        Asser.tNotNull(entry, "config entry");

        int width = MIN_WIDTH;
        var type = ReflectUtils.box(entry.binding().getDefaultType());
        if (type == Boolean.class) {
            width = Math.max(width, Math.max(font.width(LABEL_ON), font.width(LABEL_OFF)));
        } else if (type.isEnum()) {
            var constants = (Enum<?>[]) type.getEnumConstants();
            for (var constant : constants) {
                width = Math.max(width, font.width(constant.name()));
            }
        } else {
            width = Math.max(width, font.width(LABEL_EDIT));
        }
        return Math.min(MAX_WIDTH, width + 8);
    }

    /// the text rendered on the button face, cropped by `refreshLabel()` if needed
    protected abstract Component valueLabel();

    /// collect tooltip lines describing the current value
    protected void appendTooltipLines(List<Component> lines) {
        if (entry.isReadOnly()) {
            lines.add(ProbeText.pjs("option.read_only").gray().unwrap());
        }
    }

    /// Report the result of a `ConfigEntry#set(Object)` attempt.
    ///
    /// The button value is always re-read from the config entry afterwards: a failed
    /// saving attempt still leaves the new value in memory, so the reported result and
    /// the actual value are not necessarily in sync.
    protected final void applyResult(AccessResult<?> result) {
        Asser.tNotNull(result, "result");
        this.lastResult = result.type() == AccessResult.ResultType.GOOD ? null : result;
        this.labelWidth = Integer.MIN_VALUE;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (this.width != this.labelWidth) {
            refreshLabel();
        }
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void refreshLabel() {
        var font = Minecraft.getInstance().font;
        var label = Asser.tNotNull(valueLabel(), "value label");
        int maxWidth = Math.max(8, this.width - 8);
        if (font.width(label) > maxWidth) {
            var plain = font.plainSubstrByWidth(label.getString(), maxWidth - 4);
            label = ProbeText.literal(plain + '…').unwrap();
        }
        this.labelWidth = this.width;
        setMessage(label);
    }

    public final void appendResultLines(List<Component> lines) {
        if (lastResult == null) {
            return;
        }
        var color = switch (lastResult.type()) {
            case INFO -> ChatFormatting.AQUA;
            case WARNING -> ChatFormatting.YELLOW;
            case ERROR -> ChatFormatting.RED;
            case GOOD -> ChatFormatting.WHITE;
        };
        var message = lastResult.message();
        lines.add(ProbeText
            .literal(message == null ? lastResult.type().name() : message)
            .color(color)
            .unwrap());
    }
}

/// A [ConfigButton] for `Boolean` config entries: green `on` / red `off`, click toggles.
final class BoolConfigButton extends ConfigButton {

    private final ConfigEntry<Boolean> booleanEntry;

    @SuppressWarnings("unchecked")
    static ConfigButton create(int x, int y, int width, int height, ConfigEntry<?> entry, Screen screen) {
        return new BoolConfigButton(x, y, width, height, (ConfigEntry<Boolean>) entry, screen);
    }

    private BoolConfigButton(int x, int y, int width, int height, ConfigEntry<Boolean> entry, Screen screen) {
        super(x, y, width, height, entry, screen, button -> ((BoolConfigButton) button).toggle());
        this.booleanEntry = entry;
    }

    private boolean current() {
        return Boolean.TRUE.equals(booleanEntry.getSafe().value());
    }

    private void toggle() {
        applyResult(booleanEntry.set(!current()));
    }

    @Override
    protected Component valueLabel() {
        return current() ? LABEL_ON : LABEL_OFF;
    }
}

/// A [ConfigButton] for `Enum` config entries: click cycles through enum constants,
/// shift-click cycles backwards.
final class EnumConfigButton extends ConfigButton {

    private final ConfigEntry<Enum<?>> enumEntry;
    private final Enum<?>[] values;

    @SuppressWarnings("unchecked")
    static ConfigButton create(int x, int y, int width, int height, ConfigEntry<?> entry, Screen screen) {
        return new EnumConfigButton(
            x, y, width, height,
            (ConfigEntry<Enum<?>>) entry,
            enumConstants(entry.binding().getDefaultType()),
            screen
        );
    }

    private static Enum<?>[] enumConstants(Class<?> type) {
        var constants = type.getEnumConstants();
        if (constants == null) {
            return new Enum<?>[0];
        }
        var ret = new Enum<?>[constants.length];
        for (int i = 0; i < constants.length; i++) {
            ret[i] = (Enum<?>) constants[i];
        }
        return ret;
    }

    private EnumConfigButton(
        int x,
        int y,
        int width,
        int height,
        ConfigEntry<Enum<?>> entry,
        Enum<?>[] values,
        Screen screen
    ) {
        super(x, y, width, height, entry, screen, button -> {
            var casted = (EnumConfigButton) button;
            casted.cycle(Screen.hasShiftDown() ? -1 : 1);
        });
        this.enumEntry = entry;
        this.values = values;
    }

    private int currentIndex() {
        var value = enumEntry.getSafe().value();
        if (value == null) {
            return -1;
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) {
                return i;
            }
        }
        return -1; // invalid value, maybe the config file was edited manually
    }

    private void cycle(int delta) {
        int amount = values.length;
        if (amount == 0) {
            return;
        }
        int current = currentIndex();
        int next = current < 0
            ? (delta > 0 ? 0 : amount - 1)
            : Math.floorMod(current + delta, amount);
        applyResult(enumEntry.set(values[next]));
    }

    @Override
    protected Component valueLabel() {
        int index = currentIndex();
        return ProbeText.literal(index < 0 ? "?" : values[index].name()).unwrap();
    }

    @Override
    protected void appendTooltipLines(List<Component> lines) {
        int selected = currentIndex();
        var window = EnumValueWindow.of(values.length, Math.max(selected, 0), MAX_ENUM_VALUE_LINES);
        for (int i = 0; i < window.shown(); i++) {
            int index = window.start() + i;
            var name = values[index].name();
            lines.add(index == selected
                ? ProbeText.literal("> " + name).green().unwrap()
                : ProbeText.literal(name).unwrap());
        }
        if (window.hidden() > 0) {
            var more = ProbeText.pjs("option.more_values", window.hidden()).gray().unwrap();
            lines.add(window.ellipsisAtTop() ? 0 : lines.size(), more);
        }
        super.appendTooltipLines(lines);
    }
}

/// A [ConfigButton] for everything else: opens [OptionEditScreen] with the serialized
/// value of the config entry.
final class EditConfigButton extends ConfigButton {

    private final ConfigSerde<String, ?> serde;

    @SuppressWarnings("unchecked")
    static ConfigButton create(int x, int y, int width, int height, ConfigEntry<?> entry, Screen screen) {
        var io = entry.getRoot().io();
        if (!(io instanceof SerdeHolder<?>)) {
            return new UnsupportedConfigButton(
                x, y, width, height, entry, screen,
                "config IO doesn't provide value serde"
            );
        }
        try {
            var holder = (SerdeHolder<Object>) io;
            var serde = holder.xmapToString(holder.getSerde((ConfigEntry<Object>) entry));
            return new EditConfigButton(x, y, width, height, entry, serde, screen);
        } catch (Exception e) {
            return new UnsupportedConfigButton(x, y, width, height, entry, screen, e.toString());
        }
    }

    private EditConfigButton(
        int x,
        int y,
        int width,
        int height,
        ConfigEntry<?> entry,
        ConfigSerde<String, ?> serde,
        Screen screen
    ) {
        super(x, y, width, height, entry, screen, button -> ((EditConfigButton) button).open());
        this.serde = serde;
    }

    private void open() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        OptionEditScreen next = new OptionEditScreen(screen, entry, serde);
        if (screen instanceof ProbeJSConfigScreen configScreen) {
            configScreen.openChild(next);
        } else {
            Minecraft.getInstance().setScreen(next);
        }
    }

    @Override
    protected Component valueLabel() {
        return LABEL_EDIT;
    }

    @Override
    protected void appendTooltipLines(List<Component> lines) {
        var preview = preview();
        if (preview != null) {
            lines.add(ProbeText.literal(preview).white().unwrap());
        }
        lines.add(ProbeText.pjs("option.edit_hint").gray().unwrap());
        super.appendTooltipLines(lines);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    private String preview() {
        try {
            var value = entry.getSafe().value();
            if (value == null) {
                return null;
            }
            var text = String.valueOf(((ConfigSerde) serde).serialize(value));
            return text.length() > 60 ? text.substring(0, 57) + "..." : text;
        } catch (Exception e) {
            return null;
        }
    }
}

/// Fallback [ConfigButton] for config entries that cannot be presented as a button.
/// It is always disabled, but still explains the reason through its tooltip.
final class UnsupportedConfigButton extends ConfigButton {

    private final String reason;

    UnsupportedConfigButton(
        int x,
        int y,
        int width,
        int height,
        ConfigEntry<?> entry,
        Screen screen,
        String reason
    ) {
        super(x, y, width, height, entry, screen, button -> {
        });
        this.reason = Asser.tNotNull(reason, "reason");
        this.active = false;
    }

    @Override
    protected Component valueLabel() {
        return LABEL_UNSUPPORTED;
    }

    @Override
    protected void appendTooltipLines(List<Component> lines) {
        lines.add(ProbeText.literal(reason).red().unwrap());
        super.appendTooltipLines(lines);
    }
}
