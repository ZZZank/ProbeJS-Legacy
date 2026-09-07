package zzzank.probejs.features.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.ProbeDumpingThread;
import zzzank.probejs.features.parchment.ParchmentMappingLoader;
import zzzank.probejs.utils.ProbeText;

/**
 * @author ZZZank
 */
public class ProbeJSDumpScreen extends Screen {
    private DumpMessageList list;

    public ProbeJSDumpScreen() {
        super(ProbeText.literal("ProbeJS Dump").unwrap());
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        var cleanOldDumpsCheckbox = new Checkbox(
            centerX - 155,
            40,
            150,
            20,
            new TextComponent("Clean Old Dumps"),
            true
        );

        var enableParchmentCheckbox = new Checkbox(
            centerX + 5,
            40,
            150,
            20,
            new TextComponent("Enable Parchment"),
            true
        );

        var dumpButton = new Button(
            centerX - 155,
            this.height - 52,
            150,
            20,
            new TextComponent("Start Dump"),
            button -> {
                if (ProbeDumpingThread.exists()) {
                    DumpMessageList.MessageEntry messageEntry = list.addEntry(ProbeText.literal("ALREADY_RUNNING"));
                    messageEntry.provideBody(ProbeText.pjs("already_running"));
                    return;
                }
                list.children().clear();

                ParchmentMappingLoader.ENABLED = enableParchmentCheckbox.selected();
                var thread = ProbeDumpingThread.create(
                    msg -> this.sendMessage(msg.unwrap().getString()),
                    text -> this.list.addEntry(text),
                    cleanOldDumpsCheckbox.selected()
                );
                thread.start();
            }
        );

        var configButton = new Button(
            centerX + 5,
            this.height - 52,
            150,
            20,
            new TextComponent("Config"),
            button -> this.minecraft.setScreen(new ProbeJSConfigScreen(this))
        );

        var doneButton = new Button(
            centerX + 5,
            this.height - 28,
            150,
            20,
            CommonComponents.GUI_DONE,
            button -> this.onClose()
        );

        var list = new DumpMessageList(
            minecraft,
            this.width,
            this.height,
            cleanOldDumpsCheckbox.y + cleanOldDumpsCheckbox.getHeight() + 10,
            configButton.y - 10,
            this
        );
        if (this.list != null) {
            list.replaceEntries(this.list.children());
        }
        list.setRenderTopAndBottom(false);
        list.setRenderBackground(false);

        this.addButton(cleanOldDumpsCheckbox);
        this.addButton(enableParchmentCheckbox);
        this.addButton(dumpButton);
        this.addButton(configButton);
        this.addButton(doneButton);
        this.list = this.addWidget(list);
    }

    protected Button addButton(int x, int y, int width, int height, ProbeText text, Button.OnPress onPress, Button.OnTooltip onTooltip) {
        if (onPress == null) {
            onPress = (button) -> {};
        }
        if (onTooltip == null) {
            onTooltip = (button, poseStack, mouseX, mouseY) -> {};
        }
        return this.addButton(new Button(x, y, width, height, text.unwrap(), onPress, onTooltip));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        this.list.render(poseStack, mouseX, mouseY, partialTicks);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderComponentHoverEffect(PoseStack matrixStack, @Nullable Style style, int mouseX, int mouseY) {
        super.renderComponentHoverEffect(matrixStack, style, mouseX, mouseY);
    }
}
