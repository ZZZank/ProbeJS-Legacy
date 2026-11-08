package zzzank.probejs.features.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.ProbeText;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class DumpMessageList extends ContainerObjectSelectionList<DumpMessageList.MessageEntry> {
    private static final int LIST_HORIZONTAL_PADDING = 10;

    public final ProbeJSDumpScreen screen;
    private int headWidth;

    public DumpMessageList(Minecraft mc, int width, int height, int top, int bottom, ProbeJSDumpScreen screen) {
        super(mc, width, height, top, bottom, mc.font.lineHeight * 2);
        this.screen = screen;
    }

    @Override
    public int getRowWidth() {
        // 155 * 2 + 10, 2 buttons + padding
        return 310;
    }

    @Override
    protected int getScrollbarPosition() {
        return width - LIST_HORIZONTAL_PADDING;
    }

    public MessageEntry addEntry(ProbeText text) {
        var entry = new MessageEntry(text);
        this.addEntry(entry);
        return entry;
    }

    @Override
    protected void replaceEntries(@NotNull Collection<MessageEntry> entries) {
        super.replaceEntries(entries);
    }

    @Override
    protected void renderList(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var font = minecraft.font;
        headWidth = this.children()
            .stream()
            .mapToInt(entry -> font.width(entry.head.unwrap()))
            .max()
            .orElse(0) + 2;
        super.renderList(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public class MessageEntry extends ContainerObjectSelectionList.Entry<DumpMessageList.MessageEntry> {
        public final ProbeText head;
        private ProbeText body = ProbeText.literal("<none>");
        private Supplier<ProbeText> bodySupplier;

        MessageEntry(ProbeText head) {
            this.head = head;
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
            var font = minecraft.font;

            guiGraphics.drawString(font, head.unwrap(), LIST_HORIZONTAL_PADDING, top, 0xffffff, false);

            if (body != null) {
                guiGraphics.drawString(font, body.unwrap(), LIST_HORIZONTAL_PADDING + headWidth, top, 0xffffff, false);
                if (isMouseOver) {
                    guiGraphics.renderComponentHoverEffect(font, body.getStyle(), mouseX, mouseY);
                }
            }
        }

        /**
         * Set a dynamic body provider that is evaluated on each render frame.
         * Useful for showing live progress during long-running blocking operations.
         */
        public void provideBody(Supplier<ProbeText> supplier) {
            this.bodySupplier = supplier;
            this.body = null;
        }

        /**
         * Set a static body text and disable any dynamic provider.
         */
        public void provideBody(ProbeText text) {
            this.bodySupplier = null;
            this.body = installOptionalHover(text);
        }

        public void tick() {
            if (bodySupplier != null) {
                body = installOptionalHover(bodySupplier.get());
            }
        }

        /// add "display self on hover" if there's no existed hover
        private static ProbeText installOptionalHover(ProbeText text) {
            if (text.getStyle().getHoverEvent() == null) {
                text = text.hover(HoverEvent.Action.SHOW_TEXT, text.unwrap());
            }
            return text;
        }

        public ProbeText getBody() {
            return body;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return java.util.Collections.emptyList();
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }
    }
}
