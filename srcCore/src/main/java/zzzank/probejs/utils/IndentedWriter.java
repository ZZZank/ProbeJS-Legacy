package zzzank.probejs.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

/**
 * writer wrapper that can make written line indented based on the indent value set by {@link #setIndent(int, char)} and
 * {@link #pushIndent()} and {@link #popIndent()}
 * @author ZZZank
 */
public class IndentedWriter<T extends Writer> extends Writer {

    /**
     * create a {@link IndentedWriter} that uses 4 spaces as indentation
     * @param writer writer to be wrapped
     */
    public static <T extends Writer> IndentedWriter<T> space(T writer) {
        return new IndentedWriter<>(writer, 4, ' ');
    }

    /**
     * create a {@link IndentedWriter} that uses 1 tab as indentation
     * @param writer writer to be wrapped
     */
    public static <T extends Writer> IndentedWriter<T> tab(T writer) {
        return new IndentedWriter<>(writer, 1, '\t');
    }

    /**
     * create a IndentedWriter with a writer to be wrapped and an indent step which will be used by {@link #pushIndent()}
     * and {@link #popIndent()}
     *
     * @param writer writer to be wrapped
     * @param indentStep how much the length of indentation should be increased/decreased, will only be used by
     * {@link #pushIndent()} and {@link #popIndent()}
     * @param indentChar the char used as the elements of indentation
     */
    public static <T extends Writer> IndentedWriter<T> create(T writer, int indentStep, char indentChar) {
        return new IndentedWriter<>(writer, indentStep, indentChar);
    }

    private int indent = 0;
    private char[] indentChars = new char[0];
    private StringBuffer buffer = new StringBuffer();
    private final Scope scope = new Scope();
    public final T out;
    public final int indentStep;
    public final char indentChar;

    private IndentedWriter(T out, int indentStep, char indentChar) {
        this.out = out;
        this.indentStep = indentStep;
        this.indentChar = indentChar;
    }

    public void setIndent(int indent, char indentChar) {
        if (indent < 0) {
            throw new IllegalArgumentException();
        }
        this.indent = indent;
        this.indentChars = new char[indent];
        Arrays.fill(indentChars, indentChar);
    }

    @Override
    public void write(char @NotNull [] chars, int off, int len) throws IOException {
        int last = off;
        int begin;
        final int end = off + len;
        while (true) {
            begin = findNextLineBreak(chars, last, end);
            if (begin < 0) {
                break;
            }
            buffer.append(chars, last, begin + 1 - last); // include the found '\n'
            writeBuffer();
            last = begin + 1;
        }
        if (last != end) {
            buffer.append(chars, last, end - last);
        }
    }

    public void line(@NotNull String str) throws IOException {
        write(str);
        write('\n');
    }

    private void writeBuffer() throws IOException {
        out.write(indentChars);
        out.write(buffer.toString());
        buffer = new StringBuffer();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        if (buffer.length() != 0) {
            writeBuffer();
        }
        out.close();
    }

    private static int findNextLineBreak(final char[] chars, final int begin, final int end) {
        for (int i = begin; i < end; i++) {
            if (chars[i] == '\n') {
                return i;
            }
        }
        return -1;
    }

    /**
     * @see #indentStep
     * @see #indentChar
     * @see #setIndent(int, char)
     */
    public void pushIndent() {
        setIndent(indent + indentStep, this.indentChar);
    }

    /**
     * @see #indentStep
     * @see #indentChar
     * @see #setIndent(int, char)
     */
    public void popIndent() {
        setIndent(indent - indentStep, this.indentChar);
    }

    /**
     * a utility method for automatically pushing/popping indent
     * <p>
     * when this method is invoked, {@link #pushIndent()} will be called to increase indent level
     * <p>
     * the return value of this method is a {@link AutoCloseable} that, when it's {@link AutoCloseable#close()} is
     * invoked, will call {@link #popIndent()} of this writer to decrease indent level
     * <p>
     * so, by calling this in a {@code try-with-resources} block, indent level of this writer can be automatically
     * increased/decreased:
     * <pre>
     * IndentedWriter writer = ... ;
     * writer.write("no indent\n");
     * try (var ignored = writer.scope()) {
     *     writer.write("have indent\n")
     * }
     * writer.write("no indent again\n");
     * </pre>
     */
    public Scope scope() {
        pushIndent();
        return scope;
    }

    public class Scope implements AutoCloseable {
        @Override
        public void close() {
            popIndent();
        }
    }
}
