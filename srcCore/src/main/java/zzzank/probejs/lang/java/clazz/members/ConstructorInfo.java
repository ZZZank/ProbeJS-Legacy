package zzzank.probejs.lang.java.clazz.members;

import zzzank.probejs.lang.java.base.TypeVariableHolder;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ConstructorInfo extends TypeVariableHolder  {

    public final List<ParamInfo> params;

    public ConstructorInfo(Constructor<?> constructor) {
        super(constructor.getTypeParameters(), constructor.getAnnotations());
        var parameters = constructor.getParameters();
        this.params = new ArrayList<>(parameters.length);
        for (var i = 0; i < parameters.length; i++) {
            params.add(i, new ParamInfo(parameters[i], i));
        }
    }
}
