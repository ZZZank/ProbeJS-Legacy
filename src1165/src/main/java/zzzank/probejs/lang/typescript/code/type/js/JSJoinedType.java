package zzzank.probejs.lang.typescript.code.type.js;

import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

public abstract class JSJoinedType extends BaseType {
    public final List<? extends BaseType> types;

    protected JSJoinedType(Collection<? extends BaseType> types) {
        this.types = new ArrayList<>(types);
    }

    @Override
    public ImportInfos getImportInfos(@Nonnull FormatType type) {
        return ImportInfos.of().fromCodes(types, type);
    }

    public abstract StringJoiner createJoiner();

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        val joiner = createJoiner();
        for (val t : types) {
            joiner.add(t.line(declaration, formatType));
        }
        return joiner.toString();
    }

    public static class Union extends JSJoinedType {
        public Union(Collection<? extends BaseType> types) {
            super(types);
        }

        @Override
        public StringJoiner createJoiner() {
            return new StringJoiner(" | ", "(", ")");
        }
    }

    public static class Intersection extends JSJoinedType {
        public Intersection(Collection<? extends BaseType> types) {
            super(types);
        }

        @Override
        public StringJoiner createJoiner() {
            return new StringJoiner(" & ", "(", ")");
        }
    }

    public static class Custom extends JSJoinedType {
        private final CharSequence delimiter;
        private final CharSequence prefix;
        private final CharSequence suffix;

        public Custom(
            Collection<? extends BaseType> types,
            CharSequence delimiter,
            CharSequence prefix,
            CharSequence suffix
        ) {
            super(types);
            this.delimiter = delimiter;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public StringJoiner createJoiner() {
            return new StringJoiner(delimiter, prefix, suffix);
        }
    }
}
