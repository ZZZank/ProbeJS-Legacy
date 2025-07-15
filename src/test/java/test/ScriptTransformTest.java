package test;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zzzank.probejs.features.kubejs.ScriptTransformer;

/**
 * @author ZZZank
 */
public class ScriptTransformTest {

    static {
        InitFMLPathsTest.init();
    }

    @Test
    public void test() {
        var linenoNormal = eval(
            Context::evaluateString,
            """
                const e = extract;
                e.record();
                // trailing comments"""
        );
        var linenoTransformed = eval(
            ScriptTransformer::transformedScriptEval,
            """
                export const e = extract;
                e.record();
                // trailing comments"""
        );
        Assertions.assertEquals(linenoNormal, linenoTransformed);
    }

    private static int eval(Evaluator evaluator, String source) {
        return ContextFactory.getGlobal().call(cx -> {
            val scope = cx.initSafeStandardObjects();

            var lineNumberExtract = new LineNumberExtract();
            ScriptableObject.putProperty(scope, "extract", lineNumberExtract);

            evaluator.eval(cx, scope, source, "test.js", 1, null);

            return lineNumberExtract.get();
        });
    }

    private static final class LineNumberExtract {

        private final int[] lineNumberHolder = new int[]{-1};

        public void record() {
            Context.getSourcePositionFromStack(lineNumberHolder);
        }

        public int get() {
            return lineNumberHolder[0];
        }
    }

    private interface Evaluator {
        Object eval(Context cx, Scriptable scope, String source, String sourceName, int lineno, Object securityDomain);
    }
}
