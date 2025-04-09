package zzzank.probejs.utils.config.serde.gson;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.serde.ConfigSerde;

/**
 * @author ZZZank
 */
public class GsonAdapterSerde<T> implements ConfigSerde<JsonElement, T> {
    private final TypeAdapter<T> typeAdapter;

    public GsonAdapterSerde(TypeAdapter<T> typeAdapter) {
        this.typeAdapter = Asser.tNotNull(typeAdapter, "type adapter");
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull T value) {
        return typeAdapter.toJsonTree(value);
    }

    @Override
    public @NotNull T deserialize(@NotNull JsonElement json) {
        return typeAdapter.fromJsonTree(json);
    }
}
