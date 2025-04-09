package zzzank.probejs.utils.config.serde;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @author ZZZank
 */
public class PatternSerde implements ConfigSerde<JsonElement, Pattern> {
    @Override
    public @NotNull JsonElement toJson(@NotNull Pattern value) {
        return new JsonPrimitive(value.pattern());
    }

    @Override
    public @NotNull Pattern fromJson(@NotNull JsonElement json) {
        return Pattern.compile(json.getAsString());
    }
}
