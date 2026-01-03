package zzzank.probejs.features.kubejs;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.script.ScriptType;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.utils.JsonUtils;

import java.util.EnumSet;

/**
 * @author ZZZank
 */
@ToString
public final class EventJSInfo {
    @JsonAdapter(JsonUtils.ClassAsNameJsonAdapter.class)
    @SerializedName("class")
    public final Class<?> clazz;
    @Nullable
    public Boolean cancellable;
    @SerializedName("type")
    public final EnumSet<ScriptType> scriptTypes;
    @NotNull
    public String sub;

    public EventJSInfo(ScriptType type, EventJS event, String sub) {
        this(event.getClass(), event.canCancel(), EnumSet.of(type), sub);
    }

    public EventJSInfo(
        Class<?> clazz,
        @Nullable Boolean cancellable,
        EnumSet<ScriptType> scriptTypes,
        @NotNull String sub
    ) {
        this.clazz = clazz;
        this.cancellable = cancellable;
        this.scriptTypes = scriptTypes;
        this.sub = sub;
    }
}
