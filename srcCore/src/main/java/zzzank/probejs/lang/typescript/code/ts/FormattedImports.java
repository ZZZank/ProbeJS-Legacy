package zzzank.probejs.lang.typescript.code.ts;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class FormattedImports extends Code {
    private final ClassPath selfPath;

    public FormattedImports(ClassPath selfPath) {
        this.selfPath = selfPath;
    }

    @Override
    public ImportInfos getImportInfos() {
        return ImportInfos.of();
    }

    @Override
    public List<String> format(Declaration declaration) {
        val result = new ArrayList<String>();

        for (val value : declaration.references.values()) {
            if (value.info.path.equals(selfPath)) {
                continue;
            }
            result.add(value.getImportStatement());
        }
        if (result.isEmpty()) {
            // Mark the file as a module, do not remove unless there are other import/exports
            result.add("export {}");
        }

        return result;
    }
}
