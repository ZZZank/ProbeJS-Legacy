package zzzank.probejs.features.minecraft;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.ProbeText;
import zzzank.probejs.utils.ReflectUtils;
import zzzank.probejs.utils.config.io.SerdeHolder;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.report.AccessResult;
import zzzank.probejs.utils.config.struct.ConfigCategory;
import zzzank.probejs.utils.config.struct.ConfigEntry;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OptionList extends ContainerObjectSelectionList<OptionList.Entry> {
    private static final int DEPTH_OFFSET = 20;

    private static final Component OPTION_ON = Component.translatable("probejs.option.on").withStyle(style -> style.withColor(ChatFormatting.GREEN));
    private static final Component OPTION_OFF = Component.translatable("probejs.option.off").withStyle(style -> style.withColor(ChatFormatting.RED));
    private static final Component OPTION_EDIT = Component.translatable("probejs.option.edit");

    private final ProbeJSConfigScreen parent;
    private int maxNameWidth = 0;

    public OptionList(ProbeJSConfigScreen parent, Minecraft mc, ConfigCategory config) {
        super(mc, parent.width + 45, parent.height, 43, parent.height - 32, 20);
        this.parent = parent;

        var entriesByCategory = config.streamEntries()
            .collect(Collectors.groupingBy(ConfigEntry::parent));

        entriesByCategory.forEach((category, entries) -> {
            if (!category.isRoot()) {
                this.addEntry(new CategoryEntry(category));
            }

            for (var entry : entries) {
                int w = this.minecraft.font.width(entry.name()) + DEPTH_OFFSET * depth(entry);
                this.maxNameWidth = Math.max(w, this.maxNameWidth);
                this.addEntry(new OptionEntry(entry));
            }
        });
    }

    private static int depth(ConfigEntry<?> entry) {
        int depth = -1; // exclude root

        var parent = entry.parent();
        while (parent != null) {
            depth++;
            parent = parent.parent();
        }

        return depth;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    @Override
    public int getRowWidth() {
        return 320;
    }

    public abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        protected final Component title;
        protected final int titleWidth;

        protected Entry(Component title) {
            this.title = title;
            this.titleWidth = minecraft.font.width(this.title);
        }

        protected Entry(ConfigEntry<?> configEntry, String displayName) {
            this(ProbeText.literal(displayName)
                .hover(
                    HoverEvent.Action.SHOW_TEXT,
                    ProbeText.literal("")
                        .append(ProbeText.literal(configEntry.name()))
                        .append("\n\n")
                        .append(String.join("\n", configEntry.getPropOrDefault(ConfigProperty.COMMENTS)))
                        .unwrap()
                )
                .unwrap());
        }

        protected void drawTitle(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
            var font = minecraft.font;

            guiGraphics.drawString(font, this.title, x, y, 0xffffff, false);

            if (mouseX >= x
                && mouseY >= y
                && mouseX <= (x + this.titleWidth)
                && mouseY <= (y + font.lineHeight)) {
                guiGraphics.renderComponentHoverEffect(font, this.title.getStyle(), mouseX, mouseY);
            }
        }
    }

    class CategoryEntry extends Entry {
        private final int width;

        public CategoryEntry(ConfigCategory category) {
            super(category, category.path());
            this.width = OptionList.this.minecraft.font.width(this.title);
        }

        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            int x = OptionList.this.minecraft.screen.width / 2 - this.width / 2;
            int y = top + height - 10;

            drawTitle(guiGraphics, x, y, mouseX, mouseY);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
    }

    class OptionEntry extends Entry {
        private final Button editButton;
        private final ConfigEntry<?> entry;
        String lastMessage;

        public OptionEntry(ConfigEntry<?> entry) {
            super(entry, entry.name());
            this.entry = entry;

            var onPress = buildButtonAction(entry);
            this.editButton = Button.builder(getOptionMessage(this.entry), button -> {
                    if (onPress != null) {
                        onPress.onPress(button);
                    }
                    button.setMessage(getOptionMessage(this.entry));
                })
                .bounds(0, 0, 55, 20)
                .createNarration(supplier -> Component.literal(""))
                .build();
            if (onPress == null) {
                this.editButton.active = false;
                this.lastMessage = "Not supported config type";
            }
        }

        private <T> Button.OnPress buildButtonAction(ConfigEntry<T> configEntry) {
            if (ReflectUtils.box(configEntry.binding().getDefaultType()) == Boolean.class) {
                @SuppressWarnings("unchecked")
                var casted = ((ConfigEntry<Boolean>) configEntry);
                return button -> {
                    var result = casted.set(!casted.get());
                    if (result.type() != AccessResult.ResultType.GOOD) {
                        lastMessage = result.type() + "\n" + result.message();
                    }
                };
            }
            var io = entry.getRoot().io();
            if (io instanceof SerdeHolder<?>) {
                @SuppressWarnings("unchecked")
                var serdeHolder = (SerdeHolder<Object>) io;
                var serde = serdeHolder.xmapToString(serdeHolder.getSerde(configEntry));
                return button -> {
                    OptionList.this.parent.lastScrollAmount = OptionList.this.getScrollAmount();
                    OptionList.this.minecraft.setScreen(new OptionEditScreen<>(
                        OptionList.this.parent,
                        configEntry,
                        serde
                    ));
                };
            }
            return null;
        }

        @Override
        public void render(
            @NotNull GuiGraphics guiGraphics,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean isMouseOver,
            float partialTicks
        ) {
            this.editButton.setX(left + 175);
            this.editButton.setY(top);
            this.editButton.render(guiGraphics, mouseX, mouseY, partialTicks);

            int textX = left + DEPTH_OFFSET * depth(entry);
            int textY = top + height / 2 - 4;
            drawTitle(guiGraphics, textX, textY, mouseX, mouseY);
        }

        private Component getOptionMessage(ConfigEntry<?> configEntry) {
            if (ReflectUtils.box(configEntry.binding().getDefaultType()) == Boolean.class) {
                @SuppressWarnings("unchecked")
                var casted = ((ConfigEntry<Boolean>) configEntry);
                return casted.get() ? OPTION_ON : OPTION_OFF;
            }
            return OPTION_EDIT;
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.editButton);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.editButton);
        }
    }
}
