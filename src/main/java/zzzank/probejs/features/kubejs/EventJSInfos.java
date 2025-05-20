package zzzank.probejs.features.kubejs;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.val;
import zzzank.probejs.ProbeJS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public final class EventJSInfos {

    public static final Map<String, EventJSInfo> KNOWN = new HashMap<>();
    public static final Codec<Map<String, EventJSInfo>> CODEC = Codec.unboundedMap(Codec.STRING, EventJSInfo.CODEC);

    public synchronized static List<Map.Entry<String, EventJSInfo>> copySortedInfos() {
        return KNOWN.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .toList();
    }

    public static Set<Class<?>> provideClasses() {
        return KNOWN.values().stream().map(EventJSInfo::clazzRaw).collect(Collectors.toSet());
    }

    public static void loadFrom(Path path) {
        if (!path.toFile().exists()) {
            return;
        }
        try (val reader = Files.newBufferedReader(path)) {
            val obj = ProbeJS.GSON.fromJson(reader, JsonObject.class);
            if (obj == null) {
                return;
            }
            val decoded = CODEC.parse(JsonOps.INSTANCE, obj)
                .resultOrPartial(ProbeJS.LOGGER::error)
                .orElse(Map.of());
            KNOWN.putAll(Maps.filterValues(decoded, Objects::nonNull));
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error when reading EventJS infos", e);
        }
    }

    public static void writeTo(Path path) {
        try (val writer = Files.newBufferedWriter(path)) {
            CODEC.encodeStart(JsonOps.INSTANCE, new TreeMap<>(KNOWN))
                .resultOrPartial(error -> ProbeJS.LOGGER.error("Error when serializing EventJS infos: {}", error))
                .ifPresent(element -> ProbeJS.GSON_WRITER.toJson(element, writer));
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error when writing EventJS infos", e);
        }
    }
}
