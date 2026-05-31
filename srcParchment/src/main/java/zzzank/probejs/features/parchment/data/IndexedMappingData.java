package zzzank.probejs.features.parchment.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;

import java.util.List;

/// To reduce size of result json:
/// - use [StringIndexer] to convert class names into integer
/// - replace list of Javadoc lines with a single string
/// - parameter and field is now represented by a single string instead of [JsonObject]
/// - more?
///
/// @author ZZZank
public class IndexedMappingData {
    public String timestamp = null;
    public List<IndexedClass> classes;
    public List<String> indexedDiff;

    public transient final StringIndexer indexer = new StringIndexer();

    public void restoreAfterDeserialization() {
        var baseDiff = Diffable.of("", '/');
        for (var diffStr : indexedDiff) {
            var diff = baseDiff.restore(diffStr);
            indexer.addOrGetIndex(diff.toString());
            baseDiff = diff;
        }
    }

    public abstract static class JavaDocHolder {
        public String doc;
    }

    public static class IndexedClass extends JavaDocHolder {
        /// the raw value should be class internal name
        /// @see StringIndexer#getValue(int)
        public Number name;
        public List<IndexedMethod> methods;
        public List<IndexedNamedType> fields;
    }

    public static class IndexedMethod extends JavaDocHolder {
        /// remapped name
        public String name;
        /// N parameter type + 1 return type
        public List<IndexedNamedType> desc;
    }

    /// field, or param, or return type
    @JsonAdapter(IndexedNamedType.GsonAdapter.class)
    public static class IndexedNamedType extends JavaDocHolder {
        /// for field, the name is remapped name, for finding the correct field
        /// for parameter, the name is for replacing existed name. Index is used for finding param
        public String name;
        /// the raw value should be class internal name
        /// @see StringIndexer#getValue(int)
        public Number type;

        public static class GsonAdapter
            implements JsonSerializer<IndexedNamedType>, JsonDeserializer<IndexedNamedType> {

            @Override
            public IndexedNamedType deserialize(
                JsonElement json,
                java.lang.reflect.Type typeOfT,
                JsonDeserializationContext context
            ) throws JsonParseException {
                var parts = json.getAsString().split(":", 3);
                var result = new IndexedNamedType();
                result.type = Integer.parseInt(parts[0]);
                result.name = parts.length > 1 ? parts[1] : null;
                result.doc = parts.length > 2 ? parts[2] : null;
                return result;
            }

            @Override
            public JsonElement serialize(
                IndexedNamedType src,
                java.lang.reflect.Type typeOfSrc,
                JsonSerializationContext context
            ) {
                var builder = new StringBuilder().append(src.type);
                if (src.name != null) {
                    builder.append(":").append(src.name);
                }
                if (src.doc != null) {
                    builder.append(":").append(src.doc);
                }
                return new JsonPrimitive(builder.toString());
            }
        }
    }
}
