package zzzank.probejs.features.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.ProbeText;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class DumpMessageList extends ContainerObjectSelectionList<DumpMessageList.MessageEntry> {
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
            var resolvedBody = resolveBody();

            guiGraphics.drawString(font, head.unwrap(), left, top, 0xffffff, false);
            guiGraphics.drawString(font, resolvedBody.unwrap(), left + headWidth, top, 0xffffff, false);

            if (isMouseOver) {
                guiGraphics.renderComponentHoverEffect(font, resolvedBody.getStyle(), mouseX, mouseY);
            }
        }

        /**
         * Set a dynamic body provider that is evaluated on each render frame.
         * Useful for showing live progress during long-running blocking operations.
         */
        public void provideBody(Supplier<ProbeText> supplier) {
            this.bodySupplier = supplier;
        }

        /**
         * Set a static body text and disable any dynamic provider.
         */
        public void provideBody(ProbeText text) {
            this.bodySupplier = null;
            this.body = text;
        }

        private ProbeText resolveBody() {
            if (bodySupplier != null) {
                return bodySupplier.get();
            }
            return body;
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
