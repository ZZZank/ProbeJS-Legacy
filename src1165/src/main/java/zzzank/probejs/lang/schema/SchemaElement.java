package zzzank.probejs.lang.schema;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import lombok.val;
import zzzank.probejs.utils.JsonUtils;

import java.util.*;

public abstract class SchemaElement<T extends SchemaElement<T>> {
    protected final List<Object> enums = new ArrayList<>();
    protected final Map<String, Object> additional = new HashMap<>();

    public abstract String getType();

    protected abstract JsonObject toSchema();

    public JsonObject getSchema() {
        val object = toSchema();
        object.addProperty("type", getType());
        if (!enums.isEmpty()) {
            object.add("enum", JsonUtils.parseObject(enums));
        }
        for (val entry : additional.entrySet()) {
            val element = JsonUtils.parseObject(entry.getValue());
            if (element == JsonNull.INSTANCE) {
                continue;
            }
            object.add(entry.getKey(), element);
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    protected final T self() {
        return (T) this;
    }

    public T enums(Object... values) {
        enums.addAll(Arrays.asList(values));
        return self();
    }

    public T field(String key, Object value) {
        additional.put(key, value);
        return self();
    }

    public ArrayElement asArray() {
        return new ArrayElement(this);
    }
}
