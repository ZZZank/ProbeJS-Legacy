package test;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongComparator;
import lombok.val;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.world.ForgeChunkManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import zzzank.probejs.lang.java.ClassRegistry;

import java.util.Comparator;
import java.util.Map;

/**
 * @author ZZZank
 */
@Disabled
public class VariableTypeResolvingTest {

    @ParameterizedTest
    @ValueSource(classes = {
        LongComparator.class,
        Map.Entry.class,
        Comparator.class,
        ImmutableList.class,
        ForgeConfigSpec.Builder.class,
        ForgeChunkManager.TicketTracker.class
    })
    public void test(Class<?> type) {
        val classReg = new ClassRegistry();

        val clazz = classReg.addClass(type);
        // stack overflow will happen before
        Assertions.assertNotNull(clazz);
    }
}
