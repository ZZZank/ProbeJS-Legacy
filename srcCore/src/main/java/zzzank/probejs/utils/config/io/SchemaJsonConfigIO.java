package zzzank.probejs.utils.config.io;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.JsonUtils;
import zzzank.probejs.utils.ReflectUtils;
import zzzank.probejs.utils.config.binding.RangedBinding;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.struct.ConfigCategory;
import zzzank.probejs.utils.config.struct.ConfigEntry;
import zzzank.probejs.utils.config.struct.ConfigRoot;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/// ```json
/// // config-file.json
/// {
///     "$schema": "./config-file-schema.json",
///     "entry1": 34,
///     "entry 2": 78
/// }
/// ```
///
/// The schema file follows JSON Schema (Draft 2020-12):
/// ```
/// {
///     "$schema": "https://json-schema.org/draft/2020-12/schema",
///     "type": "object",
///     "properties": {
///         "entry1": {
///             "type": "integer",
///             "default": 12,
///             "description": "comments here"
///         },
///         "entry 2": {
///             "type": "string",
///             "default": "world",
///             "description": "multi-\n-line comments"
///         }
///     }
/// }
/// ```
///
/// @author ZZZank
public class SchemaJsonConfigIO extends SerdeHolder<JsonElement> implements ConfigIO {
    public static final ConfigProperty<ConfigSerde<JsonElement, ?>> PROP_KEY =
        ConfigProperty.register("schema_json_io_serde", null);

    public static final String SCHEMA_KEY = "$schema";
    public static final String JSON_SCHEMA_DRAFT = "https://json-schema.org/draft/2020-12/schema";

    private final Gson gson;
    @Nullable
    private final Path schemaRelativeToConfigFolder;
    public final Map<Class<?>, JsonObject> schemaBases = new WeakHashMap<>();
    public final List<Function<Class<?>, JsonObject>> schemaBaseFactories = new ArrayList<>();

    public SchemaJsonConfigIO(Gson gson, @Nullable Path schemaRelativeToConfigFolder) {
        this.gson = Asser.tNotNull(gson, "gson");
        this.schemaRelativeToConfigFolder = schemaRelativeToConfigFolder;
        schemaBaseFactories.add(this::builtinSchemaBaseFactory);
    }

    @Override
    public ConfigProperty<ConfigSerde<JsonElement, ?>> getSerdeKey() {
        return PROP_KEY;
    }

    private JsonObject builtinSchemaBaseFactory(Class<?> type) {
        if (type.isPrimitive()) {
            type = ReflectUtils.box(type);
        }
        if (type == Integer.class || type == Long.class || type == Short.class || type == Byte.class) {
            return simpleSchema("integer");
        }
        if (type == Float.class || type == Double.class) {
            return simpleSchema("number");
        }
        if (type == Boolean.class) {
            return simpleSchema("boolean");
        }
        if (type == Character.class || type == String.class) {
            return simpleSchema("string");
        }
        if (type == Pattern.class) {
            var obj = simpleSchema("string");
            obj.addProperty("format", "regex");
            return obj;
        }
        if (type.isEnum()) {
            var obj = simpleSchema("string");
            var enumArray = new JsonArray();
            for (val constant : type.getEnumConstants()) {
                enumArray.add(constant.toString());
            }
            obj.add("enum", enumArray);
            return obj;
        }
        if (Collection.class.isAssignableFrom(type)) {
            return simpleSchema("array");
        }
        return null;
    }

    private static JsonObject simpleSchema(String value) {
        var obj = new JsonObject();
        obj.addProperty("type", value);
        return obj;
    }

    public JsonObject getSchemaBase(Class<?> type) {
        var schemaBase = schemaBases.get(type);
        if (schemaBase == null) {
            for (var factory : schemaBaseFactories) {
                schemaBase = factory.apply(type);
                if (schemaBase != null) {
                    schemaBases.put(type, schemaBase);
                    break;
                }
            }
        }
        return schemaBase;
    }

    @Override
    public void read(ConfigRoot config, Reader reader) throws IOException {
        val json = gson.fromJson(reader, JsonObject.class);
        if (json == null) {
            return;
        }
        readEntry(config, json);
    }

