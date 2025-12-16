package zzzank.probejs.features.kubejs;

import dev.latvian.mods.kubejs.typings.desc.*;
import lombok.val;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSJoinedType;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;
import zzzank.probejs.utils.CollectUtils;

import java.util.List;

/**
 * @author ZZZank
 */
public class TypeDescAdapter {
    public static final String PROBEJS_PREFIX = "$$probejs$$";
    public static final DescriptionContext PROBEJS = new DescriptionContext() {
        @Override
        public String typeName(Class<?> type) {
            return PROBEJS_PREFIX + type.getName();
        }
    };

    public static List<BaseType> convertTypes(TypeDescJS... descJSs) {
        return switch (descJSs.length) {
            case 0 -> List.of();
            case 1 -> List.of(convertType(descJSs[0]));
            case 2 -> List.of(convertType(descJSs[0]), convertType(descJSs[1]));
            default -> CollectUtils.mapToList(descJSs, TypeDescAdapter::convertType);
        };
    }

    public static BaseType convertType(TypeDescJS typeDesc) {
        if (typeDesc instanceof ArrayDescJS(TypeDescJS component)) {
            return convertType(component).asArray();

        } else if (typeDesc instanceof FixedArrayDescJS(TypeDescJS[] types)) {
            return Types.join(",", "[", "]", convertTypes(types));

        } else if (typeDesc instanceof GenericDescJS(TypeDescJS base, TypeDescJS[] params)) {
            if (base instanceof PrimitiveDescJS(String value) && value.equals("Map")) {
                if (params.length != 2) {
                    return Types.ANY;
                }
                val valueType = convertType(params[1]);
                return Types.custom(
                    (decl, formatType) -> "{[k: string]: %s}".formatted(valueType.line(decl, formatType)),
                    valueType::getImportInfos
                );
            }
            return new TSParamType(convertType(base), convertTypes(params));

        } else if (typeDesc instanceof ObjectDescJS(java.util.List<ObjectDescJS.Entry> types)) {
            val builder = Types.object();
            for (val type : types) {
                builder.member(type.key(), type.optional(), convertType(type.value()));
            }
            return builder.build();

        } else if (typeDesc instanceof OrDescJS(TypeDescJS[] types)) {
            return new JSJoinedType.Union(convertTypes(types));

        } else if (typeDesc instanceof PrimitiveDescJS(String value)) {
            if (value.startsWith(PROBEJS_PREFIX)) {
                val className = RemapperBridge.unmapClass(value.substring(PROBEJS_PREFIX.length()));
                try {
                    return Types.type(Class.forName(className));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            return Types.primitive(value);
        }

        throw new RuntimeException("Unknown TypeDescJS: " + typeDesc);
    }
}
