package zzzank.probejs.docs;

import com.google.common.collect.ImmutableSet;
import dev.latvian.mods.rhino.util.ClassWrapper;
import lombok.val;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.transpiler.redirect.ClassRedirect;
import zzzank.probejs.lang.transpiler.redirect.RhizoGenericRedirect;
import zzzank.probejs.lang.transpiler.redirect.SimpleClassRedirect;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.ClassWrapperPJS;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ZZZank
 */
public class TypeRedirecting implements ProbeJSPlugin {

    public static final Set<Class<?>> CLASS_CONVERTIBLES = new HashSet<>();

    static {
        CLASS_CONVERTIBLES.add(ClassWrapperPJS.class);
        if (RhizoState.CLASS_WRAPPER) {
            CLASS_CONVERTIBLES.add(ClassWrapper.class);
        }
    }

    @Override
    public void addPredefinedTypes(TypeConverter converter) {
        if (RhizoState.GENERIC_ANNOTATION) {
            converter.addTypeRedirect(new RhizoGenericRedirect());
        }
        //class wrapper
        if (!CLASS_CONVERTIBLES.isEmpty()) {
            val targets = ImmutableSet.copyOf(CLASS_CONVERTIBLES);
            converter.addTypeRedirect(new SimpleClassRedirect(targets, (c) -> GlobalClasses.J_CLASS));
            converter.addTypeRedirect(new ClassRedirect(targets));
        }
    }
}
