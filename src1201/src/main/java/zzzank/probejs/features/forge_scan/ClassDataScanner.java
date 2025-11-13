package zzzank.probejs.features.forge_scan;

import net.minecraftforge.forgespi.language.ModFileScanData;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public interface ClassDataScanner {

    /**
     * stream of class data -> class name
     */
    Collection<String> scan(Stream<ModFileScanData.ClassData> dataStream);
}
