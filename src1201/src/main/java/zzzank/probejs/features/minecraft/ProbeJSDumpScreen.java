package zzzank.probejs.features.minecraft;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeDumpingThread;
import zzzank.probejs.features.parchment.ParchmentMappingLoader;
import zzzank.probejs.utils.ProbeText;

/**
 * @author ZZZank
 */
public class ProbeJSDumpScreen extends Screen {
    private DumpMessageList list;

    public ProbeJSDumpScreen() {
        super(Component.literal("ProbeJS Dump"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        var cleanOldDumpsCheckbox = new Checkbox(
            centerX - 155,
            40,
            150,
            20,
            Component.literal("Clean Old Dumps"),
            true
        );

        var enableParchmentCheckbox = new Checkbox(
            centerX + 5,
            40,
            150,
            20,
            Component.literal("Enable Parchment"),
            true
        );

        var dumpButton = Button.builder(
                Component.literal("Start Dump"),
                button -> {
                    if (ProbeDumpingThread.exists()) {
                        DumpMessageList.MessageEntry messageEntry = list.addEntry(ProbeText.literal("ALREADY_RUNNING"));
                        messageEntry.provideBody(ProbeText.pjs("already_running"));
                        return;
                    }
                    list.children().clear();

                    ParchmentMappingLoader.ENABLED = enableParchmentCheckbox.selected();
                    var thread = ProbeDumpingThread.create(
                        msg -> this.minecraft.gui.getChat().addMessage(msg.unwrap()),
                        text -> this.list.addEntry(text),
                        cleanOldDumpsCheckbox.selected()
                    );
                    thread.start();
                })
            .bounds(centerX - 155, this.height - 52, 150, 20)
            .build();

        var configButton = Button.builder(
                Component.literal("Config"),
                button -> this.minecraft.setScreen(new ProbeJSConfigScreen(this)))
            .bounds(centerX + 5, this.height - 52, 150, 20)
            .build();

        var doneButton = Button.builder(
                CommonComponents.GUI_DONE,
                button -> this.minecraft.setScreen(null))
            .bounds(centerX + 5, this.height - 28, 150, 20)
            .build();

        var list = new DumpMessageList(
            minecraft,
            this.width,
            this.height,
            cleanOldDumpsCheckbox.getY() + cleanOldDumpsCheckbox.getHeight() + 10,
            configButton.getY() - 10,
            this
        );
        if (this.list != null) {
            list.replaceEntries(this.list.children());
        }
        list.setRenderBackground(false);
        list.setRenderTopAndBottom(false);

        this.addRenderableWidget(cleanOldDumpsCheckbox);
        this.addRenderableWidget(enableParchmentCheckbox);
        this.addRenderableWidget(dumpButton);
        this.addRenderableWidget(configButton);
        this.addRenderableWidget(doneButton);
        this.list = this.addRenderableWidget(list);
    }

    @Override
    public void tick() {
        if (this.list != null) {
            for (var entry : this.list.children()) {
                entry.tick();
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}
