package zzzank.probejs.lang.typescript.code;

import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.List;

public abstract class Code {
    public abstract ImportInfos getImportInfos();

    public abstract List<String> format(Declaration declaration);
}
