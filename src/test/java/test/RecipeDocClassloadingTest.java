package test;

import org.junit.jupiter.api.Test;
import zzzank.probejs.docs.recipes.doc.BuiltinRecipeDocs;

/**
 * @author ZZZank
 */
public class RecipeDocClassloadingTest {

    @Test
    public void test() {
        for (var docSupplier : BuiltinRecipeDocs.ALL) {
            docSupplier.get();
        }
    }
}
