package zzzank.probejs.features.minecraft;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.ProbeText;

/**
 * @author ZZZank
 */
public class ProbeJSDumpScreen extends Screen {
    public ProbeJSDumpScreen() {
        super(ProbeText.literal("ProbeJS Dump").unwrap());
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}
