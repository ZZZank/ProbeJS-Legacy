package com.probejs.formatter;

import com.probejs.document.DocumentField;
import com.probejs.document.comment.special.CommentHidden;
import com.probejs.formatter.api.DocumentReceiver;
import com.probejs.formatter.api.IFormatter;
import com.probejs.formatter.resolver.NameResolver;
import com.probejs.info.clazz.FieldInfo;
import com.probejs.info.type.TypeResolver;
import com.probejs.util.PUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class FormatterField extends DocumentReceiver<DocumentField> implements IFormatter {

    @Getter
    private final FieldInfo fieldInfo;
    @Setter
    private boolean isFromInterface = false;

    public FormatterField(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    @Override
    public List<String> format(int indent, int stepIndent) {
        List<String> lines = new ArrayList<>();
        val comment = document != null ? document.getComment() : null;
        if (comment != null) {
            if (comment.getSpecialComment(CommentHidden.class) != null) {
                return lines;
            }
            lines.addAll(comment.format(indent, stepIndent));
        }

        val builder = new StringBuilder(PUtil.indent(indent));
        if (fieldInfo.isStatic() && !isFromInterface) {
            builder.append("static ");
        }
        if (fieldInfo.isFinal()) {
            builder.append("readonly ");
        }
        builder.append(fieldInfo.getName());
        builder.append(": ");

        if (document != null) {
            builder.append(document.getType().getTypeName());
        } else if (fieldInfo.isStatic() && NameResolver.formatValue(fieldInfo.getStaticValue()) != null) {
            builder.append(NameResolver.formatValue(fieldInfo.getStaticValue()));
        } else {
            builder.append(
                FormatterType.of(
                    fieldInfo.getType(),
                    NameResolver.specialTypeGuards.getOrDefault(
                        TypeResolver.getContainedTypeOrSelf(fieldInfo.getType()).getResolvedClass(),
                        true
                    )
                )
                    .format()
            );
        }
        builder.append(';');
        lines.add(builder.toString());
        return lines;
    }

}
