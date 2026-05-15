package zzzank.probejs.lang.typescript.code.member;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.refer.ImportType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Setter
@Accessors(chain = true)
public class InterfaceDecl extends ClassDecl {

    public boolean withStatic = true;

    public InterfaceDecl(
        String name,
        @Nullable BaseType superClass,
        List<BaseType> interfaces,
        List<TSVariableType> variableTypes
    ) {
        super(name, superClass, interfaces, variableTypes);
    }

    public ClassDecl createStaticClass() {
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

        // Format head - export interface Interf$$Interface<T> extends ... {
        String head = "export interface " + ImportType.STATIC.fmt(name) + TSVariableType.formatGenericParam(variableTypes, declaration);
        if (!interfaces.isEmpty()) {
            head += " extends " + Types.join(", ", interfaces).line(declaration);
        }
        head += " {";

        // Format body - fields, constructors, methods
        List<String> body = new ArrayList<>();

        for (val method : methods) {
            if (!method.isStatic) {// static method will be in static class
                body.addAll(method.format(declaration));
            }
        }
        // field (all static) will be in static class

        // tail - }
        List<String> tail = new ArrayList<>();
        for (val code : bodyCode) {
            tail.addAll(code.format(declaration));
        }
        tail.add("}\n");

        // Concatenate them as a whole
        List<String> formatted = new ArrayList<>();
        formatted.add(head);
        formatted.addAll(body);
        formatted.addAll(tail);

        return formatted;
    }
}
