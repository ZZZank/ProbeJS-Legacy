package zzzank.probejs.utils;

import com.google.gson.*;
import lombok.val;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class JsonUtils {
    private static final Pattern MATCH_TRAILING = Pattern.compile(",(?!\\s*?[{\\[\"'\\w])");

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
            val jsonArray = new JsonArray();
            for (val o : list) {
                jsonArray.add(parseObject(o));
            }
            return jsonArray;
        } else if (obj instanceof Map<?, ?> map) {
            val object = new JsonObject();
            for (val entry : map.entrySet()) {
                val key = entry.getKey();
                val value = entry.getValue();
                object.add(String.valueOf(key), parseObject(value));
            }
            return object;
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

    public static Object deserializeObject(JsonElement jsonElement) {
        if (jsonElement instanceof JsonPrimitive primitive) {
            return primitive.isBoolean() ? primitive.getAsBoolean()
                : primitive.isString() ? primitive.getAsString()
                    : primitive.isNumber() ? primitive.getAsNumber()
                        : null;
        } else if (jsonElement instanceof JsonArray array) {
            val deserialized = new ArrayList<>();
            for (val element : array) {
                deserialized.add(deserializeObject(element));
            }
            return deserialized;
        } else if (jsonElement instanceof JsonObject object) {
            val deserialized = new HashMap<>();
            for (val entry : object.entrySet()) {
                val s = entry.getKey();
                deserialized.put(s, deserializeObject(object.get(s)));
            }
            return deserialized;
        }

        return null;
    }

    public static <T extends JsonElement> T deepCopy(T elem) {
        if (elem.isJsonObject()) {
            val result = new JsonObject();
            for (val entry : elem.getAsJsonObject().entrySet()) {
                result.add(entry.getKey(), deepCopy(entry.getValue()));
            }
            return (T) result;
        } else if (elem.isJsonArray()) {
            val result = new JsonArray();
            for (val element : elem.getAsJsonArray()) {
                result.add(deepCopy(element));
            }
            return (T) result;
        }
        return elem;
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
