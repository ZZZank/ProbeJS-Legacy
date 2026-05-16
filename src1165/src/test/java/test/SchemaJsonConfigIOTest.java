package test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.impl.VirtualFileSystem;
import zzzank.probejs.utils.config.io.SchemaJsonConfigIO;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.serde.gson.GsonSerdeFactory;
import zzzank.probejs.utils.config.struct.ConfigRootImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ZZZank
 */
class SchemaJsonConfigIOTest {

    final Gson gson = new Gson();
    VirtualFileSystem vfs;

    // region save

    @Test
    void saveBasicTypes() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();
        root.define("debug").bindDefault(false).build();
        root.define("name").bindDefault("hello").build();
        root.define("ratio").bindDefault(0.75f).build();

        io.save(root, configPath);

        // ---- config file ----
        var configObj = parseJson(vfs.getContent(configPath)).getAsJsonObject();
        assertEquals(8080, configObj.get("port").getAsInt());
        assertFalse(configObj.get("debug").getAsBoolean());
        assertEquals("hello", configObj.get("name").getAsString());
        assertEquals(0.75, configObj.get("ratio").getAsDouble(), 1e-9);
        assertTrue(configObj.has("$schema"), "config should have $schema key");

        // ---- schema file ----
        var schemaObj = parseJson(vfs.getContent(schemaPath)).getAsJsonObject();
        assertEquals(
            "https://json-schema.org/draft/2020-12/schema",
            schemaObj.get("$schema").getAsString()
        );
        assertEquals("object", schemaObj.get("type").getAsString());

