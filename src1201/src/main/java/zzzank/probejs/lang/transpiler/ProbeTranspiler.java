package zzzank.probejs.lang.transpiler;

import dev.latvian.mods.rhino.util.HideFromJS;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.plugin.ProbeJSPlugins;

/**
 * @author ZZZank
 */
public class ProbeTranspiler extends Transpiler {
    public ProbeTranspiler(TypeConverter typeConverter) {
        super(typeConverter);
    }

    @Override
    protected ClassTranspiler createClassTranspiler() {
        return new ClassTranspiler(
            typeConverter,
            ProbeJSPlugins.buildClassTransformer(this)
        );
    }

    @Override
    public boolean isRejected(Clazz clazz) {
        return clazz.hasAnnotation(HideFromJS.class) || super.isRejected(clazz);
    }
}
