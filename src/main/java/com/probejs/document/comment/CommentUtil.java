package com.probejs.document.comment;

import com.probejs.document.DocComment;
import com.probejs.document.comment.special.CommentHidden;
import com.probejs.document.comment.special.CommentMod;
import com.probejs.document.comment.special.CommentModify;
import com.probejs.document.comment.special.CommentRename;
import com.probejs.document.type.DocType;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.probejs.formatter.FormatterComments.CommentStyle.J_DOC;

public interface CommentUtil {

    static boolean isLoaded(DocComment comment) {
        if (comment == null) {
            return true;
        }
        return comment
            .getSpecialComments(CommentMod.class)
            .stream()
            .allMatch(CommentMod::isLoaded);
    }

    static boolean isHidden(DocComment comment) {
        if (comment == null) {
            return false;
        }
        return comment.getSpecialComment(CommentHidden.class) != null;
    }

    static Map<String, DocType> getTypeModifiers(DocComment comment) {
        Map<String, DocType> modifiers = new HashMap<>();
        if (comment != null) {
            comment
                .getSpecialComments(CommentModify.class)
                .forEach(modify -> modifiers.put(modify.getName(), modify.getType()));
        }
        return modifiers;
    }

    static Map<String, String> getRenames(DocComment comment) {
        Map<String, String> renames = new HashMap<>();
        if (comment == null) {
            return renames;
        }
        comment
            .getSpecialComments(CommentRename.class)
            .forEach(rename -> renames.put(rename.getName(), rename.getTo()));
        return renames;
    }

    /**
     * "/**" -> null
     * "/*   " -> null
     * "/**wow" -> " * wow"
     */
    @Nullable
    static String convertFirstLine(String line) {
        line = line.trim();
        if ("/*".equals(line) || "/**".equals(line)) {
            return null;
        }
        if (line.startsWith("/**")) {
            return J_DOC.getInline() + line.substring(3);
        }
        return line;
    }

    /**
     * " *\/" -> null
     * "okkkk*\/" -> " * okkk"
     */
    @Nullable
    static String convertLastLine(String line) {
        line = line.trim();
        if ("*/".equals(line)) {
            return null;
        }
        if (line.endsWith("*/")) {
            val start = line.startsWith("* ") ? 2 : 0;
            val end = line.length() - 2;
            return J_DOC.getInline() + line.substring(start, end);
        }
        return line;
    }

    /**
     * remove one star-mark with one space("* ") at the front, if any.
     *
     * @return processed string, or itself if it has no "*" at the front.
     */
    static String trimInnerLine(String line) {
        line = line.trim();
        if (line.equals("*")) {
            return "";
        }
        if (line.startsWith("* ")) {
            return line.substring(2);
        }
        return line;
    }
}
