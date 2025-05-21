package zzzank.probejs.features.kesseractjs;

import dev.latvian.mods.kubejs.typings.desc.*;
import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSJoinedType;
import zzzank.probejs.lang.typescript.code.type.ts.TSClassType;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;

import java.util.Arrays;

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

    public static BaseType convertType(TypeDescJS typeDesc) {
        if (typeDesc instanceof ArrayDescJS arrayDesc) {
            return convertType(arrayDesc.type()).asArray();
        } else if (typeDesc instanceof FixedArrayDescJS fixedArrayDesc) {
            return Types.join(",", "[", "]", Arrays.stream(fixedArrayDesc.types())
                .map(TypeDescAdapter::convertType)
                .toList());
        } else if (typeDesc instanceof GenericDescJS genericDesc) {
            if (genericDesc.type() instanceof PrimitiveDescJS primitiveDesc && primitiveDesc.type().equals("Map")) {
                if (genericDesc.types().length != 2) {
                    return Types.ANY;
                }
                val valueType = convertType(genericDesc.types()[1]);
                return Types.custom(
                    (decl, formatType) -> "{[k: string]: %s}".formatted(valueType.line(decl, formatType)),
                    valueType::getImportInfos
                );
            }

            return new TSParamType(
                convertType(genericDesc.type()),
                Arrays.stream(genericDesc.types()).map(TypeDescAdapter::convertType).toList()
            );
        } else if (typeDesc instanceof ObjectDescJS objectDesc) {
            val builder = Types.object();
            for (val type : objectDesc.types()) {
                builder.member(type.key(), type.optional(), convertType(type.value()));
            }
            return builder.build();
        } else if (typeDesc instanceof OrDescJS orDesc) {
            return new JSJoinedType.Union(
                Arrays.stream(orDesc.types()).map(TypeDescAdapter::convertType).toList());
        } else if (typeDesc instanceof PrimitiveDescJS primitiveDesc) {
            String content = primitiveDesc.type();
            if (content.startsWith(PROBEJS_PREFIX)) {
                content = content.substring(PROBEJS_PREFIX.length());
                val parts = content.split("\\.");
                parts[parts.length - 1] = "$" + parts[parts.length - 1];
                return new TSClassType(new ClassPath(Arrays.stream(parts).toArray(String[]::new)));
            } else {
                return Types.primitive(content);
            }
        }

        throw new RuntimeException("Unknown TypeDescJS");
    }
}