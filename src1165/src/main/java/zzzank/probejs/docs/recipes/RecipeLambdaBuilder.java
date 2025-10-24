package zzzank.probejs.docs.recipes;

import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;

/**
 * @author ZZZank
 */
public class RecipeLambdaBuilder extends JSLambdaType.BuilderBase<RecipeLambdaBuilder> {

    public RecipeLambdaBuilder input(BaseType type) {
        return param("input", type);
    }

    public RecipeLambdaBuilder inputs(BaseType type) {
        return param("inputs", type);
    }

    public RecipeLambdaBuilder output(BaseType type) {
        return param("output", type);
    }

    public RecipeLambdaBuilder outputs(BaseType type) {
        return param("outputs", type);
    }
}
