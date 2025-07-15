package test;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
        var cx = Context.enter();
        val scope = cx.initSafeStandardObjects();

        var lineNumberExtract = new LineNumberExtract(cx);
        ScriptableObject.putProperty(scope, "extract", lineNumberExtract, cx);

        evaluator.eval(cx, scope, source, "test.js", 1, null);

        return lineNumberExtract.get();
    }

    private static final class LineNumberExtract {

        private final int[] lineNumberHolder = new int[]{-1};
        private final Context cx;

        public LineNumberExtract(Context cx) {
            this.cx = cx;
        }

        public void record() {
            Context.getSourcePositionFromStack(cx, lineNumberHolder);
        }

        public int get() {
            return lineNumberHolder[0];
        }
    }

    private interface Evaluator {
        Object eval(Context cx, Scriptable scope, String source, String sourceName, int lineno, Object securityDomain);
    }
}
