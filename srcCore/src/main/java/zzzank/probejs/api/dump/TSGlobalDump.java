package zzzank.probejs.api.dump;

import lombok.val;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.api.output.PerFileWriter;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZZZank
 */
public class TSGlobalDump extends TSDumpBase implements TSDump.FolderDump {
    public final Map<String, TypeScriptFile> globals = new HashMap<>();

    public TSGlobalDump(Path writeTo) {
        this(new PerFileWriter().setWithIndex(false).setWriteAsModule(false), writeTo);
    }

    public TSGlobalDump(TSFileWriter writer, Path writeTo) {
        super(writer, writeTo);
    }

    @Override
    protected void dumpImpl() throws IOException {
        for (val file : globals.values()) {
            writer.accept(file);
        }

        try (val writer = Files.newBufferedWriter(writeTo.resolve("index.d.ts"))) {
            for (val identifier : globals.keySet()) {
                writer.write(String.format("export * from %s\n", ProbeJS.GSON.toJson("./" + identifier)));
            }
        }
        writer.write(writeTo);
    }
}
