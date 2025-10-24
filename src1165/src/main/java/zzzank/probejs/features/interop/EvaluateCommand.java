package zzzank.probejs.features.interop;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.script.ScriptManager;
import dev.latvian.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.NativeJavaObject;
import lombok.val;
import zzzank.probejs.features.bridge.Command;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.JsonUtils;

public class EvaluateCommand extends Command {
    @Override
    public String identifier() {
        return "evaluate";
    }

    @Override
    public JsonElement handle(JsonObject payload) {
        val scriptType = payload.get("scriptType").getAsString();
        val content = payload.get("content").getAsString();

        ScriptManager scriptManager = switch (scriptType) {
            case "startup_scripts" -> KubeJS.startupScriptManager;
            case "client_scripts" -> KubeJS.clientScriptManager;
            case "server_scripts" -> ServerScriptManager.instance.scriptManager;
            default -> throw new RuntimeException("Unable to get script manager.");
        };

        //well, there's one and only one ScriptPack in so-called scriptManager.pack`s`
        val pack = CollectUtils.anyIn(scriptManager.packs.values());
        if (pack == null) {
            throw new RuntimeException("Unable to get script context or scope.");
        }
        Object result = pack.context.evaluateString(pack.scope, content, "probejsEvaluator", 1, null);
        if (result instanceof NativeJavaObject nativeJavaObject) {
            result = nativeJavaObject.unwrap();
        }
        JsonElement jsonElement = JsonUtils.parseObject(result);
        if (jsonElement == JsonNull.INSTANCE && result != null) {
            jsonElement = new JsonPrimitive(result.toString());
        }
        return jsonElement;
    }
}
