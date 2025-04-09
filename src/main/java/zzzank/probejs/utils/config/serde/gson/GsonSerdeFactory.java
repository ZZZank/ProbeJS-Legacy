package zzzank.probejs.utils.config.serde.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.serde.ConfigSerdeFactory;

/**
 * @author ZZZank
 */
public class GsonSerdeFactory implements ConfigSerdeFactory<JsonElement> {
    private final Gson gson;

    public GsonSerdeFactory(Gson gson) {
        this.gson = Asser.tNotNull(gson, "gson");
    }

    @Override
    public <T> ConfigSerde<JsonElement, T> getSerde(Class<T> type) {
        try {
            return new GsonAdapterSerde<>(gson.getAdapter(type));
        } catch (Exception e) {
            return null;
        }
    }
}