    private <T> void readEntry(ConfigEntry<T> entry, JsonElement json) {
        if (entry.isCategory()) {
            var category = entry.asCategory();
            var jsonCategory = json.getAsJsonObject();

            for (var subEntry : category.get().values()) {
                var subValue = jsonCategory.get(subEntry.name());
                if (subValue != null) {
                    readEntry(subEntry, subValue);
                }
            }
        } else {
            readEntryImpl(entry, json);
        }
    }

    protected <T> void readEntryImpl(ConfigEntry<T> entry, JsonElement json) {
        entry.set(getSerde(entry).deserialize(json));
    }

    @Override
    public void save(ConfigRoot config, Writer writer) throws IOException {
        var configJson = buildConfigJson(config);

        if (schemaRelativeToConfigFolder != null) {
            configJson.getAsJsonObject()
                .addProperty(SCHEMA_KEY, schemaRelativeToConfigFolder.toString().replace(File.separatorChar, '/'));
        }

        gson.toJson(configJson, writer);

        if (schemaRelativeToConfigFolder != null && !config.inMemoryOnly()) {
            val schemaPath = config.filePath().resolveSibling(schemaRelativeToConfigFolder);
            Files.createDirectories(schemaPath.getParent());

            try (val schemaWriter = Files.newBufferedWriter(schemaPath)) {
                gson.toJson(buildSchemaJson(config), schemaWriter);
            }
        }
    }

    private <T> JsonElement buildConfigJson(ConfigEntry<T> entry) {
        if (entry.isCategory()) {
            var category = entry.asCategory();
            var json = new JsonObject();

            for (var subEntry : category.get().values()) {
                json.add(subEntry.name(), buildConfigJson(subEntry));
            }

            return json;
        } else {
            return getSerde(entry).serialize(entry.get());
        }
    }

    private JsonObject buildSchemaJson(ConfigRoot config) {
        val schema = simpleSchema("object");
        schema.addProperty(SCHEMA_KEY, JSON_SCHEMA_DRAFT);

        val properties = new JsonObject();
        writeSchemaCategory(config, properties);
        schema.add("properties", properties);
        return schema;
    }

    private void writeSchemaCategory(ConfigCategory category, JsonObject propertiesTarget) {
        for (val entry : category.get().values()) {
            val name = entry.name();
            if (entry.isCategory()) {
                val subSchema = simpleSchema("object");

                val subProperties = new JsonObject();
                writeSchemaCategory(entry.asCategory(), subProperties);
                subSchema.add("properties", subProperties);

                propertiesTarget.add(name, subSchema);
            } else {
                propertiesTarget.add(name, generateSchemaEntry(entry));
            }
        }
    }

    private <T> JsonObject generateSchemaEntry(ConfigEntry<T> entry) {
        JsonObject schemaEntry;
        val serde = getSerde(entry);

        // type-specific schema base (must come first so enum from binding can override)
        val type = entry.binding().getDefaultType();
        val schemaBase = getSchemaBase(type);
        if (schemaBase != null) {
            schemaEntry = JsonUtils.deepCopy(schemaBase);
        } else {
            schemaEntry = new JsonObject();
        }

        // default value
        schemaEntry.add("default", serde.serialize(entry.getDefault()));

        // description
        val comments = entry.getProp(ConfigProperty.COMMENTS).orElse(Collections.emptyList());
        if (!comments.isEmpty()) {
            schemaEntry.addProperty("description", String.join("\n", comments));
        }

        // enums from property (overrides enum from schema base for runtime refinement)
        val enums = entry.getProp(ConfigProperty.ENUMS).orElse(null);
        if (enums != null && !enums.isEmpty()) {
            val enumArray = new JsonArray();
            for (val e : enums) {
                enumArray.add(e);
            }
            schemaEntry.add("enum", enumArray);
        }

        // examples
        val example = entry.getProp(ConfigProperty.EXAMPLE).orElse(null);
        if (example != null) {
            val examples = new JsonArray();
            examples.add(example);
            schemaEntry.add("examples", examples);
        }

        // min/max from RangedBinding
        if (entry.binding() instanceof RangedBinding<?> ranged) {
            @SuppressWarnings("unchecked")
            var rawSerde = (ConfigSerde<JsonElement, Object>) serde;
            schemaEntry.add("minimum", rawSerde.serialize(ranged.min()));
            schemaEntry.add("maximum", rawSerde.serialize(ranged.max()));
        }

        return schemaEntry;
    }
}
