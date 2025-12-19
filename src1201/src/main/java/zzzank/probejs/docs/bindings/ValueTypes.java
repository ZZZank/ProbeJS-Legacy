package zzzank.probejs.docs.bindings;

import dev.latvian.mods.rhino.*;
import lombok.val;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author ZZZank
 */
public class ValueTypes {

    private static final Map<Class<?>, ValueTypeConverter> FORMATTERS = new LinkedHashMap<>();
    private static final Set<Class<?>> PRIMITIVES = new HashSet<>(Arrays.asList(
        String.class,
        Character.class, Character.TYPE,
        Long.class, Long.TYPE,
        Integer.class, Integer.TYPE,
        Short.class, Short.TYPE,
        Byte.class, Byte.TYPE,
        Double.class, Double.TYPE,
        Float.class, Float.TYPE,
        Boolean.class, Boolean.TYPE
    ));

    public static final BaseType BARE_BONE_MAP = Types.object().rawNameMember("[key: string]", Types.ANY).build();
    public static final BaseType BARE_BONE_LIST = Types.object().rawNameMember("[index: number]", Types.ANY).build();

    static {
        //shortcut
        FORMATTERS.put(NativeArray.class, ValueTypes::convertList);
        FORMATTERS.put(NativeObject.class, ValueTypes::convertScriptableObject);
        FORMATTERS.put(NativeFunction.class, ValueTypes::formatFunction);
        //js
        FORMATTERS.put(BaseFunction.class, ValueTypes::formatFunction);
        FORMATTERS.put(ArrowFunction.class, ValueTypes::formatFunction);
        FORMATTERS.put(Scriptable.class, ValueTypes::convertScriptableObject);
        //general
        FORMATTERS.put(Map.class, ValueTypes::convertMap);
        FORMATTERS.put(List.class, ValueTypes::convertList);
        //primitives
        for (val t : PRIMITIVES) {
            FORMATTERS.put(t, ValueTypes::convertPrimitive);
        }
    }

    @Nullable
    public static BaseType convert(Object obj, TypeConverter converter, int limit) {
        if (obj == null) {
            return null;
        }
        val type = obj.getClass();
        val direct = FORMATTERS.get(type);
        if (direct != null) {
            return direct.convertOrDefault(obj, converter, limit);
        }
        for (val entry : FORMATTERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue().convertOrDefault(obj, converter, limit);
            }
        }
        return null;
    }

    public static JSPrimitiveType convertPrimitive(Object o, TypeConverter converter, int limit) {
        if (o == null || !PRIMITIVES.contains(o.getClass()) || limitConsumed(limit)) {
            return null;
        }
        return Types.primitive(ProbeJS.GSON.toJson(o));
    }

    public static BaseType convertOrDefault(Object obj, TypeConverter converter, int limit) {
        if (obj == null) {
            return Types.NULL;
        }
        val converted = convert(obj, converter, limit);
        if (converted != null) {
            return converted;
        }
        return converter.convertType(obj.getClass());
    }

    public static BaseType convertMap(Object obj, TypeConverter converter, int limit) {
        if (!(obj instanceof Map<?, ?> map)) {
            return null;
        } else if (limitConsumed(limit)) {
            return BARE_BONE_MAP;
        }
        val builder = Types.object();
        for (val entry : map.entrySet()) {
            val key = String.valueOf(entry.getKey());
            val value = entry.getValue();
            builder.member(key, convertOrDefault(value, converter, consumeLimit(limit)));
        }
        return builder.build();
    }

    public static BaseType convertList(Object obj, TypeConverter converter, int limit) {
        if (!(obj instanceof List<?> list)) {
            return null;
        } else if (limitConsumed(limit)) {
            return Types.ANY.asArray();
        }

        val nextLimit = consumeLimit(limit);
        val converted = new BaseType[list.size()];
        for (int i = 0; i < list.size(); i++) {
            converted[i] = convertOrDefault(list.get(i), converter, nextLimit);
        }

        return Types.join(", ", "[", "]", converted);
    }

    public static BaseType convertScriptableObject(Object obj, TypeConverter converter, int limit) {
        if (!(obj instanceof ScriptableObject scriptable)) {
            return null;// if not Scriptable, why call this
        } else if (limitConsumed(limit)) {
            return Types.OBJECT;
        }
        return convertMap(obj, converter, limit);
    }

    public static BaseType formatFunction(Object obj, TypeConverter converter, int limit) {
        if (!(obj instanceof BaseFunction fn)) {
            return null;
        }
        val builder = Types.lambda().returnType(Types.ANY);
        val arity = fn.getArity();
        for (int i = 0; i < arity; i++) {
            builder.param("arg" + i, Types.ANY);
        }
        return builder.build();
    }

    private static int consumeLimit(int limit) {
        return limit < 0 ? -1 : limit - 1;
    }

    private static boolean limitConsumed(int limit) {
        return limit <= 0;
    }

    interface ValueTypeConverter {
        BaseType convert(Object obj, TypeConverter converter, int depth);

        default BaseType convertOrDefault(Object object, TypeConverter converter, int depth) {
            if (object == null) {
                return Types.NULL;
            }
            val converted = this.convert(object, converter, depth);
            return converted == null ? converter.convertType(object.getClass()) : converted;
        }

        default BaseType convert(Object obj, TypeConverter converter) {
            return convert(obj, converter, -1);
        }
    }
}
