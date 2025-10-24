package zzzank.probejs.lang.typescript.code.type.js;

import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.Declaration;

import java.util.Collection;
import java.util.StringJoiner;

public class JSObjectType extends JSMemberType {

    public JSObjectType(Collection<JSParam> members) {
        super(members);
    }

    @Override
    protected String getMemberName(String name) {
        return ProbeJS.GSON.toJson(name);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return formatMembers(new StringJoiner(", ", "{", "}"), declaration, formatType).toString();
    }

    public static class Builder extends JSMemberType.Builder<Builder, JSObjectType> {
        public JSObjectType build() {
            return new JSObjectType(members);
        }
    }
}
