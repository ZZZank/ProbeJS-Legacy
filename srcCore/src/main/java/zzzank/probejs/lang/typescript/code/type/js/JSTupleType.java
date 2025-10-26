package zzzank.probejs.lang.typescript.code.type.js;

import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.utils.NameUtils;

import java.util.Collection;
import java.util.StringJoiner;

public class JSTupleType extends JSMemberType {

    public JSTupleType(Collection<JSParam> members) {
        super(members);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return formatMembers(new StringJoiner(", ", "[", "]"), declaration, formatType).toString();
    }

    @Override
    protected String getMemberName(String name) {
        return NameUtils.isNameSafe(name) ? name : "arg";
    }

    public static class Builder extends JSMemberType.Builder<Builder, JSTupleType> {

        @Override
        public JSTupleType build() {
            return new JSTupleType(members);
        }
    }
}
