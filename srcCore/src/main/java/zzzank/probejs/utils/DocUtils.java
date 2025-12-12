package zzzank.probejs.utils;

import lombok.val;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.lang.typescript.code.member.ParamDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DocUtils {
    public static void applyParam(
        TypeScriptFile file,
        Predicate<MethodDecl> methodFilter,
        int paramIndex,
        Consumer<ParamDecl> action
    ) {
        if (file == null) {
            return;
        }
        file.findCode(ClassDecl.class)
            .map(c -> c.methods)
            .orElse(List.of())
            .stream()
            .filter(methodFilter)
            .forEach(method -> action.accept(method.params.get(paramIndex)));
    }

    public static void replaceParamType(
        TypeScriptFile file,
        Predicate<MethodDecl> methodFilter,
        int paramIndex,
        BaseType toReplace
    ) {
        applyParam(file, methodFilter, paramIndex, param -> param.type = toReplace);
        for (val info : toReplace.getImportInfos(BaseType.FormatType.INPUT)) {
            file.declaration.addImport(info);
        }
    }
}
