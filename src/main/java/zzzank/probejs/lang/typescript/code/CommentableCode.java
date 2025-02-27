package zzzank.probejs.lang.typescript.code;

import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class CommentableCode extends Code {
    public final List<String> comments = new ArrayList<>();

    /**
     * @return a mutable view of {@link #comments}, with blank lines at the front/back removed
     */
    public List<String> getTrimmedComments() {
        int begin = 0;
        int end = comments.size();
        while (begin < end && comments.get(begin).trim().isEmpty()) {
            begin++;
        }
        while (end > begin && comments.get(end - 1).trim().isEmpty()) {
            end--;
        }
        return comments.subList(begin, end);
    }

    public List<String> formatComments() {
        val trimmed = getTrimmedComments();
        if (trimmed.size() == 1) {
            return Collections.singletonList("/** " + trimmed.get(0) + " */");
        }
        List<String> formatted = new ArrayList<>(trimmed.size() + 2);
        formatted.add("/**");
        for (val comment : trimmed) {
            formatted.add(" * " + comment);
        }
        formatted.add(" */");
        return formatted;
    }

    public abstract List<String> formatRaw(Declaration declaration);

    public final List<String> format(Declaration declaration) {
        if (comments.isEmpty()) {
            return formatRaw(declaration);
        }
        val formattedRaw = formatRaw(declaration);
        val formattedComments = formatComments();
        val result = new ArrayList<String>(formattedComments.size() + formattedRaw.size());
        result.addAll(formattedComments);
        result.addAll(formattedRaw);
        return result;
    }

    public void addComment(String... comments) {
        for (String comment : comments) {
            this.comments.addAll(Arrays.asList(comment.split("\\n")));
        }
    }

    public void addCommentAtStart(String... comments) {
        List<String> lines = new ArrayList<>();
        for (String comment : comments) {
            lines.addAll(Arrays.asList(comment.split("\\n")));
        }
        this.comments.addAll(0, lines);
    }

    public void linebreak() {
        comments.add("");
    }

    public void newline(String... comments) {
        this.comments.add("");
        addComment(comments);
    }
}
