package zzzank.probejs.lang.transpiler.transformation;

import lombok.val;
import zzzank.probejs.lang.transpiler.Transpiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author ZZZank
 */
public class ClassTransformerRegistration {
    public final Transpiler transpiler;
    private final List<ClassTransformer> registered = new ArrayList<>();

    public ClassTransformerRegistration(Transpiler transpiler) {
        this.transpiler = transpiler;
    }

    public void register(ClassTransformer... transformers) {
        for (val transformer : transformers) {
            registered.add(Objects.requireNonNull(transformer));
        }
    }

    public List<ClassTransformer> getRegistered() {
        return Collections.unmodifiableList(registered);
    }
}
