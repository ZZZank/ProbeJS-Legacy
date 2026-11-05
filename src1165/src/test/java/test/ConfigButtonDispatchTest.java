package test;

import com.google.gson.Gson;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.junit.jupiter.api.Test;
import zzzank.probejs.features.minecraft.ConfigButton;
import zzzank.probejs.utils.config.io.JsonConfigIO;
import zzzank.probejs.utils.config.serde.gson.GsonSerdeFactory;
import zzzank.probejs.utils.config.struct.ConfigEntry;
import zzzank.probejs.utils.config.struct.ConfigRootImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author ZZZank
 */
class ConfigButtonDispatchTest {

    enum TestEnum {
        NONE,
        EVENTS,
        FULL
    }

    private static final Screen SCREEN = new Screen(TextComponent.EMPTY) {
    };

    private static ConfigRootImpl newRoot() {
        var gson = new Gson();
        var io = new JsonConfigIO(gson);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));
        return new ConfigRootImpl(io, null);
    }

    private static ConfigButton buttonOf(ConfigEntry<?> entry) {
        return ConfigButton.of(0, 0, 60, 20, entry, SCREEN);
    }

    private static String typeOf(ConfigEntry<?> entry) {
        return buttonOf(entry).getClass().getSimpleName();
    }

    @Test
    void dispatchesSupportedTypes() {
        var root = newRoot();
        assertEquals("BoolConfigButton", typeOf(root.define("bool").bindDefault(true).build()));
        assertEquals("EnumConfigButton", typeOf(root.define("enum").bindDefault(TestEnum.EVENTS).build()));
        assertEquals("EditConfigButton", typeOf(root.define("text").bindDefault("hello").build()));
    }

    @Test
    void boolButtonTogglesValue() {
        var root = newRoot();
        var entry = root.define("bool").bindDefault(true).build();
        var button = buttonOf(entry);

        button.onPress();
        assertFalse(entry.get());

        button.onPress();
        assertTrue(entry.get());
    }

    @Test
    void readOnlyEntryIsDisabled() {
        var root = newRoot();
        var entry = root.define("read_only").bindReadOnly(3).build();
        assertFalse(buttonOf(entry).active);
    }

    @Test
    void configCategoryFallsBackToDisabledButton() {
        var root = newRoot();
        var button = buttonOf(root.defineSubCategory("sub"));
        assertEquals("UnsupportedConfigButton", button.getClass().getSimpleName());
        assertFalse(button.active);
    }
}
