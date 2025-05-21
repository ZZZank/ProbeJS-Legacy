package zzzank.probejs.features.interop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.val;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.features.bridge.Command;
import zzzank.probejs.utils.registry.RegistryInfo;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.util.stream.Stream;

public abstract class ListRegistryCommand extends Command {

    protected abstract Stream<ResourceLocation> getItems(RegistryInfo registry);

    @Override
    public JsonElement handle(JsonObject payload) {
        String registryKey = payload.get("registry").getAsString();

        for (val info : RegistryInfos.values()) {
            val key = info.resourceKey();
            val registryName = key.location().getNamespace().equals("minecraft") ?
                key.location().getPath() :
                key.location().toString();
            if (!registryKey.equals(registryName)) {
                continue;
            }

            val result = new JsonArray();
            getItems(info)
                .map(ResourceLocation::toString)
                .map(JsonPrimitive::new)
                .forEach(result::add);
            return result;
        }
        return new JsonArray();
    }

    public static class Objects extends ListRegistryCommand {

        @Override
        public String identifier() {
            return "list_registry_items";
        }

        @Override
        protected Stream<ResourceLocation> getItems(RegistryInfo registry) {
            return registry.objectIds();
        }
    }

    public static class Tags extends ListRegistryCommand {

        @Override
        public String identifier() {
            return "list_registry_tags";
        }

        @Override
        protected Stream<ResourceLocation> getItems(RegistryInfo registry) {
            return registry.tagIds();
        }
    }
}
