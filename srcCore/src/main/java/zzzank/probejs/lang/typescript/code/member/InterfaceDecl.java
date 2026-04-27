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
            ImportType.STATIC.fmt(this.name),
            null,
            Collections.singletonList(Types.primitive(this.name).withPossibleParams(variableTypes)),
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

        // Format head - export interface name<T> extends ... {
        String head = String.format("export interface %s", name);
        if (!variableTypes.isEmpty()) {
            head += Types.join(", ", "<", ">", variableTypes)
                .line(declaration, BaseType.FormatType.VARIABLE);
        }
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

        // Static methods and fields, adds it even if it's empty, so auto import can still discover it
        if (this.withStatic) {
            formatted.addAll(createStaticClass().format(declaration));
        }
        return formatted;
    }
}