        var props = schemaObj.getAsJsonObject("properties");
        assertSchemaType(props, "port", "integer", 8080);
        assertSchemaType(props, "debug", "boolean", false);
        assertSchemaType(props, "name", "string", "hello");
        assertSchemaType(props, "ratio", "number", 0.75);
    }

    @Test
    void saveWithComments() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port")
            .bindDefault(8080)
            .comment("Server port", "Range: 1024-65535")
            .build();

        io.save(root, configPath);

        var schemaObj = parseJson(vfs.getContent(schemaPath)).getAsJsonObject();
        var portSchema = schemaObj.getAsJsonObject("properties").getAsJsonObject("port");
        assertEquals("Server port\nRange: 1024-65535", portSchema.get("description").getAsString());
    }

    @Test
    void saveWithRangedBinding() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port")
            .bindRanged(8080, 1024, 65535)
            .build();

        io.save(root, configPath);

        var schemaObj = parseJson(vfs.getContent(schemaPath)).getAsJsonObject();
        var portSchema = schemaObj.getAsJsonObject("properties").getAsJsonObject("port");
        assertEquals("integer", portSchema.get("type").getAsString());
        assertEquals(1024, portSchema.get("minimum").getAsInt());
        assertEquals(65535, portSchema.get("maximum").getAsInt());
    }

    @Test
    void saveWithExample() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("name")
            .bindDefault("world")
            .setProperty(ConfigProperty.EXAMPLE, "hello")
            .build();

        io.save(root, configPath);

        var schemaObj = parseJson(vfs.getContent(schemaPath)).getAsJsonObject();
        var nameSchema = schemaObj.getAsJsonObject("properties").getAsJsonObject("name");
        assertEquals("hello", nameSchema.getAsJsonArray("examples").get(0).getAsString());
    }

    @Test
    void saveWithEnum() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("mode")
            .bindDefault("fast")
            .setProperty(ConfigProperty.ENUMS, List.of("fast", "slow", "auto"))
            .build();

        io.save(root, configPath);

        var schemaObj = parseJson(vfs.getContent(schemaPath)).getAsJsonObject();
        var modeSchema = schemaObj.getAsJsonObject("properties").getAsJsonObject("mode");
        var enumValues = modeSchema.getAsJsonArray("enum");
        assertEquals(3, enumValues.size());
        assertEquals("fast", enumValues.get(0).getAsString());
        assertEquals("slow", enumValues.get(1).getAsString());
        assertEquals("auto", enumValues.get(2).getAsString());
    }

    // endregion
    // region read

    @Test
    void readRestoresValues() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();
        root.define("debug").bindDefault(false).build();

        // write config file to VFS
        vfs.write(configPath, """
            {"$schema":"config.schema.json","port":9090,"debug":true}
            """);

        io.read(root, configPath);
        assertEquals(9090, root.getEntry("port").get());
        assertTrue((Boolean) root.getEntry("debug").get());
    }

    @Test
    void readMissingEntryKeepsDefault() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();

        // config has no "port" key
        vfs.write(configPath, "{\"$schema\":\"config.schema.json\"}");

        io.read(root, configPath);
        assertEquals(8080, root.getEntry("port").get());
    }

    @Test
    void readIgnoresExtraKeys() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();
        root.define("debug").bindDefault(false).build();

        vfs.write(configPath, """
            {"$schema":"config.schema.json","port":9090,"debug":true,"unknown_key":123}
            """);

        io.read(root, configPath);
        assertEquals(9090, root.getEntry("port").get());
        assertTrue((Boolean) root.getEntry("debug").get());
    }

    // endregion
    // region schema format

    @Test
    void schemaHasCorrectRootStructure() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();

        io.save(root, configPath);

        var schemaObj = parseJson(vfs.getContent(schemaPath)).getAsJsonObject();
        assertEquals("https://json-schema.org/draft/2020-12/schema", schemaObj.get("$schema").getAsString());
        assertEquals("object", schemaObj.get("type").getAsString());
        assertTrue(schemaObj.has("properties"), "schema root must have 'properties'");
    }

    @Test
    void schemaIncludesDefaultValues() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();

        io.save(root, configPath);

        var schemaObj = parseJson(vfs.getContent(schemaPath)).getAsJsonObject();
        var portSchema = schemaObj.getAsJsonObject("properties").getAsJsonObject("port");
        assertEquals(8080, portSchema.get("default").getAsInt());
    }

    // endregion
    // region $schema pointer in config

    @Test
    void configIncludesSchemaPointer() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("./config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();

        io.save(root, configPath);

        var configObj = parseJson(vfs.getContent(configPath)).getAsJsonObject();
        assertEquals("config.schema.json", configObj.get("$schema").getAsString());
    }

    @Test
    void noSchemaPointerWhenSchemaPathIsNull() throws Exception {
        var configPath = vfs.getPath("/config.json");

        var io = new SchemaJsonConfigIO(gson, null);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();

        io.save(root, configPath);

        var configObj = parseJson(vfs.getContent(configPath)).getAsJsonObject();
        assertFalse(configObj.has("$schema"), "no $schema when schema path is null");
    }

    @Test
    void noSchemaFileWhenSchemaPathIsNull() throws Exception {
        var configPath = vfs.getPath("/config.json");

        var io = new SchemaJsonConfigIO(gson, null);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        root.define("port").bindDefault(8080).build();

        io.save(root, configPath);

        // should not throw, and no schema file should have been written
        assertNotNull(vfs.getContent(configPath));
        assertEquals(1, vfs.viewFilePaths().size(), "only config file should exist");
    }

    // endregion
    // region category

    @Test
    void saveWithSubCategory() throws Exception {
        var configPath = vfs.getPath("/config.json");
        var schemaPath = vfs.getPath("/config.schema.json");

        var io = new SchemaJsonConfigIO(gson, schemaPath);
        io.registerSerdeFactory(new GsonSerdeFactory(gson));

        var root = new ConfigRootImpl(io, configPath);
        var network = root.subCategory("network");
        network.define("port").bindDefault(8080).build();
        network.define("timeout").bindDefault(5000L).build();

        io.save(root, configPath);

        // config: flat structure with nested object
        var configObj = parseJson(vfs.getContent(configPath)).getAsJsonObject();
        assertTrue(configObj.has("network"));
        var networkConfig = configObj.getAsJsonObject("network");
        assertEquals(8080, networkConfig.get("port").getAsInt());

        // schema: should have nested properties
        var schemaObj = parseJson(vfs.getContent(schemaPath)).getAsJsonObject();
        var networkSchema = schemaObj.getAsJsonObject("properties").getAsJsonObject("network");
        assertEquals("object", networkSchema.get("type").getAsString());
        assertTrue(networkSchema.has("properties"));
        assertEquals("integer", networkSchema.getAsJsonObject("properties").getAsJsonObject("port").get("type").getAsString());
    }

    // endregion
    // region helpers

    private void assertSchemaType(
        JsonObject properties, String name, String expectedType, Object expectedDefault
    ) {
        var schema = properties.getAsJsonObject(name);
        assertNotNull(schema, "property '" + name + "' not found in schema");
        assertEquals(expectedType, schema.get("type").getAsString());
        if (expectedDefault instanceof Number expectedNum) {
            assertEquals(expectedNum.doubleValue(), gson.fromJson(schema.get("default"), Double.class));
        } else {
            assertEquals(expectedDefault, toJavaValue(schema.get("default")));
        }
    }

    private static Object toJavaValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            var p = element.getAsJsonPrimitive();
            if (p.isBoolean()) {
                return p.getAsBoolean();
            }
            if (p.isNumber()) {
                return p.getAsNumber();
            }
            if (p.isString()) {
                return p.getAsString();
            }
        }
        return element;
    }

    // endregion

    @BeforeEach
    public void preTest() {
        vfs = new VirtualFileSystem();
    }

    @AfterEach
    public void afterTest() {
        vfs.close();
    }

    private JsonElement parseJson(String content) {
        return gson.fromJson(content, JsonElement.class);
    }
}
