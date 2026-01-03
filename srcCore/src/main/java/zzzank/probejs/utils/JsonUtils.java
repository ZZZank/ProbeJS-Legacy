package zzzank.probejs.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.val;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class JsonUtils {

    public static JsonArray asStringArray(Collection<String> array) {
        JsonArray jsonArray = new JsonArray();
        for (String s : array) {
            jsonArray.add(s);
        }
        return jsonArray;
    }

    public static JsonElement parseObject(Object obj) {
        if (obj == null) {
            return JsonNull.INSTANCE;
        } else if (obj instanceof Number number) {
            return new JsonPrimitive(number);
        } else if (obj instanceof String string) {
            return new JsonPrimitive(string);
        } else if (obj instanceof Boolean bool) {
            return new JsonPrimitive(bool);
        } else if (obj instanceof Character c) {
            return new JsonPrimitive(c);
        } else if (obj instanceof List<?> list) {
            return parseObject(list);
        } else if (obj instanceof Map<?, ?> map) {
            return parseObject(map);
        } else if (obj.getClass().isArray()) {
            val length = Array.getLength(obj);
            val jsonArray = new JsonArray();
            for (int i = 0; i < length; i++) {
                jsonArray.add(parseObject(Array.get(obj, i)));
            }
            return jsonArray;
        }
        return JsonNull.INSTANCE;
    }

    public static JsonArray parseObject(List<?> list) {
        val jsonArray = new JsonArray();
        for (val o : list) {
            jsonArray.add(parseObject(o));
        }
        return jsonArray;
    }

    public static JsonObject parseObject(Map<?, ?> map) {
        val object = new JsonObject();
        for (val entry : map.entrySet()) {
            val key = entry.getKey();
            val value = entry.getValue();
            object.add(String.valueOf(key), parseObject(value));
        }
        return object;
    }

    public static Object deserializeObject(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return deserializeObject(json.getAsJsonPrimitive());
        } else if (json.isJsonArray()) {
            return deserializeObject(json.getAsJsonArray());
        } else if (json.isJsonObject()) {
            return deserializeObject(json.getAsJsonObject());
        }

        return null;
    }

    public static Object deserializeObject(JsonPrimitive json) {
        if (json.isBoolean()) {
            return json.getAsBoolean();
        } else if (json.isString()) {
            return json.getAsString();
        } else if (json.isNumber()) {
            return json.getAsNumber();
        }
        return null;
    }

    public static List<Object> deserializeObject(JsonArray json) {
        val deserialized = new ArrayList<>(json.size());
        for (val element : json) {
            deserialized.add(deserializeObject(element));
        }
        return deserialized;
    }

    public static Map<String, Object> deserializeObject(JsonObject json) {
        val deserialized = new HashMap<String, Object>(json.size());
        for (val entry : json.entrySet()) {
            deserialized.put(entry.getKey(), deserializeObject(entry.getValue()));
        }
        return deserialized;
    }

    public static <T extends JsonElement> T deepCopy(T json) {
        if (json.isJsonObject()) {
            return (T) deepCopy(json.getAsJsonObject());
        } else if (json.isJsonArray()) {
            return (T) deepCopy(json.getAsJsonArray());
        }
        return json;
    }

    public static JsonObject deepCopy(JsonObject json) {
        val result = new JsonObject();
        for (val entry : json.entrySet()) {
            result.add(entry.getKey(), deepCopy(entry.getValue()));
        }
        return result;
    }

    public static JsonArray deepCopy(JsonArray json) {
        val result = new JsonArray();
        for (val element : json) {
            result.add(element);
        }
        return result;
    }

    public static JsonObject mergeJsonRecursively(JsonObject base, JsonObject toMerge) {
        val result = deepCopy(base);
        for (val entry : toMerge.entrySet()) {
            val key = entry.getKey();
            val value = entry.getValue();
            if (result.has(key)) {
                result.add(key, mergeJsonRecursively(result.get(key), value));
            } else {
                result.add(key, value);
            }
        }
        return result;
    }

    public static JsonArray mergeJsonRecursively(JsonArray base, JsonArray toMerge) {
        val elements = new ArrayList<JsonElement>();
        for (val element : base) {
            elements.add(deepCopy(element));
        }
        for (val element : toMerge) {
            int index = elements.indexOf(element);
            if (index == -1) {
                elements.add(element);
            } else {
                elements.set(index, mergeJsonRecursively(elements.get(index), element));
            }
        }
        val result = new JsonArray();
        for (val element : elements) {
            result.add(element);
        }
        return result;
    }

    public static JsonElement mergeJsonRecursively(JsonElement base, JsonElement toMerge) {
        if (base.isJsonObject() && toMerge.isJsonObject()) {
            return mergeJsonRecursively(base.getAsJsonObject(), base.getAsJsonObject());
        }

        if (base.isJsonArray() && toMerge.isJsonArray()) {
            return mergeJsonRecursively(base.getAsJsonArray(), toMerge.getAsJsonArray());
        }

        return toMerge;
    }

    public static class ClassAsNameJsonAdapter extends TypeAdapter<Class<?>> {

        @Override
        public void write(JsonWriter out, Class<?> value) throws IOException {
            out.jsonValue(value.getName());
        }

        @Override
        public Class<?> read(JsonReader in) throws IOException {
            try {
                return Class.forName(in.nextString(), false, ClassAsNameJsonAdapter.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
