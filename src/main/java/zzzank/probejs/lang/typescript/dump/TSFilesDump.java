package zzzank.probejs.lang.typescript.dump;

import lombok.val;
import zzzank.probejs.api.output.AutoSplitPackagedWriter;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.lang.typescript.TypeScriptFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
public class TSFilesDump extends TSDumpBase {
    /// return `false` -> this file should be removed from dump
    public final List<Predicate<TypeScriptFile>> modifiers = new ArrayList<>();
    public Collection<TypeScriptFile> files = Collections.emptyList();

    public TSFilesDump(Path writeTo) {
        this(AutoSplitPackagedWriter.defaultSetup(), writeTo);
    }

    public TSFilesDump(TSFileWriter writer, Path writeTo) {
        super(writer, writeTo);
    }

    @Override
    protected void dumpImpl() throws IOException {
        val modifier = and(modifiers);
        for (var file : files) {
            if (modifier.test(file)) {
                writer.accept(file);
            }
        }
        writer.write(writeTo);
    }

    protected static <T> Predicate<T> and(Collection<? extends Predicate<T>> predicates) {
        if (predicates.isEmpty()) {
            return a -> true;
        } else if (predicates.size() == 1) {
            return predicates.iterator().next();
        } else if (predicates.size() == 2) {
            val iter = predicates.iterator();
            val pred1 = iter.next();
            val pred2 = iter.next();
            return pred1.and(pred2);
        } else if (predicates.size() == 3) {
            val iter = predicates.iterator();
            val pred1 = iter.next();
            val pred2 = iter.next();
            val pred3 = iter.next();
            return t -> pred1.test(t) && pred2.test(t) && pred3.test(t);
        } else if (predicates.size() == 4) {
            val iter = predicates.iterator();
            val pred1 = iter.next();
            val pred2 = iter.next();
            val pred3 = iter.next();
            val pred4 = iter.next();
            return t -> pred1.test(t) && pred2.test(t) && pred3.test(t) && pred4.test(t);
        }
        val predArray = (Predicate<T>[]) predicates.toArray(new Predicate[0]);
        return t -> {
            for (val pred : predArray) {
                if (!pred.test(t)) {
                    return false;
                }
            }
            return true;
        };
    }
}
