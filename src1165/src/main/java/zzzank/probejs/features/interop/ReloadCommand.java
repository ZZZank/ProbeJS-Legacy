package zzzank.probejs.features.interop;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import zzzank.probejs.features.bridge.Command;

public class ReloadCommand extends Command {
    @Override
    public String identifier() {
        return "reload";
    }

    @Override
    public JsonElement handle(JsonObject payload) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new RuntimeException("No current server found.");
        }

        switch (payload.get("scriptType").getAsString()) {
            case "server_scripts" -> runCommand(server, "kubejs reload server_scripts");
            case "startup_scripts" -> runCommand(server, "kubejs reload startup_scripts");
            case "client_scripts" -> runCommand(server, "kubejs reload client_scripts");
            case "reload" -> runCommand(server, "reload");
        }

        return JsonNull.INSTANCE;
    }

    public static void runCommand(MinecraftServer server, String command) {
        server.getCommands().performCommand(server.createCommandSourceStack(), command);
    }
}
