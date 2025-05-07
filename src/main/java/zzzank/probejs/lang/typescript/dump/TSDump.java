package zzzank.probejs.lang.typescript.dump;

import zzzank.probejs.api.output.TSFileWriter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public interface TSDump {
    Path writeTo();

    void dump() throws IOException;

    TSFileWriter writer();

    boolean running();
}
