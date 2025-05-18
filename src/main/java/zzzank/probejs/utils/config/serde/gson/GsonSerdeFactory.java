package zzzank.probejs.utils.config.serde.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.serde.ConfigSerdeFactory;

import java.lang.reflect.Type;

/**
 * @author ZZZank
 */
public class GsonSerdeFactory implements ConfigSerdeFactory<JsonElement> {
    private final Gson gson;

    public GsonSerdeFactory(Gson gson) {
        this.gson = Asser.tNotNull(gson, "gson");
    }

    @Override
    public <T> ConfigSerde<JsonElement, T> getSerde(Type type) {
        try {
            return new GsonAdapterSerde<>(gson.getAdapter((TypeToken<T>) TypeToken.get(type)));
        } catch (Exception e) {
            return null;
        }
    }
}
