package zzzank.probejs.lang.typescript.code.type.js;

import lombok.AllArgsConstructor;
import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

@AllArgsConstructor
public abstract class JSMemberType extends BaseType {
    public final Collection<JSParam> members;

    @Override
    public ImportInfos getImportInfos(@Nonnull FormatType type) {
        return ImportInfos.of().fromCodes(members.stream().map(JSParam::type), type);
    }

    protected StringJoiner formatMembers(StringJoiner joiner, Declaration declaration, FormatType type) {
        val nameProcessor = (UnaryOperator<String>) this::getMemberName;
        for (val member : members) {
            joiner.add(member.format(declaration, type, nameProcessor));
        }
        return joiner;
    }

    protected abstract String getMemberName(String name);

    public static abstract class Builder<SELF extends Builder<SELF, OUT>, OUT extends BaseType> {
        public final Collection<JSParam> members = new ArrayList<>(3);

        public SELF member(String name, BaseType type) {
            return member(name, false, type);
        }

        @SuppressWarnings("unchecked")
        public SELF member(JSParam param) {
            members.add(param);
            return (SELF) this;
        }

        public SELF member(String name, boolean optional, BaseType type) {
            return member(new JSParam(name, optional, type));
        }

        public SELF literalMember(String name, boolean optional, BaseType type) {
            return member(new JSParam.Literal(name, optional, type));
        }

        public SELF literalMember(String name, BaseType type) {
            return literalMember(name, false, type);
        }

        public abstract OUT build();
    }
}
