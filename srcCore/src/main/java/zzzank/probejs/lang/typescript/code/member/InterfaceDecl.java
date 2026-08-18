package zzzank.probejs.lang.typescript.code.member;

import org.jetbrains.annotations.Nullable;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.refer.ImportType;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.DocUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterfaceDecl extends ClassDecl {

    public boolean withStatic = true;
    private ClassDecl staticClass = null;

    public InterfaceDecl(
        String name,
        @Nullable BaseType superClass,
        List<BaseType> interfaces,
        List<TSVariableType> variableTypes
    ) {
        super(name, superClass, interfaces, variableTypes);
    }

    public ClassDecl getStaticClass() {
        if (staticClass == null) {
            var classDecl = new ClassDecl(
                name,
                null,
                Collections.singletonList(Types.primitive(ImportType.STATIC.fmt(name))
                    .withPossibleParams(variableTypes)),
                variableTypes
            );

            classDecl.methods.addAll(methods);
            classDecl.fields.addAll(fields);
            classDecl.bodyCode.addAll(bodyCode);
            classDecl.isAbstract = true;
            classDecl.nativeClazz = this.nativeClazz;
            staticClass = classDecl;
        }
        return staticClass;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        var lines = new ArrayList<String>();

        // Format head - export interface Interf$$Interface<T> extends ... {
        String head = "export interface "
                      + (withStatic ? ImportType.STATIC.fmt(name) : name)
                      + TSVariableType.formatGenericParam(variableTypes, declaration);
        if (!interfaces.isEmpty()) {
            head += " extends " + Types.join(", ", interfaces).line(declaration);
        }
        head += " {";

        lines.add(head);

        // Format body - fields, constructors, methods

        // static method will be in static class
        var interfaceMethods = CollectUtils.iterate(methods.stream().filter(m -> !m.isStatic));
        DocUtils.addIndentedCodes(lines, interfaceMethods, declaration);
        // field (all static) will be in static class

        DocUtils.addIndentedCodes(lines, bodyCode, declaration);

        // Tail
        lines.add("}");

        return lines;
    }
}
