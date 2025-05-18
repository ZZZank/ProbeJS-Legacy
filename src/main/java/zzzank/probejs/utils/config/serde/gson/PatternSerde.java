package zzzank.probejs.utils.config.serde.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.config.serde.ConfigSerde;

import java.util.regex.Pattern;

/**
 * @author ZZZank
 */
public class PatternSerde implements ConfigSerde<JsonElement, Pattern> {
    @Override
    public @NotNull JsonElement serialize(@NotNull Pattern value) {
        return new JsonPrimitive(value.pattern());
    }

    @Override
    public @NotNull Pattern deserialize(@NotNull JsonElement intermediate) {
        return Pattern.compile(intermediate.getAsString());
    }
}
