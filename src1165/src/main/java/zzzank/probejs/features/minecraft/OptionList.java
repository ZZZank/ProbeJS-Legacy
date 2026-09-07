package zzzank.probejs.features.minecraft;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
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

    private static final Component OPTION_ON = new TranslatableComponent("probejs.option.on").withStyle(style -> style.withColor(ChatFormatting.GREEN));
    private static final Component OPTION_OFF = new TranslatableComponent("probejs.option.off").withStyle(style -> style.withColor(ChatFormatting.RED));
    private static final Component OPTION_EDIT = new TranslatableComponent("probejs.option.edit");

    private final ProbeJSConfigScreen parent;
    private int maxNameWidth = 0;
    private Style currentHover = null;

    public OptionList(ProbeJSConfigScreen parent, Minecraft mc, ConfigCategory config) {
        super(mc,parent.width + 45, parent.height, 43, parent.height - 32, 20);
        this.parent = parent;

        var entriesByCategory = config.streamEntries()
            .collect(Collectors.groupingBy(ConfigEntry::parent));

        entriesByCategory.forEach((category, entries) -> {
            if (!category.isRoot()) {
                this.addEntry(new CategoryEntry(category));
            }

            for (var entry : entries) {
                if (entry.getPropOrDefault(ConfigProperty.INTERNAL)) {
                    continue;
                }
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

    @Override
    protected void renderDecorations(PoseStack poseStack, int mouseX, int mouseY) {
        if (currentHover != null) {
            parent.renderComponentHoverEffect(poseStack, currentHover, mouseX, mouseY);
            currentHover = null;
        }

        super.renderDecorations(poseStack, mouseX, mouseY);
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
                        .append(configEntry.getPropOrDefault(ConfigProperty.COMMENTS))
                        .unwrap()
                )
                .unwrap());
        }

        protected void drawTitle(PoseStack matrixStack, int x, int y, int mouseX, int mouseY) {
            var font = minecraft.font;

            font.draw(matrixStack, this.title, x, y, 0xffffff);

            if (mouseX >= x
                && mouseY >= y
                && mouseX <= (x + this.titleWidth)
                && mouseY <= (y + font.lineHeight)) {
                currentHover = this.title.getStyle();
            }
        }
    }

    class CategoryEntry extends Entry {
        private final int width;

        public CategoryEntry(ConfigCategory category) {
            super(category, category.path());
            this.width = OptionList.this.minecraft.font.width(this.title);
        }

        public void render(PoseStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            int x = OptionList.this.minecraft.screen.width / 2 - this.width / 2;
            int y = top + height - 10;

            drawTitle(matrixStack, x, y, mouseX, mouseY);
        }

        @Override
        public boolean changeFocus(boolean focus) {
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children() {
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
            this.editButton = new Button(
                0,
                0,
                55,
                20,
                getOptionMessage(this.entry),
                button -> {
                    if (onPress != null) {
                        onPress.onPress(button);
                    }
                    button.setMessage(getOptionMessage(this.entry));
                },
                (button, poseStack, x, y) -> {
                    if (lastMessage != null) {
                        OptionList.this.parent.renderTooltip(poseStack, ProbeText.literal(lastMessage).unwrap(), x, y);
                    }
                });
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
            PoseStack matrixStack,
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
            this.editButton.x = left + 175;
            this.editButton.y = top;
            this.editButton.render(matrixStack, mouseX, mouseY, partialTicks);

            int textX = left + DEPTH_OFFSET * depth(entry);
            int textY = top + height / 2 - 4;
            drawTitle(matrixStack, textX, textY, mouseX, mouseY);
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
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.editButton);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }
}