package zzzank.probejs.utils.config.io;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.val;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.JsonUtils;
import zzzank.probejs.utils.config.ConfigEntry;
import zzzank.probejs.utils.config.ConfigImpl;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.serde.ConfigSerdeFactory;

import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public class JsonConfigIO implements ConfigIO {
    public static final String DEFAULT_VALUE_KEY = "$default";
    public static final String VALUE_KEY = "$value";
    public static final String COMMENTS_KEY = "$comment";

    private final Gson gson;
    private final Map<Class<?>, ConfigSerde<?>> serdes = new ConcurrentHashMap<>();
    private final List<ConfigSerdeFactory> serdeFactories = new ArrayList<>();

    public static JsonConfigIO make(Gson gson, Consumer<JsonConfigIO> modifier) {
        val io = new JsonConfigIO(gson);
        modifier.accept(io);
        return io;
    }

    public JsonConfigIO(Gson gson) {
        this.gson = Asser.tNotNull(gson, "gson");
    }

    public <T> ConfigSerde<T> putSerde(Class<T> type, ConfigSerde<T> serde) {
        return cast(serdes.put(type, serde));
    }

    public <T> ConfigSerde<T> getSerde(Class<T> type) {
        val serde = serdes.computeIfAbsent(
            type, t -> Lists.reverse(serdeFactories)
                .stream()
                .map(serdeFactory -> serdeFactory.getSerde(type))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null)
        );
        return cast(serde);
    }

    /**
     * factories added in a later time will have higher priority
     */
    public synchronized void addSerdeFactory(ConfigSerdeFactory factory) {
        serdeFactories.add(Asser.tNotNull(factory, "serde factory"));
    }

    @SuppressWarnings("unchecked")
    private static <T> ConfigSerde<T> cast(ConfigSerde<?> serde) {
        return (ConfigSerde<T>) serde;
    }

    @Override
    public void read(ConfigImpl config, Reader reader) {
        val json = gson.fromJson(reader, JsonObject.class);
        for (val entry : json.entrySet()) {
            val namespaced = config.ensureNamespace(entry.getKey());
            val namespace = namespaced.getKey();
            val name = namespaced.getValue();

            val reference = (ConfigEntry<Object>) config.get(namespace, name);
            if (reference == null || reference.readOnly()) {
                continue;
            }

            val raw = entry.getValue().getAsJsonObject().get(VALUE_KEY);
            val serde = getSerde(reference.binding.getDefaultType());
            if (serde == null) {
                throw new IllegalStateException(String.format(
                    "No ConfigSerde available for config '%s' with type '%s'",
                    reference.path(),
                    reference.binding.getDefaultType().getName()
                ));
            }
            reference.setNoSave(serde.fromJson(raw));
        }
    }

    @Override
    public void save(ConfigImpl config, Writer writer) {
        val object = new JsonObject();
        for (val entry : config.entries()) {
            val o = new JsonObject();
            val serde = getSerde(entry.binding.getDefaultType());

            o.add(DEFAULT_VALUE_KEY, serde.toJson(Cast.to(entry.getDefault())));
            o.add(VALUE_KEY, serde.toJson(Cast.to(entry.get())));
            val comments = entry.getComments();
            switch (comments.size()) {
                case 0 -> {
                }
                case 1 -> o.add(COMMENTS_KEY, new JsonPrimitive(comments.get(0)));
                default -> o.add(COMMENTS_KEY, JsonUtils.parseObject(comments));
            }

            object.add(entry.path(), o);
        }
        gson.toJson(object, writer);
    }
}
