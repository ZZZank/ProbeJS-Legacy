package zzzank.probejs.lang.java.clazz.members;

import zzzank.probejs.lang.java.base.TypeVariableHolder;
import zzzank.probejs.utils.CollectUtils;

import java.lang.reflect.Constructor;
import java.util.List;

public class ConstructorInfo extends TypeVariableHolder  {

    public final List<ParamInfo> params;

    public ConstructorInfo(Constructor<?> constructor) {
        super(constructor.getTypeParameters(), constructor.getAnnotations());
        this.params = CollectUtils.mapToList(constructor.getParameters(), ParamInfo::new);
    }
}
