package zzzank.probejs.docs.assignments;

import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSTupleType;

/**
 * @author ZZZank
 */
public class AssignmentHelper {
    public static JSTupleType xyzOf(BaseType baseType) {
        return Types.tuple()
            .member("x", baseType)
            .member("y", baseType)
            .member("z", baseType)
            .build();
    }

    public static JSTupleType minMaxOf(BaseType type) {
        return Types.tuple()
            .member("min", type)
            .member("max", type)
            .build();
    }
}
