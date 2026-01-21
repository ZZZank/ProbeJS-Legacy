package zzzank.probejs.lang.typescript.code.type;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.type.js.JSJoinedType;
import zzzank.probejs.lang.typescript.code.type.ts.TSArrayAccessType;
import zzzank.probejs.lang.typescript.code.type.ts.TSArrayType;
import zzzank.probejs.lang.typescript.code.type.ts.TSOptionalType;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;
import zzzank.probejs.lang.typescript.code.type.utility.ContextShield;
import zzzank.probejs.lang.typescript.code.type.utility.ImportShield;
import zzzank.probejs.lang.typescript.code.type.utility.WithFormatType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.utils.CollectUtils;

import java.util.*;

public abstract class BaseType extends Code {
    @Override
    @Deprecated
    public final ImportInfos getImportInfos() {
        return getImportInfos(FormatType.RETURN);
    }

    public abstract ImportInfos getImportInfos(@NotNull FormatType type);

    public final List<String> format(Declaration declaration) {
        return format(declaration, FormatType.RETURN);
    }

    public List<String> format(Declaration declaration, FormatType formatType) {
        return Collections.singletonList(line(declaration, formatType));
    }

    public abstract String line(Declaration declaration, FormatType formatType);

    public String line(Declaration declaration) {
        return line(declaration, FormatType.RETURN);
    }
    // Stuffs for convenience

    public TSArrayType asArray() {
        return zzzank.probejs.lang.typescript.code.type.Types.array(this);
    }

    public TSArrayAccessType arrayAccess(BaseType index) {
        return new TSArrayAccessType(this, index);
    }

    public TSArrayAccessType arrayAccess(String index) {
        return new TSArrayAccessType(this, Types.primitive(index));
    }

    public ContextShield<BaseType> contextShield(FormatType formatType) {
        return Types.contextShield(this, formatType);
    }

    public ImportShield<BaseType> importShield(ImportInfos imports) {
        return Types.importShield(this, imports);
    }

    public TSOptionalType optional() {
        return Types.optional(this);
    }

    public TSParamType withParams(BaseType... params) {
        return Types.parameterized(this, params);
    }

    public TSParamType withParams(String... params) {
        return Types.parameterized(this, CollectUtils.mapToList(params, Types::primitive));
    }

    public TSParamType withParams(Collection<? extends BaseType> params) {
        return Types.parameterized(this, params);
    }

    /**
     * @return the type itself if {@code params} is empty, otherwise a new {@link TSParamType}
     */
    public BaseType withPossibleParams(@NotNull Collection<? extends BaseType> params) {
        if (params.isEmpty()) {
            return this;
        }
        return Types.parameterized(this, params);
    }

    public JSJoinedType.Union or(BaseType... types) {
        val selfTypes = this instanceof JSJoinedType.Union u
            ? u.types
            : Collections.singletonList(this);
        val joined = new ArrayList<BaseType>(selfTypes.size() + types.length);
        joined.addAll(selfTypes);
        joined.addAll(Arrays.asList(types));
        return (JSJoinedType.Union) Types.or(joined);
    }

    public JSJoinedType.Intersection and(BaseType... types) {
        val selfTypes = this instanceof JSJoinedType.Intersection i
            ? i.types
            : Collections.singletonList(this);
        val joined = new ArrayList<BaseType>(selfTypes.size() + types.length);
        joined.addAll(selfTypes);
        joined.addAll(Arrays.asList(types));
        return (JSJoinedType.Intersection) Types.and(joined);
    }

    public WithFormatType comment(String comment) {
        return Types.withComment(this, comment);
    }

    public enum FormatType {
        INPUT,
        RETURN,
        VARIABLE
    }
}
