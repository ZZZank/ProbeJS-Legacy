package zzzank.probejs.lang.typescript;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.refer.ImportInfo;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TypeScriptFile {
    public final Declaration declaration;
    public final List<Code> codes;
    public final ClassPath path;

    public TypeScriptFile(ClassPath self) {
        this.declaration = new Declaration();
        this.codes = new ArrayList<>();

        if (self != null) {
            declaration.addImport(ImportInfo.ofDefault(self));
        }
        this.path = self;
    }

    public void excludeSymbol(String name) {
        declaration.exclude(name);
    }

    public void addCode(Code code) {
        codes.add(code);
        for (val info : code.getImportInfos()) {
            declaration.addImport(info);
        }
    }

    public void addCodes(Code... codes) {
        addCodes(Arrays.asList(codes));
    }

    public void addCodes(Collection<? extends Code> codes) {
        for (val code : codes) {
            addCode(code);
        }
    }

    public List<String> format() {
        List<String> formatted = new ArrayList<>();

        for (val code : codes) {
            formatted.addAll(code.format(declaration));
        }

        return formatted;
    }

    public void write(Path writeTo) throws IOException {
        try (val writer = Files.newBufferedWriter(writeTo)) {
            this.write(writer);
        }
    }

    public void write(Writer writer) throws IOException {
        boolean written = false;

        for (val value : declaration.references.values()) {
            if (value.info.path.equals(path)) {
                continue;
            }
            writer.write(value.getImportStatement() + "\n");
            written = true;
        }
        if (!written) {
            writer.write("export {} // Mark the file as a module, do not remove unless there are other import/exports!");
        }

        writer.write("\n");
        for (val line : format()) {
            writer.write(line);
            writer.write('\n');
        }
    }

    @NotNull
    public <T extends Code> Optional<T> findCode(Class<T> type) {
        return Optional.ofNullable(findCodeNullable(type));
    }

    @Nullable
    public <T extends Code> T findCodeNullable(Class<T> type) {
        for (val code : codes) {
            if (type.isInstance(code)) {
                return (T) code;
            }
        }
        return null;
    }
}
