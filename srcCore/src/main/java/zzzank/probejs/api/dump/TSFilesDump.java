package zzzank.probejs.api.dump;

import zzzank.probejs.api.output.AutoSplitPackagedWriter;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * @author ZZZank
 */
public class TSFilesDump extends TSDumpBase implements TSDump.FolderDump {
    public Collection<TypeScriptFile> files = Collections.emptyList();

    public TSFilesDump(Path writeTo) {
        this(AutoSplitPackagedWriter.defaultSetup(), writeTo);
    }

    public TSFilesDump(TSFileWriter writer, Path writeTo) {
        super(writer, writeTo);
    }

    @Override
    protected void dumpImpl() throws IOException {
        for (var file : files) {
            writer.accept(file);
        }
        writer.write(writeTo);
    }
}
