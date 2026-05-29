package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.parchmentmc.feather.io.gson.MDCGsonAdapterFactory;
import org.parchmentmc.feather.io.gson.NamedAdapter;
import org.parchmentmc.feather.io.gson.OffsetDateTimeAdapter;
import org.parchmentmc.feather.io.gson.SimpleVersionAdapter;
import org.parchmentmc.feather.io.gson.metadata.MetadataAdapterFactory;
import org.parchmentmc.feather.mapping.MappingDataContainer;
import org.parchmentmc.feather.mapping.VersionedMappingDataContainer;
import org.parchmentmc.feather.named.Named;
import org.parchmentmc.feather.util.SimpleVersion;
import zzzank.probejs.lang.parchment.data.IndexedMappingData;
import zzzank.probejs.lang.parchment.data.IndexedMappingData.IndexedClass;
import zzzank.probejs.lang.parchment.data.IndexedMappingData.IndexedMethod;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ZZZank
 */
public class IndexedMappingTest {

    @Test
    public void test() throws Exception {
        // 1. Load parchment-example.json from test resources
        InputStream is = getClass().getResourceAsStream("/parchment-1165.json");
        if (is == null) {
            System.err.print("parchment-example.json not found, exiting");
        }
        assertNotNull(is, "parchment-example.json not found on classpath");
        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        is.close();

        // 2. Parse to MappingDataContainer (same Gson setup as InjectParchment.fromJson)
        Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(new MDCGsonAdapterFactory())
            .registerTypeAdapter(SimpleVersion.class, new SimpleVersionAdapter())
            .registerTypeAdapterFactory(new MetadataAdapterFactory())
            .registerTypeAdapter(Named.class, new NamedAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();
        MappingDataContainer container = gson.fromJson(json, VersionedMappingDataContainer.class);
        assertNotNull(container, "Failed to parse MappingDataContainer from JSON");

        // 3. Convert to IndexedMappingData
        IndexedMappingData indexed = new IndexedMappingData(container);

        additionalTest(indexed);

        var path = Path.of("./indexed-parchment.json");
        try (var writer = Files.newBufferedWriter(path)) {
            gson.toJson(indexed, writer);
        }

        try (var reader = Files.newBufferedReader(path)) {
            var restored = new GsonBuilder().disableHtmlEscaping().create().fromJson(reader, IndexedMappingData.class);
            restored.restoreAfterDeserialization();
            assertEquals(indexed.indexer, restored.indexer);
        }
    }

    private static void additionalTest(IndexedMappingData indexed) {
        // 4. Verify top-level structure
        assertNotNull(indexed.classes, "IndexedMappingData.classes should not be null");
        assertFalse(indexed.classes.isEmpty(), "Expected at least one class in mapping data");
        assertNotNull(indexed.indexedDiff, "indexedNames should not be null");
        assertFalse(indexed.indexedDiff.isEmpty(), "indexedNames should be non-empty");

        // 5. Verify a specific class — Channel
        IndexedClass channelClass = findClass(indexed, "com/mojang/blaze3d/audio/Channel");
        assertNotNull(channelClass, "Expected class 'com/mojang/blaze3d/audio/Channel'");
        assertNotNull(channelClass.methods);
        assertFalse(channelClass.methods.isEmpty(), "Channel should have methods");

        // 6. Verify a method on Channel — attachBufferStream with parameter name via indexedDesc
        IndexedMethod attachMethod = findMethod(channelClass, "attachBufferStream");
        assertNotNull(attachMethod, "Expected method 'attachBufferStream'");
        assertEquals("attachBufferStream", attachMethod.name);
        // descriptor (LAudioStream;)V → indexedDesc: [0]=AudioStream, [1]=void
        // Parchment param {index:1, name:"stream"} → slot=0 → indexedDesc[0]
        assertEquals("net/minecraft/client/sounds/AudioStream",
            indexed.indexer.getValue(attachMethod.indexedDesc.get(0).indexedType));
        assertEquals("stream", attachMethod.indexedDesc.get(0).name,
            "attachBufferStream param name should be on indexedDesc[0] after slot mapping");

        // 7. Verify a constructor parameter name — Channel.<init>(I)V
        IndexedMethod initMethod = findMethod(channelClass, "<init>");
        assertNotNull(initMethod, "Expected <init> constructor");
        // descriptor (I)V → indexedDesc: [0]=int, [1]=void
        // Parchment param {index:1, name:"source"} → slot=0 → indexedDesc[0]
        assertEquals("source", initMethod.indexedDesc.get(0).name);

        // 8. Verify parameter javadoc + long/double gap fix on GlDebug.printDebugLog
        IndexedClass glDebugClass = findClass(indexed, "com/mojang/blaze3d/platform/GlDebug");
        assertNotNull(glDebugClass, "Expected class 'GlDebug'");
        IndexedMethod printMethod = findMethod(glDebugClass, "printDebugLog");
        assertNotNull(printMethod, "Expected method 'printDebugLog'");
        // descriptor (IIIIIJJ)V → argument slots: [I,I,I,I,I,J(long*2),J(long*2)]
        // Parchment indices: [0,1,2,3,4,5,7] — gap at 6 because J takes 2 slots
        // indexedDesc: [0:I, 1:I, 2:I, 3:I, 4:I, 5:J, 6:J, 7:V(return)]
        // With fix: index 5 → slotToArg[5]=5 (first J), index 7 → slotToArg[7]=6 (second J)
        assertEquals("source", printMethod.indexedDesc.get(0).name);
        assertNotNull(printMethod.indexedDesc.get(0).javaDoc,
            "printDebugLog's first parameter should have javadoc");
        assertTrue(printMethod.indexedDesc.get(0).javaDoc.contains("GLenum source"));
        // verify the long/double gap: index 7 → indexedDesc[6] (second J), not [7] (return)
        assertEquals("userParam", printMethod.indexedDesc.get(6).name,
            "Parchment param index 7 should map to indexedDesc[6] (second long arg)");

        // 9. Verify a method descriptor matches indexedDesc entry count
        IndexedMethod calculateBufferSize = findMethod(channelClass, "calculateBufferSize");
        assertNotNull(calculateBufferSize, "Expected method 'calculateBufferSize'");
        // descriptor: (Ljavax/sound/sampled/AudioFormat;I)I → 2 params + 1 return = 3 entries
        assertEquals(3, calculateBufferSize.indexedDesc.size());
        // Parchment param {index:0, name:"format"} → indexedDesc[0].name = "format"
        assertEquals("format", calculateBufferSize.indexedDesc.get(0).name);
        // Parchment param {index:1, name:"sampleAmount"} → indexedDesc[1].name = "sampleAmount"
        assertEquals("sampleAmount", calculateBufferSize.indexedDesc.get(1).name);

        // 10. Verify StringIndexer — both Channel and Library share type strings
        String typeAudioFormat = indexed.indexer.getValue(
            calculateBufferSize.indexedDesc.get(0).indexedType
        );
        assertEquals("javax/sound/sampled/AudioFormat", typeAudioFormat);

        // 11. Verify null javadoc on classes that have none
        assertNull(channelClass.javaDoc, "Channel class has no javadoc in example data");

        // 12. Verify class-level javadoc is null even when package-level javadoc exists
        IndexedClass listenerClass = findClass(indexed, "com/mojang/blaze3d/audio/Listener");
        assertNotNull(listenerClass);
        assertNull(listenerClass.javaDoc, "Listener class has no class-level javadoc in example");

        // 13. Verify IndexedField list is null for empty field list
        assertNull(channelClass.fields);
    }

    @Nullable
    private static IndexedClass findClass(IndexedMappingData data, String name) {
        for (var clazz : data.classes) {
            String className = data.indexer.getValue(clazz.indexedName);
            if (name.equals(className)) {
                return clazz;
            }
        }
        return null;
    }

    @Nullable
    private static IndexedMethod findMethod(IndexedClass clazz, String name) {
        for (var method : clazz.methods) {
            if (name.equals(method.name)) {
                return method;
            }
        }
        return null;
    }
}
