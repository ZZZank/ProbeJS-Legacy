package zzzank.probejs.features.minecraft;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.ProbeConfig;

public class ProbeJSConfigScreen extends Screen {
    @Nullable
    private final Screen lastScreen;

    private OptionList optionList;
    private double lastScrollAmount = 0;

    public ProbeJSConfigScreen(@Nullable Screen lastScreen) {
        super(Component.literal("ProbeJS Config"));
        this.lastScreen = lastScreen;
    }

    /// Switch to a child screen (e.g. [OptionEditScreen]) while remembering the current
    /// scroll position of the option list, so that it can be restored when coming back.
    void openChild(Screen child) {
        if (this.optionList != null) {
            this.lastScrollAmount = this.optionList.getScrollAmount();
        }
        this.minecraft.setScreen(child);
    }

    @Override
    protected void init() {
        this.optionList = new OptionList(this, this.minecraft, ProbeConfig.INSTANCE);
        this.optionList.setScrollAmount(lastScrollAmount);

        var wikiButton = Button.builder(
                Component.literal("ProbeJS Legacy GitHub"),
                (arg) -> Util.getPlatform().openUri("https://github.com/ZZZank/ProbeJS-Legacy"))
            .bounds(this.width / 2 - 155, this.height - 29, 150, 20)
            .build();
        var doneButton = Button.builder(
                CommonComponents.GUI_DONE,
                (arg) -> this.minecraft.setScreen(lastScreen))
            .bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
            .build();
        this.addRenderableWidget(this.optionList);
        this.addRenderableWidget(wikiButton);
        this.addRenderableWidget(doneButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xffffff);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}
