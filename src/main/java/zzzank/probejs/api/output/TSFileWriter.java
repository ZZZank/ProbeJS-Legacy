package zzzank.probejs.api.output;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.utils.UnsafeFunction;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public interface TSFileWriter {

    TSFileWriter setWriterProvider(UnsafeFunction<Path, Writer, IOException> writerProvider);

    TSFileWriter setFileSuffix(String suffix);

    TSFileWriter setWithIndex(boolean withIndex);

    TSFileWriter setWriteAsModule(boolean writeAsModule);

    void accept(@NotNull TypeScriptFile file);

    void write(Path base) throws IOException;

    int countAcceptedFiles();

    int countWrittenFiles();

    String TS_SUFFIX = ".ts";

    String D_TS_SUFFIX = ".d.ts";

    String INDEX_FILE_NAME = "index";
}
