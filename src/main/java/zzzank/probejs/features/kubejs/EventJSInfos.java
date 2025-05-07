package zzzank.probejs.features.kubejs;

import com.google.gson.JsonArray;
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
    public static final Codec<List<EventJSInfo>> CODEC = EventJSInfo.CODEC.listOf();

    public static List<EventJSInfo> sortedInfos() {
        val sorted = new ArrayList<>(KNOWN.values());
        sorted.sort(null);
        return sorted;
    }

    public static Set<Class<?>> provideClasses() {
        return KNOWN.values().stream().map(EventJSInfo::clazzRaw).collect(Collectors.toSet());
    }

    public static void loadFrom(Path path) {
        if (!path.toFile().exists()) {
            return;
        }
        try (val reader = Files.newBufferedReader(path)) {
            val obj = ProbeJS.GSON.fromJson(reader, JsonArray.class);
            if (obj == null) {
                return;
            }
            val decoded = CODEC.parse(JsonOps.INSTANCE, obj)
                .resultOrPartial(ProbeJS.LOGGER::error)
                .orElse(Collections.emptyList());
            for (val info : decoded) {
                KNOWN.put(info.id(), info);
            }
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error when reading EventJS infos", e);
        }
    }

    public static void writeTo(Path path) {
        try (val writer = Files.newBufferedWriter(path)) {
            CODEC.encodeStart(JsonOps.INSTANCE, sortedInfos())
                .resultOrPartial(error -> ProbeJS.LOGGER.error("Error when serializing EventJS infos: {}", error))
                .ifPresent(element -> ProbeJS.GSON_WRITER.toJson(element, writer));
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error when writing EventJS infos", e);
        }
    }
}
