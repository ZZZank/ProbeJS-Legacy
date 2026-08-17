package zzzank.probejs.features.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.ProbeConfig;

public class ProbeJSConfigScreen extends Screen {
    private final Screen lastScreen;

    private OptionList optionList;
    private Button doneButton;
    private Button wikiButton;
    double lastScrollAmount = 0;

    public ProbeJSConfigScreen(Screen lastScreen) {
        super(new TextComponent("ProbeJS Config"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.optionList = new OptionList(this, this.minecraft, ProbeConfig.INSTANCE);
        this.optionList.setScrollAmount(lastScrollAmount);

        this.children.add(this.optionList);
        this.wikiButton = new Button(
            this.width / 2 - 155,
            this.height - 29,
            150,
            20,
            new TextComponent("Wiki"),
            (arg) -> Util.getPlatform().openUri("https://kubejs.com/wiki/addons/third-party/probejs")
        );
        this.doneButton = new Button(
            this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_DONE, (arg) -> {
            this.onClose();
        }
        );
        this.addButton(this.wikiButton);
        this.addButton(this.doneButton);
        this.addWidget(this.optionList);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.optionList.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderComponentHoverEffect(PoseStack matrixStack, @Nullable Style style, int mouseX, int mouseY) {
        super.renderComponentHoverEffect(matrixStack, style, mouseX, mouseY);
    }
}