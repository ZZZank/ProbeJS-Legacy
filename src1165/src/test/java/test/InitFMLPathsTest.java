package test;

import net.minecraftforge.fml.loading.FMLPaths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class InitFMLPathsTest {
    private static volatile boolean initialized = false;

    @BeforeAll
    public static void init() {
        if (!initialized) {
            synchronized (InitFMLPathsTest.class) {
                if (!initialized) {
                    initialized = true;
                    FMLPaths.loadAbsolutePaths(Path.of("run"));
                }
            }
        }
    }

    @Test
    public void testAccess() {
        for (var fmlPaths : FMLPaths.values()) {
            Assertions.assertNotNull(fmlPaths.get());
        }
    }
}
