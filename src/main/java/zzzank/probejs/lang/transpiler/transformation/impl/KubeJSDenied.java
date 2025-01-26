package zzzank.probejs.lang.transpiler.transformation.impl;

import dev.latvian.kubejs.script.ScriptManager;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;

/**
 * @author ZZZank
 */
public class KubeJSDenied implements ClassTransformer {
    private final ScriptManager manager;

    public KubeJSDenied(ScriptManager manager) {
        this.manager = manager;
    }

    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        if (!manager.isClassAllowed(clazz.getOriginal().getName())) {
            classDecl.addComment(
                "This class is not allowed By KubeJS!",
                "You should not load the class, or KubeJS will throw an error.",
                "Loading the class using require() will not throw an error, but the class will be undefined."
            );
        }
    }
}
