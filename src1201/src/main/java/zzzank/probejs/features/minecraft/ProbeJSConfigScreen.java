package zzzank.probejs.features.minecraft;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeConfig;

public class ProbeJSConfigScreen extends Screen {
    private final Screen lastScreen;

    private OptionList optionList;
    private Button doneButton;
    private Button wikiButton;
    double lastScrollAmount = 0;

    public ProbeJSConfigScreen(Screen lastScreen) {
        super(Component.literal("ProbeJS Config"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.optionList = new OptionList(this, this.minecraft, ProbeConfig.INSTANCE);
        this.optionList.setScrollAmount(lastScrollAmount);

        this.wikiButton = Button.builder(
                Component.literal("ProbeJS Legacy GitHub"),
                (arg) -> Util.getPlatform().openUri("https://github.com/ZZZank/ProbeJS-Legacy"))
            .bounds(this.width / 2 - 155, this.height - 29, 150, 20)
            .build();
        this.doneButton = Button.builder(
                CommonComponents.GUI_DONE,
                (arg) -> this.onClose())
            .bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
            .build();
        this.addRenderableWidget(this.optionList);
        this.addRenderableWidget(this.wikiButton);
        this.addRenderableWidget(this.doneButton);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xffffff);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}
