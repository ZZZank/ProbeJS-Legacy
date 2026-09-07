package zzzank.probejs.features.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import zzzank.probejs.utils.ProbeText;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class DumpMessageList extends AbstractSelectionList<DumpMessageList.MessageEntry> {
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
    protected void replaceEntries(Collection<MessageEntry> entries) {
        super.replaceEntries(entries);
    }

    @Override
    protected void renderList(PoseStack matrixStack, int x, int y, int mouseX, int mouseY, float partialTicks) {
        var font = minecraft.font;
        headWidth = this.children()
            .stream()
            .mapToInt(entry -> font.width(entry.head.unwrap()))
            .max()
            .orElse(0) + 2;
        super.renderList(matrixStack, x, y, mouseX, mouseY, partialTicks);
    }

    public class MessageEntry extends AbstractSelectionList.Entry<DumpMessageList.MessageEntry> {
        public final ProbeText head;
        private ProbeText body = ProbeText.literal("<none>");
        private Supplier<ProbeText> bodySupplier;

        MessageEntry(ProbeText head) {
            this.head = head;
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
            var font = minecraft.font;
            var resolvedBody = resolveBody();

            font.draw(matrixStack, head.unwrap(), left, top, 0xffffff);
            font.draw(matrixStack, resolvedBody.unwrap(), left + headWidth, top, 0xffffff);

            if (isMouseOver) {
                screen.renderComponentHoverEffect(matrixStack, resolvedBody.getStyle(), mouseX, mouseY);
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
    }
}
