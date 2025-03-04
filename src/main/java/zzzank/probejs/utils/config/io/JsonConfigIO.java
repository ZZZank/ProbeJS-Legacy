package zzzank.probejs.utils.config.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.val;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.JsonUtils;
import zzzank.probejs.utils.config.ConfigEntry;
import zzzank.probejs.utils.config.ConfigImpl;

import java.io.Reader;
import java.io.Writer;

/**
 * @author ZZZank
 */
public class JsonConfigIO implements ConfigIO {
    public static final String DEFAULT_VALUE_KEY = "$default";
    public static final String VALUE_KEY = "$value";
    public static final String COMMENTS_KEY = "$comment";

    @Override
    public void read(ConfigImpl config, Reader reader) {
        val json = ProbeJS.GSON.fromJson(reader, JsonObject.class);
        for (val entry : json.entrySet()) {
            val namespaced = config.ensureNamespace(entry.getKey());
            val namespace = namespaced.getKey();
            val name = namespaced.getValue();

            val reference = (ConfigEntry<Object>) config.get(namespace, name);
            if (reference == null || reference.readOnly()) {
                continue;
            }

            val raw = entry.getValue().getAsJsonObject().get(VALUE_KEY);
            reference.setNoSave(reference.serde.fromJson(raw));
        }
    }

    @Override
    public void save(ConfigImpl config, Writer writer) {
        val object = new JsonObject();
        for (val entry : config.entries()) {
            val o = new JsonObject();

            o.add(DEFAULT_VALUE_KEY, entry.serde.toJson(Cast.to(entry.getDefault())));
            o.add(VALUE_KEY, entry.serde.toJson(Cast.to(entry.get())));
            val comments = entry.getComments();
            switch (comments.size()) {
                case 0 -> {}
                case 1 -> o.add(COMMENTS_KEY, new JsonPrimitive(comments.get(0)));
                default -> o.add(COMMENTS_KEY, JsonUtils.parseObject(comments));
            }

            object.add(config.stripNamespace(entry.namespace, entry.name), o);
        }
        ProbeJS.GSON_WRITER.toJson(object, writer);
    }
}
