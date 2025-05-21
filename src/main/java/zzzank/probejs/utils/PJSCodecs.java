package zzzank.probejs.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.val;

import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public interface PJSCodecs {
    Codec<Class<?>> CLASS_CODEC = Codec.STRING.comapFlatMap(
        wrapUnsafeFn(name -> Class.forName(name, false, PJSCodecs.class.getClassLoader())), Class::getName
    );
    Codec<ScriptType> SCRIPT_TYPE_CODEC = createEnumStringCodec(ScriptType.class);

    static <T extends Enum<T>> Codec<T> createEnumStringCodec(Class<T> type) {
        return createEnumStringCodec(type, true);
    }

    static <T extends Enum<T>> Codec<T> createEnumStringCodec(final Class<T> type, final boolean ignoreCase) {
        val indexedValues = new HashMap<String, T>();
        for (val value : type.getEnumConstants()) {
            val name = ignoreCase
                ? value.name().toLowerCase(Locale.ROOT)
                : value.name();
            indexedValues.put(name, value);
        }
        return Codec.STRING.comapFlatMap(
            wrapUnsafeFn(name -> {
                if (name == null) {
                    throw new NullPointerException("Name is null");
                }
                val result = indexedValues.get(ignoreCase ? name.toLowerCase(Locale.ROOT) : name);
                if (result == null) {
                    throw new IllegalArgumentException(
                        "No enum constant " + type.getCanonicalName() + "." + name);
                }
                return result;
            }),
            Enum::name
        );
    }

    static <I, O> Function<I, DataResult<O>> wrapUnsafeFn(UnsafeFn<I, O> fn) {
        return fn;
    }

    interface UnsafeFn<I, O> extends Function<I, DataResult<O>> {
        O applyUnsafe(I i) throws Exception;

        @Override
        default DataResult<O> apply(I input) {
            try {
                return DataResult.success(applyUnsafe(input));
            } catch (Exception e) {
                return DataResult.error(e.toString());
            }
        }
    }
}
