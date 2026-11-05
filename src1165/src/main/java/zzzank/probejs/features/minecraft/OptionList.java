package zzzank.probejs.features.minecraft;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.utils.ProbeText;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.struct.ConfigCategory;
import zzzank.probejs.utils.config.struct.ConfigEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OptionList extends ContainerObjectSelectionList<OptionList.Entry> {
    private final ProbeJSConfigScreen parent;
    private final int valueWidth;
    private List<Component> deferredTooltipLines = null;

    public OptionList(ProbeJSConfigScreen parent, Minecraft mc, ConfigCategory config) {
        // mc, width, height, y0, y1, itemHeight
        super(mc, parent.width + 45, parent.height, 43, parent.height - 32, 20);
        this.parent = parent;

        var entriesByCategory = config.streamEntries()
            .filter(entry -> !entry.getPropOrDefault(ConfigProperty.INTERNAL))
            .collect(Collectors.groupingBy(
                ConfigEntry::parent,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        // all buttons share the same width, so the value column stays aligned
        this.valueWidth = config.streamEntries()
            .mapToInt(entry -> ConfigButton.preferredWidth(mc.font, entry))
            .max()
            .orElse(ConfigButton.MIN_WIDTH);

        entriesByCategory.forEach((category, entries) -> {
            if (!category.isRoot()) {
                this.addEntry(new CategoryEntry(category));
            }
            for (var entry : entries) {
                this.addEntry(new OptionEntry(entry));
            }
        });
    }

    @Override
    public int getRowWidth() {
        return 320;
    }

    @Override
    protected void renderDecorations(PoseStack poseStack, int mouseX, int mouseY) {
        if (deferredTooltipLines != null) {
            parent.renderComponentTooltip(poseStack, deferredTooltipLines, mouseX, mouseY);
            deferredTooltipLines = null;
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
                        .append("\n")
                        .append(configEntry.getPropOrDefault(ConfigProperty.COMMENTS))
                        .unwrap()
                )
                .unwrap());
        }

        protected void drawTitle(PoseStack matrixStack, int x, int y, int mouseX, int mouseY) {
            var font = minecraft.font;

            font.draw(matrixStack, this.title, x, y, 0xffffff);

            var hovered = getHoveredLines(x, y, mouseX, mouseY);
            if (hovered != null) {
                deferredTooltipLines = hovered;
            }
        }

        @Nullable
        protected List<Component> getHoveredLines(int x, int y, int mouseX, int mouseY) {
            var font = minecraft.font;

            if (mouseX >= x && mouseY >= y && mouseX <= (x + this.titleWidth) && mouseY <= (y + font.lineHeight)) {
                return this.title.getStyle()
                    .getHoverEvent()
                    .getValue(HoverEvent.Action.SHOW_TEXT)
                    .getSiblings();
            }
            return null;
        }
    }

    class CategoryEntry extends Entry {
        public CategoryEntry(ConfigCategory category) {
            super(category, category.path());
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
            int x = parent.width / 2 - this.titleWidth / 2;
            int y = top + height - 10;
            drawCenteredString(matrixStack, minecraft.font, title, x, y, 0xffffff);
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
        private final ConfigButton button;

        public OptionEntry(ConfigEntry<?> entry) {
            super(entry, entry.name());
            this.button = ConfigButton.of(
                0,
                0,
                OptionList.this.valueWidth,
                20,
                entry,
                OptionList.this.parent
            );
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
            drawTitle(matrixStack, left, top + height / 2 - 4, mouseX, mouseY);

            this.button.x = getScrollbarPosition() - this.button.getWidth();
            this.button.y = top;
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        protected List<Component> getHoveredLines(int x, int y, int mouseX, int mouseY) {
            if (button.isMouseOver(mouseX, mouseY)) {
                var result = new ArrayList<Component>();
                button.appendTooltipLines(result);
                button.appendResultLines(result);
                return result;
            }

            return super.getHoveredLines(x, y, mouseX, mouseY);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.button);
        }
    }
}
