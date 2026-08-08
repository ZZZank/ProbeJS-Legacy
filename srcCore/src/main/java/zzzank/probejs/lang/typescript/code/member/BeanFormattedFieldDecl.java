package zzzank.probejs.lang.typescript.code.member;

import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class BeanFormattedFieldDecl extends FieldDecl {
    public BeanFormattedFieldDecl(String name, BaseType type) {
        super(name, type);
    }

    @Override
    public List<String> format(Declaration declaration) {
        if (isStatic || isFinal) {
            return super.format(declaration);
        }

        var lines = new ArrayList<String>((this.comments.size() + 1) * 2);

        var getter = new BeanDecl.Getter(this.name, this.type);
        getter.comments.addAll(this.comments);
        lines.addAll(getter.format(declaration));

        var setter = new BeanDecl.Setter(this.name, this.type);
        setter.comments.addAll(this.comments);
        lines.addAll(setter.format(declaration));

        return lines;
    }
}
