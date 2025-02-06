package zzzank.probejs.features.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.script.ScriptType;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.JsonUtils;
import zzzank.probejs.utils.Mutable;
import zzzank.probejs.utils.ReflectUtils;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * @author ZZZank
 */
@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public final class EventJSInfo implements Comparable<EventJSInfo> {
    private final Class<? extends EventJS> clazzRaw;
    private final String id;
    private final boolean cancellable;
    private final EnumSet<ScriptType> scriptTypes;
    private final Mutable<String> sub;

    public EventJSInfo(ScriptType type, EventJS event, String id, @Nullable String sub) {
        this(event.getClass(), id, event.canCancel(), EnumSet.of(type), new Mutable<>(sub));
    }

    @Nullable
    public static EventJSInfo fromJson(String id, JsonObject json) {
        //class
        val clazz = ReflectUtils.classOrNull(json.get("class").getAsString(), ProbeJS.LOGGER);
        if (clazz == null || !EventJS.class.isAssignableFrom(clazz)) {
            return null;
        }
        //type
        val types = EnumSet.noneOf(ScriptType.class);
        for (val element : json.get("type").getAsJsonArray()) {
            types.add(ScriptType.valueOf(element.getAsString()));
        }
        //sub
        val sub = json.has("sub") ? json.get("sub").getAsString() : null;
        //cancellable
        val cancellable = json.has("cancellable") && json.get("cancellable").getAsBoolean();

        return new EventJSInfo(
            (Class<? extends EventJS>) clazz,
            id,
            cancellable,
            types,
            new Mutable<>(sub)
        );
    }

    public Map.Entry<String, JsonObject> toJson() {
        val m = CollectUtils.ofMap(
            "class", clazzRaw.getName(),
            "type", CollectUtils.mapToList(scriptTypes, ScriptType::name),
            "cancellable", this.cancellable
        );
        if (sub.notNull()) {
            m.put("sub", sub.get());
        }
        return new AbstractMap.SimpleImmutableEntry<>(id, (JsonObject) JsonUtils.parseObject(m));
    }

    @Override
    public int compareTo(@NotNull EventJSInfo o) {
        return this.id.compareTo(o.id);
    }
}
