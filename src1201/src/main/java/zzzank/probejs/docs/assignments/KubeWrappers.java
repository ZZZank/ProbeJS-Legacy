package zzzank.probejs.docs.assignments;

import dev.latvian.mods.rhino.mod.util.color.Color;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

/**
 * @author ZZZank
 */
public class KubeWrappers implements ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        scriptDump.assignType(Component.class, Types.type(Component.class).asArray());
        scriptDump.assignType(Component.class, "ComponentObject", Types.object()
            .member("text", true, Types.STRING)
            .member("translate", true, Types.primitive("Special.LangKey"))
            .member("with", true, Types.ANY.asArray())
            .member("color", true, Types.type(Color.class))
            .member("bold", true, Types.BOOLEAN)
            .member("italic", true, Types.BOOLEAN)
            .member("underlined", true, Types.BOOLEAN)
            .member("strikethrough", true, Types.BOOLEAN)
            .member("obfuscated", true, Types.BOOLEAN)
            .member("insertion", true, Types.STRING)
            .member("font", true, Types.STRING)
            .member("click", true, Types.type(ClickEvent.class))
            .member("hover", true, Types.type(Component.class))
            .member("extra", true, Types.type(Component.class).asArray())
            .build());
        scriptDump.assignType(Component.class, Types.STRING);
        scriptDump.assignType(Component.class, Types.NUMBER);
        scriptDump.assignType(Component.class, Types.BOOLEAN);
    }
}
