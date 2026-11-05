package test;

import org.junit.jupiter.api.Test;
import zzzank.probejs.features.minecraft.EnumValueWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author ZZZank
 */
class EnumValueWindowTest {

    private static final int[] ROW_BUDGETS = {3, 5};

    @Test
    void rendersEverythingWhenValuesFit() {
        for (int total = 0; total <= 5; total++) {
            var window = EnumValueWindow.of(total, 0, 5);
            assertEquals(0, window.start());
            assertEquals(total, window.shown());
            assertEquals(0, window.hidden());
            assertFalse(window.ellipsisAtTop());
            assertEquals(total, window.lineCount());
        }
    }

    @Test
    void javadocExample() {
        // VALUE_1, VALUE_2, > VALUE_3, VALUE_4, ... (3 more values)
        var window = EnumValueWindow.of(7, 2, 5);
        assertEquals(0, window.start());
        assertEquals(4, window.shown());
        assertEquals(3, window.hidden());
        assertFalse(window.ellipsisAtTop());
        assertEquals(5, window.lineCount());
    }

    @Test
    void ellipsisMovesToTopNearTheEnd() {
        var window = EnumValueWindow.of(10, 9, 5);
        assertEquals(6, window.start());
        assertEquals(4, window.shown());
        assertEquals(6, window.hidden());
        assertTrue(window.ellipsisAtTop());
        assertEquals(5, window.lineCount());
    }

    @Test
    void clampsOutOfRangeSelection() {
        var tooLow = EnumValueWindow.of(10, -5, 5);
        assertEquals(0, tooLow.start());
        assertEquals(4, tooLow.shown());

        var tooHigh = EnumValueWindow.of(10, 99, 5);
        assertEquals(6, tooHigh.start());
        assertTrue(tooHigh.ellipsisAtTop());
    }

    @Test
    void emptyValueListIsSupported() {
        var window = EnumValueWindow.of(0, 0, 5);
        assertEquals(0, window.shown());
        assertEquals(0, window.hidden());
        assertEquals(0, window.lineCount());
    }

    @Test
    void invariantsHoldForAllInputs() {
        for (int total = 0; total <= 30; total++) {
            for (int selected = 0; selected < Math.max(total, 1); selected++) {
                for (int budget : ROW_BUDGETS) {
                    var window = EnumValueWindow.of(total, selected, budget);
                    var context = String.format("total=%s, selected=%s, budget=%s", total, selected, budget);

                    assertTrue(window.start() >= 0, context);
                    assertTrue(window.shown() >= 0, context);
                    assertTrue(window.start() + window.shown() <= total, context);
                    assertEquals(total - window.shown(), window.hidden(), context);
                    assertEquals(window.shown() + (window.hidden() > 0 ? 1 : 0), window.lineCount(), context);
                    assertTrue(window.lineCount() <= budget, context);

                    if (total <= budget) {
                        assertEquals(0, window.start(), context);
                        assertEquals(total, window.shown(), context);
                        continue;
                    }

                    assertEquals(budget - 1, window.shown(), context);
                    assertTrue(selected >= window.start(), context);
                    assertTrue(selected < window.start() + window.shown(), context);
                    assertEquals(
                        window.start() + window.shown() == total,
                        window.ellipsisAtTop(),
                        context
                    );
                }
            }
        }
    }
}
