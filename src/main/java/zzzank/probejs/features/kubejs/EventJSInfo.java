package zzzank.probejs.features.kubejs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.script.ScriptType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.PJSCodecs;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * @author ZZZank
 */
@Getter
@Accessors(fluent = true)
@ToString
@AllArgsConstructor
public final class EventJSInfo {
    private final Class<?> clazzRaw;
    private final boolean cancellable;
    private final EnumSet<ScriptType> scriptTypes;
    public String sub;

    public static final Codec<EventJSInfo> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            PJSCodecs.CLASS_CODEC.fieldOf("clazz").forGetter(EventJSInfo::clazzRaw),
            Codec.BOOL.fieldOf("cancellable").forGetter(EventJSInfo::cancellable),
            PJSCodecs.SCRIPT_TYPE_CODEC.listOf()
                .xmap(EnumSet::copyOf, ArrayList::new)
                .fieldOf("scriptTypes")
                .forGetter(EventJSInfo::scriptTypes),
            Codec.STRING.optionalFieldOf("sub", "").forGetter(EventJSInfo::sub)
        ).apply(builder, EventJSInfo::new)
    );

    public EventJSInfo(ScriptType type, EventJS event, String sub) {
        this(event.getClass(), event.canCancel(), EnumSet.of(type), sub);
    }
}
