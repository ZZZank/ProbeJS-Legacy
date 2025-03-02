package zzzank.probejs.features.rhizo;

import com.google.common.base.Suppliers;
import net.minecraftforge.fml.ModList;

import java.util.function.Supplier;

import static zzzank.probejs.utils.ReflectUtils.classExist;

/**
 * @author ZZZank
 */
public interface RhizoState {

    Supplier<Boolean> MOD = Suppliers.memoize(() -> ModList.get().isLoaded("rhizo"));
    boolean ENUM_TYPE_WRAPPER = classExist("dev.latvian.mods.rhino.util.wrap.EnumTypeWrapper");
    boolean ENUM_TYPE_INFO = classExist("dev.latvian.mods.rhino.native_java.type.info.EnumTypeInfo");
    boolean REMAPPER = classExist("dev.latvian.mods.rhino.util.remapper.RemapperManager");
    boolean GENERIC_ANNOTATION = classExist("dev.latvian.mods.rhino.annotations.typing.Generics");
    boolean INFO_ANNOTATION = classExist("dev.latvian.mods.rhino.annotations.typing.JSInfo");
    boolean RETURNS_SELF_ANNOTATION = classExist("dev.latvian.mods.rhino.annotations.typing.ReturnsSelf");
}
