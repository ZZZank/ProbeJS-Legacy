package zzzank.probejs.api.output;

import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.utils.UnsafeFunction;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author ZZZank
 */
public abstract class AbstractWriter implements TSFileWriter {
    protected static final UnsafeFunction<Path, Writer, IOException> DEFAULT_WRITER_PROVIDER
        = Files::newBufferedWriter;

    protected int written = 0;
    protected boolean writeAsModule = true;
    protected boolean withIndex = true;
    protected String suffix = D_TS_SUFFIX;
    protected UnsafeFunction<Path, Writer, IOException> writerProvider = DEFAULT_WRITER_PROVIDER;

    @Override
    public TSFileWriter setWriterProvider(UnsafeFunction<Path, Writer, IOException> writerProvider) {
        this.writerProvider = Objects.requireNonNull(writerProvider);
        return this;
    }

    @Override
    public TSFileWriter setFileSuffix(String suffix) {
        this.suffix = Objects.requireNonNull(suffix);
        return this;
    }

    @Override
    public TSFileWriter setWithIndex(boolean withIndex) {
        this.withIndex = withIndex;
        return this;
    }

    @Override
    public TSFileWriter setWriteAsModule(boolean writeAsModule) {
        this.writeAsModule = writeAsModule;
        return this;
    }

    protected void writeFile(TypeScriptFile file, Writer writer) throws IOException {
        if (this.writeAsModule) {
            writer.write("declare module ");
            writer.write(ProbeJS.GSON.toJson(file.path.getTSPath()));
            writer.write(" {\n");
            file.write(writer);
            writer.write("}\n");
        } else {
            file.write(writer);
        }
        written++;
    }

    @Override
    public final void write(Path base) throws IOException {
        written = 0;
        preWriting();
        try {
            writeClasses(base);
            if (withIndex) {
                writeIndex(base);
            }
        } finally {
            postWriting();
        }
    }

    protected void preWriting() {
    }

    protected abstract void postWriting();

    protected abstract void writeClasses(Path base) throws IOException;

    protected abstract void writeIndex(Path base) throws IOException;

    @Override
    public int countWrittenFiles() {
        return written;
    }
}
