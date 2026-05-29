package zzzank.probejs.lang.parchment.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import org.objectweb.asm.Type;
import org.parchmentmc.feather.mapping.MappingDataContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * To reduce size of result json:
 * - use {@link StringIndexer} to convert class names into integer
 * - replace list of Javadoc lines with a single string
 * - parameter and field is now represented by a single string instead of {@link JsonObject}
 * - more?
 *
 * @author ZZZank
 */
public class IndexedMappingData {
    public List<IndexedClass> classes;
    public List<String> indexedDiff;

    public transient StringIndexer indexer;

    public IndexedMappingData(MappingDataContainer mappingData) {
        this.indexer = new StringIndexer();

        classes = new ArrayList<>(mappingData.getClasses().size());
        for (var classData : mappingData.getClasses()) {
            classes.add(new IndexedClass(classData, indexer));
        }

        indexer.freeze();

        indexedDiff = indexer.buildDiffs();
    }

    public void restoreAfterDeserialization() {
        this.indexer = new StringIndexer();

        var baseDiff = Diffable.of("", '/');
        for (var diffStr : indexedDiff) {
            var diff = baseDiff.restore(diffStr);
            indexer.addOrGetIndex(diff.toString());
            baseDiff = diff;
        }
    }

    public static class IndexedClass {
        /// @see Type#getInternalName()
        /// @see StringIndexer#getValue(int)
        public Number name;
        public String doc;
        public List<IndexedMethod> methods;
        public List<IndexedNamedType> fields;

        public IndexedClass(MappingDataContainer.ClassData classData, StringIndexer indexer) {
            this.name = indexer.addOrGetIndex(classData.getName());
            this.doc = joinLines(classData.getJavadoc());

            if (!classData.getMethods().isEmpty()) {
                this.methods = new ArrayList<>(classData.getMethods().size());
                for (var method : classData.getMethods()) {
                    methods.add(new IndexedMethod(method, indexer));
                }
            }

            if (!classData.getFields().isEmpty()) {
                this.fields = new ArrayList<>(classData.getFields().size());
                for (var field : classData.getFields()) {
                    var indexed = new IndexedNamedType(Type.getType(field.getDescriptor()), indexer);
                    indexed.name = field.getName();
                    indexed.doc = joinLines(field.getJavadoc());
                    fields.add(indexed);
                }
            }
        }
    }

    public static class IndexedMethod {
        /// remapped name
        public String name;
        public String doc;
        /// N parameter type + 1 return type
        public List<IndexedNamedType> desc;

        public IndexedMethod(MappingDataContainer.MethodData method, StringIndexer indexer) {
            this.name = method.getName();
            this.doc = joinLines(method.getJavadoc());

            var methodType = Type.getMethodType(method.getDescriptor());

            var argumentTypes = methodType.getArgumentTypes();
            this.desc = new ArrayList<>(argumentTypes.length + 1);
            for (var argumentType : argumentTypes) {
                desc.add(new IndexedNamedType(argumentType, indexer));
            }
            desc.add(new IndexedNamedType(methodType.getReturnType(), indexer));

            // Map ParameterData bytecode slot index -> indexedDesc argument index
            // Handles: min offset (0=static, 1=instance) + long/double 2-slot gap
            var min = method.getParameters()
                .stream()
                .mapToInt(MappingDataContainer.ParameterData::getIndex)
                .min()
                .orElse(0);
            if (min < 2) {
                // build slot → argument lookup (long/double take 2 local variable slots)
                int totalSlots = 0;
                for (var argType : argumentTypes) {
                    totalSlots += argType.getSize();
                }
                var slotToArg = new int[totalSlots];
                int slot = 0;
                for (int i = 0; i < argumentTypes.length; i++) {
                    slotToArg[slot] = i;
                    slot += argumentTypes[i].getSize();
                }
                for (var parameter : method.getParameters()) {
                    var indexedParam = this.desc.get(slotToArg[parameter.getIndex() - min]);
                    indexedParam.name = parameter.getName();
                    indexedParam.doc = parameter.getJavadoc();
                }
            }
        }
    }

    /// field, or param, or return type
    @JsonAdapter(IndexedNamedType.GsonAdapter.class)
    public static class IndexedNamedType {
        /// for field, the name is remapped name
        public String name;
        public String doc;
        /// @see Type#getInternalName()
        /// @see StringIndexer#getValue(int)
        public Number type;

        public IndexedNamedType(Type type, StringIndexer indexer) {
            this.type = indexer.addOrGetIndex(type.getInternalName());
        }

        private IndexedNamedType(String[] parts) {
            this.type = Integer.parseInt(parts[0]);
            this.name = parts.length > 1 ? parts[1] : null;
            this.doc = parts.length > 2 ? parts[2] : null;
        }

        public static class GsonAdapter
            implements JsonSerializer<IndexedNamedType>, JsonDeserializer<IndexedNamedType> {

            @Override
            public IndexedNamedType deserialize(
                JsonElement json,
                java.lang.reflect.Type typeOfT,
                JsonDeserializationContext context
            ) throws JsonParseException {
                return new IndexedNamedType(json.getAsString().split(":", 3));
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

    private static String joinLines(List<String> lines) {
        return lines.isEmpty() ? null : String.join("\n", lines);
    }
}
