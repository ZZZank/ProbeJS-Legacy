package zzzank.probejs.lang.typescript.code.member;

import lombok.val;
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
            staticClass = createStaticClass();
        }
        return staticClass;
    }

    private ClassDecl createStaticClass() {
        val classDecl = new ClassDecl(
            this.name,
            null,
            Collections.singletonList(Types.primitive(ImportType.STATIC.fmt(name))
                .withPossibleParams(variableTypes)
                .contextShield(BaseType.FormatType.INTERFACE)),
            this.variableTypes
        );

        classDecl.isAbstract = true;
        classDecl.methods.addAll(this.methods);
        classDecl.fields.addAll(fields);
        return classDecl;
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        for (val method : methods) {
            method.isInterface = true;
        }

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
