package zzzank.probejs.utils.config.struct;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.NameUtils;
import zzzank.probejs.utils.config.binding.ConfigBinding;
import zzzank.probejs.utils.config.binding.DefaultBinding;
import zzzank.probejs.utils.config.binding.RangedBinding;
import zzzank.probejs.utils.config.binding.ReadOnlyBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;
import zzzank.probejs.utils.config.prop.ConfigProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class ConfigEntryBuilder<T> {

    public final ConfigCategory parent;
    public final String name;
    public ConfigBinding<T> binding;
    public ConfigProperties properties = new ConfigProperties();

    ConfigEntryBuilder(ConfigCategory parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public <T_> ConfigEntryBuilder<T_> bind(ConfigBinding<T_> binding) {
        val type = binding.getDefaultType();
        Asser.t(type.isInstance(binding.getDefault()), "config default value must match expected type");
        val casted = Cast.<ConfigEntryBuilder<T_>>to(this);
        casted.binding = Asser.tNotNull(binding, "config binding");
        return casted;
    }

    public <T_> ConfigEntryBuilder<T_> bindDefault(@NotNull T_ defaultValue) {
        return bind(new DefaultBinding<>(defaultValue, extractType(defaultValue), name));
    }

    public <T_> ConfigEntryBuilder<T_> bindReadOnly(@NotNull T_ defaultValue) {
        return bind(new ReadOnlyBinding<>(defaultValue, extractType(defaultValue), name));
    }

    public <T_ extends Comparable<T_>> ConfigEntryBuilder<T_> bindRanged(
        @NotNull T_ defaultValue,
        @NotNull T_ min,
        @NotNull T_ max
    ) {
        return bind(new RangedBinding<>(defaultValue, extractType(defaultValue), name, min, max));
    }

    private static <T> Class<T> extractType(T value) {
        if (value instanceof Enum<?> e) {
            return (Class<T>) e.getDeclaringClass();
        }
        return (Class<T>) value.getClass();
    }

    public <T_> ConfigEntryBuilder<T> setProperty(ConfigProperty<T_> property, @NotNull T_ value) {
        properties.put(property, value);
        return this;
    }

    public ConfigEntryBuilder<T> setComments(List<String> comments) {
        return setProperty(ConfigProperty.COMMENTS, comments);
    }

    public ConfigEntryBuilder<T> comment(String... comments) {
        this.properties.merge(
            ConfigProperty.COMMENTS,
            Arrays.stream(comments)
                .map(NameUtils.MATCH_LINE_BREAK::split)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList()),
            (a, b) -> {
                a.addAll(b);
                return a;
            }
        );
        return this;
    }
}
