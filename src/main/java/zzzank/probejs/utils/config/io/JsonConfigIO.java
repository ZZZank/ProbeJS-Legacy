package zzzank.probejs.utils.config.io;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.val;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.JsonUtils;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.serde.holder.AbstractSerdeHolder;
import zzzank.probejs.utils.config.struct.ConfigCategory;
import zzzank.probejs.utils.config.struct.ConfigRoot;

import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public class JsonConfigIO extends AbstractSerdeHolder<JsonElement> implements ConfigIO {
    public static final String DEFAULT_VALUE_KEY = "$default";
    public static final String VALUE_KEY = "$value";
    public static final String COMMENTS_KEY = "$comment";

    private final Gson gson;

    public static JsonConfigIO make(Gson gson, Consumer<JsonConfigIO> modifier) {
        val io = new JsonConfigIO(gson);
        modifier.accept(io);
        return io;
    }

    public JsonConfigIO(Gson gson) {
        this.gson = Asser.tNotNull(gson, "gson");
    }

    @Override
    public void read(ConfigRoot config, Reader reader) {
        val json = gson.fromJson(reader, JsonObject.class);
        readCategory(config, json);
    }

    private void readCategory(ConfigCategory category, JsonObject config) {
        for (val entry : category.get().values()) {
            val name = entry.name();
            val entryInConfig = config.getAsJsonObject(name);
            if (entryInConfig == null) {
                continue;
            }
            if (entry.isCategory()) {
                readCategory(entry.asCategory(), entryInConfig);
                continue;
            }
            val valueInConfig = entryInConfig.get(VALUE_KEY);
            if (valueInConfig == null) {
                continue;
            }
            val serde = serdeByEntryRequired(entry);
            entry.set(Cast.to(serde.deserialize(valueInConfig)));
        }
    }

    @Override
    public void save(ConfigRoot config, Writer writer) {
        val object = new JsonObject();
        writeCategory(config, object);
        gson.toJson(object, writer);
    }

    private void writeCategory(ConfigCategory category, JsonObject writeTo) {
        for (val entry : category.get().values()) {
            val name = entry.name();
            if (entry.isCategory()) {
                val entryJson = new JsonObject();
                writeCategory(entry.asCategory(), entryJson);
                writeTo.add(name, entryJson);
                continue;
            }

            val serde = serdeByEntryRequired(entry);

            val entryJson = new JsonObject();

            entryJson.add(DEFAULT_VALUE_KEY, serde.serialize(Cast.to(entry.binding().getDefault())));
            entryJson.add(VALUE_KEY, serde.serialize(Cast.to(entry.get())));
            val comments = entry.getProp(ConfigProperty.COMMENTS).orElse(Collections.emptyList());
            switch (comments.size()) {
                case 0 -> {
                }
                case 1 -> entryJson.add(COMMENTS_KEY, new JsonPrimitive(comments.get(0)));
                default -> entryJson.add(COMMENTS_KEY, JsonUtils.parseObject(comments));
            }

            writeTo.add(name, entryJson);
        }
    }
}
