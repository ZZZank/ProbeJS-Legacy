package zzzank.probejs.docs.bindings;

import dev.latvian.kubejs.BuiltinKubeJSPlugin;
import dev.latvian.mods.rhino.Undefined;
import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;

/**
 * resolve values in global, but keep in mind that only first level members are resolved
 *
 * @author ZZZank
 */
class ResolveGlobal {

    public static final String NAME = "global";
    public static final JSPrimitiveType RESOLVED = Types.primitive("ProbeJS$$ResolvedGlobal");

    public static void addGlobals(ScriptDump scriptDump) {
        val resolved = resolveType(
            ProbeConfig.globalResolvingDepth.get(),
            BuiltinKubeJSPlugin.GLOBAL,
            scriptDump.transpiler.typeConverter
        );
        scriptDump.addGlobal(
            "resolved_global",
            new TypeDecl(RESOLVED.content, resolved.contextShield(BaseType.FormatType.RETURN))
        );
    }

    public static BaseType resolveType(int depth, Object value, TypeConverter converter) {
        if (value == null) {
            return Types.NULL;
        } else if (value instanceof Undefined) {
            return Types.UNDEFINED;
        } else if (depth < 1) {
            return converter.convertType(value.getClass());
        }
        return ValueTypes.convert(value, converter, depth);
    }
}
