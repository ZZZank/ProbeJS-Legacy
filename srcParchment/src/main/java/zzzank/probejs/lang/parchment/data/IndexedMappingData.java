package zzzank.probejs.lang.parchment.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.parchmentmc.feather.mapping.MappingDataContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * To reduce size of result json:
 * - use {@link StringIndexer} to convert class names into integer
 * - replace list of Javadoc lines with a single string
 * - parameter is now represented by a single string instead of {@link JsonObject}
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

        indexedDiff = indexer.buildDiffs();
    }

    public void restoreAfterDeserialization() {
        this.indexer = new StringIndexer();

        var baseDiff = PrefixDiff.EMPTY;
        for (var diffStr : indexedDiff) {
            var diff = PrefixDiff.fromString(diffStr);
            indexer.addOrGetIndex(baseDiff.restore(diff));
            baseDiff = diff;
        }
    }

    public static class IndexedClass {
        /// original name: `com/mojang/blaze3d/audio/OggAudioStream`
        public int indexedName;
        public String javaDoc;
        public List<IndexedMethod> methods;
        public List<IndexedField> fields;

        public IndexedClass(MappingDataContainer.ClassData classData, StringIndexer indexer) {
            this.indexedName = indexer.addOrGetIndex(classData.getName());
            this.javaDoc = joinLines(classData.getJavadoc());

            if (!classData.getMethods().isEmpty()) {
                this.methods = new ArrayList<>(classData.getMethods().size());
                for (var method : classData.getMethods()) {
                    methods.add(new IndexedMethod(method, indexer));
                }
            }

            if (!classData.getFields().isEmpty()) {
                this.fields = new ArrayList<>(classData.getFields().size());
                for (var field : classData.getFields()) {
                    fields.add(new IndexedField(field, indexer));
                }
            }
        }
    }

    public static class IndexedMethod {
        /// remapped name
        public String name;
        public String javaDoc;
        /// N parameter type + 1 return type
        public List<IndexedParamOrReturn> indexedDesc;

        public IndexedMethod(MappingDataContainer.MethodData method, StringIndexer indexer) {
            this.name = method.getName();
            this.javaDoc = joinLines(method.getJavadoc());

            var methodType = Type.getMethodType(method.getDescriptor());

            var argumentTypes = methodType.getArgumentTypes();
            this.indexedDesc = new ArrayList<>(argumentTypes.length + 1);
            for (var argumentType : argumentTypes) {
                indexedDesc.add(new IndexedParamOrReturn(argumentType, indexer));
            }
            indexedDesc.add(new IndexedParamOrReturn(methodType.getReturnType(), indexer));

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
                    var indexedParam = this.indexedDesc.get(slotToArg[parameter.getIndex() - min]);
                    indexedParam.name = parameter.getName();
                    indexedParam.javaDoc = parameter.getJavadoc();
                }
            }
        }
    }

    @JsonAdapter(IndexedParamOrReturn.GsonAdapter.class)
    public static class IndexedParamOrReturn {
        public int indexedType;
        @Nullable
        public String name = null;
        @Nullable
        public String javaDoc = null;

        public IndexedParamOrReturn(Type type, StringIndexer indexer) {
            this.indexedType = indexer.addOrGetIndex(type.getInternalName());
        }

        private IndexedParamOrReturn(String[] parts) {
            this.indexedType = Integer.parseInt(parts[0]);
            this.name = parts.length > 1 ? parts[1] : null;
            this.javaDoc = parts.length > 2 ? parts[2] : null;
        }

        public static class GsonAdapter
            implements JsonSerializer<IndexedParamOrReturn>, JsonDeserializer<IndexedParamOrReturn> {

            @Override
            public IndexedParamOrReturn deserialize(
                JsonElement json,
                java.lang.reflect.Type typeOfT,
                JsonDeserializationContext context
            ) throws JsonParseException {
                return new IndexedParamOrReturn(json.getAsString().split(":", 3));
            }

            @Override
            public JsonElement serialize(
                IndexedParamOrReturn src,
                java.lang.reflect.Type typeOfSrc,
                JsonSerializationContext context
            ) {
                var builder = new StringBuilder().append(src.indexedType);
                if (src.name != null) {
                    builder.append(":").append(src.name);
                }
                if (src.javaDoc != null) {
                    builder.append(":").append(src.javaDoc);
                }
                return new JsonPrimitive(builder.toString());
            }
        }
    }

    public static class IndexedField {
        /// remapped name
        public String name;
        public String javaDoc;
        public int indexedType;

        public IndexedField(MappingDataContainer.FieldData field, StringIndexer indexer) {
            this.indexedType = indexer.addOrGetIndex(Type.getType(field.getDescriptor()).getInternalName());
            this.name = field.getName();
            this.javaDoc = joinLines(field.getJavadoc());
        }
    }

    private static String joinLines(List<String> lines) {
        return lines.isEmpty() ? null : String.join("\n", lines);
    }
}
