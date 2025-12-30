package zzzank.probejs.utils;

import com.google.gson.*;
import lombok.val;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        val result = new JsonArray(json.size());
        for (val element : json) {
            result.add(element);
        }
        return result;
    }

    public static JsonElement mergeJsonRecursively(JsonElement base, JsonElement toMerge) {
        if (base instanceof JsonObject firstObject && toMerge instanceof JsonObject secondObject) {
            val result = deepCopy(firstObject);
            for (val entry : secondObject.entrySet()) {
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

        if (base instanceof JsonArray firstArray && toMerge instanceof JsonArray secondArray) {
            val elements = new ArrayList<JsonElement>();
            for (val element : firstArray) {
                elements.add(deepCopy(element));
            }
            for (val element : secondArray) {
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

        return toMerge;
    }

    public enum PathConverter implements JsonDeserializer<Path>, JsonSerializer<Path> {
        INSTANCE;

        @Override
        public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Paths.get(json.getAsString());
        }

        @Override
        public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public static JsonElement errorAsPayload(Throwable throwable) {
        JsonObject object = new JsonObject();

        object.addProperty("message", throwable.getMessage());
        JsonArray jsonArray = new JsonArray();
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            jsonArray.add(stackTraceElement.toString());
        }
        object.add("stackTrace", jsonArray);

        return object;
    }
}
