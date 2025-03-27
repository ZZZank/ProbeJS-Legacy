package zzzank.probejs.docs;

import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.util.ClassWrapper;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.transpiler.redirect.ClassRedirect;
import zzzank.probejs.lang.transpiler.redirect.InheritableClassRedirect;
import zzzank.probejs.lang.transpiler.redirect.RhizoGenericRedirect;
import zzzank.probejs.lang.transpiler.redirect.SimpleClassRedirect;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.ClassWrapperPJS;

import java.util.*;

/**
 * @author ZZZank
 */
public class TypeRedirecting implements ProbeJSPlugin {

    public static final Set<Class<?>> CLASS_CONVERTIBLES = new HashSet<>();
    public static final Map<Class<?>, BaseType> JS_OBJ = new IdentityHashMap<>();

    static {
        CLASS_CONVERTIBLES.add(ClassWrapperPJS.class);
        if (RhizoState.CLASS_WRAPPER) {
            CLASS_CONVERTIBLES.add(ClassWrapper.class);
        }
        JS_OBJ.put(ScriptableObject.class, Types.EMPTY_OBJECT);
        JS_OBJ.put(NativeArray.class, Types.ANY.asArray());
        JS_OBJ.put(
            BaseFunction.class,
            Types.lambda().param("args", Types.ANY, false, true).returnType(Types.ANY).build()
        );
    }

    @Override
    public void addPredefinedTypes(TypeConverter converter) {
        if (RhizoState.GENERIC_ANNOTATION) {
            converter.addTypeRedirect(new RhizoGenericRedirect());
        }
        //class wrapper
        converter.addTypeRedirect(new SimpleClassRedirect(CLASS_CONVERTIBLES, (c) -> GlobalClasses.J_CLASS));
        converter.addTypeRedirect(new ClassRedirect(CLASS_CONVERTIBLES));
        //js objects
        converter.addTypeRedirect(new InheritableClassRedirect(JS_OBJ.keySet(), JS_OBJ::get));
    }
}
