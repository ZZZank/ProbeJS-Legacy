package zzzank.probejs.features.minecraft;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.config.report.AccessResult;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.struct.ConfigEntry;

/**
 * @author ZZZank
 */
class OptionEditScreen<T> extends Screen {
    private final Screen lastScreen;
    private final ConfigEntry<T> configEntry;
    private final ConfigSerde<String, T> serde;
    private EditBox editBox;
    private String errorMessage;

    OptionEditScreen(Screen lastScreen, ConfigEntry<T> configEntry, ConfigSerde<String, T> serde) {
        super(Component.translatable("probejs.option.edit"));
        this.lastScreen = lastScreen;
        this.configEntry = configEntry;
        this.serde = serde;
    }

    @Override
    protected void init() {
        super.init();
        String initialValue = serde.serialize(configEntry.get());
        this.editBox = new EditBox(
            this.font,
            this.width / 2 - 150,
            this.height / 2 - 10,
            300,
            20,
            Component.literal(initialValue)
        );
        this.editBox.setMaxLength(32767);
        this.editBox.setValue(initialValue);
        this.addWidget(this.editBox);

        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> {
                    try {
                        var newValue = serde.deserialize(this.editBox.getValue());
                        var result = configEntry.set(newValue);
                        if (result.type() != AccessResult.ResultType.GOOD) {
                            errorMessage = result.message();
                        } else {
                            this.minecraft.setScreen(lastScreen);
                        }
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                    }
                })
            .bounds(this.width / 2 - 155, this.height / 2 + 20, 150, 20)
            .build());

        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_CANCEL,
                button -> this.minecraft.setScreen(lastScreen))
            .bounds(this.width / 2 + 5, this.height / 2 + 20, 150, 20)
            .build());

        this.setInitialFocus(this.editBox);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 30, 16777215);
        this.editBox.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (errorMessage != null) {
            guiGraphics.drawCenteredString(this.font, errorMessage, this.width / 2, this.height / 2 - 50, 0xFF5555);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }
}
