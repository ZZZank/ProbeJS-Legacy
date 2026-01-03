package zzzank.probejs.features.kubejs;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.latvian.kubejs.script.ScriptType;
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
    @SuppressWarnings("unchecked")
    public static final TypeToken<Map<String, EventJSInfo>> TYPE_TOKEN = (TypeToken<Map<String, EventJSInfo>>)
        TypeToken.getParameterized(Map.class, String.class, EventJSInfo.class);

    static {
        for (var record : BuiltinEventRecord.RECORDS) {
            var types = record.type() != null
                ? EnumSet.of(ScriptType.STARTUP, record.type())
                : EnumSet.allOf(ScriptType.class);
            var info = new EventJSInfo(record.eventClass(), null, types, "");

            KNOWN.put(record.id(), info);
        }
    }

    public static Set<Class<?>> provideClasses() {
        return KNOWN.values().stream().map((EventJSInfo info) -> info.clazz).collect(Collectors.toSet());
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
            Map<String, EventJSInfo> decoded = ProbeJS.GSON.fromJson(reader, TYPE_TOKEN.getType());
            KNOWN.putAll(Maps.filterValues(decoded, Objects::nonNull));
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error when reading EventJS infos", e);
        }
    }

    public static void writeTo(Path path) {
        try (val writer = Files.newBufferedWriter(path)) {
            var toWrite = Maps.filterValues(new TreeMap<>(KNOWN), info -> info.cancellable != null);
            ProbeJS.GSON.toJson(toWrite, TYPE_TOKEN.getType(), writer);
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error when writing EventJS infos", e);
        }
    }
}
