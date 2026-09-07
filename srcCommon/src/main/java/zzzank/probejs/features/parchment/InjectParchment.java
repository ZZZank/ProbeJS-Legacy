package zzzank.probejs.features.parchment;

import org.jetbrains.annotations.Nullable;
import zzzank.probejs.features.parchment.data.ParchmentData;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.clazz.members.ParamInfo;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.member.*;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.ReflectUtils;

import java.util.List;
import java.util.Map;

/**
 * @author ZZZank
 */
public class InjectParchment implements ClassTransformer {

    public final ParchmentData data;
    private final Map<String, ClassInfo> infos;

    public InjectParchment(ParchmentData data) {
        this.data = data;
        this.infos = CollectUtils.ofSizedMap(data.classes.size());

        for (var parchmentClass : data.classes) {
            var className = parchmentClass.name.replace('/', '.');

            Map<String, ParchmentData.ParchmentMethod> methods;
            if (parchmentClass.methods != null && !parchmentClass.methods.isEmpty()) {
                methods = CollectUtils.ofSizedMap(parchmentClass.methods.size());
                for (var method : parchmentClass.methods) {
                    methods.put(method.name + method.descriptor, method);
                }
            } else {
                methods = Map.of();
            }

            Map<String, ParchmentData.ParchmentField> fields;
            if (parchmentClass.fields != null && !parchmentClass.fields.isEmpty()) {
                fields = CollectUtils.ofSizedMap(parchmentClass.fields.size());
                for (var field : parchmentClass.fields) {
                    fields.put(field.name, field);
                }
            } else {
                fields = Map.of();
            }

            this.infos.put(className, new ClassInfo(parchmentClass, methods, fields));
        }
    }

    @Nullable
    public ParchmentData.ParchmentClass getClass(String className) {
        var info = infos.get(className);
        if (info == null) {
            return null;
        }
        return info.parchment;
    }

    @Nullable
    public ParchmentData.ParchmentMethod getMethod(String className, String descriptor) {
        return infos.getOrDefault(className, ClassInfo.EMPTY).methods.get(descriptor);
    }

    @Nullable
    public ParchmentData.ParchmentField getField(String className, String fieldName) {
        return infos.getOrDefault(className, ClassInfo.EMPTY).fields.get(fieldName);
    }

    /**
     * Apply parameter names from a {@link ParchmentData.ParchmentMethod} to a parameter declaration list.
     */
    private void applyParameterNames(
        ParchmentData.ParchmentMethod method,
        List<ParamDecl> paramDecls,
        List<ParamInfo> params,
        boolean isStatic
    ) {
        if (method.parameters == null || method.parameters.isEmpty()) {
            return;
        }

        // handle sparse and unordered list
        var localIndexMap = CollectUtils.<Integer, ParchmentData.ParchmentParameter>ofSizedMap(method.parameters.size());
        for (var param : method.parameters) {
            if (param.name != null && !param.name.isEmpty()) {
                localIndexMap.put(param.localIndex, param);
            }
        }

        int localIndex = isStatic ? 0 : 1;
        for (int i = 0; i < params.size(); i++) {
            var paramInfo = params.get(i);
            var paramDecl = paramDecls.get(i);

            var parchmentParam = localIndexMap.get(localIndex);
            if (parchmentParam != null) {
                paramDecl.name = parchmentParam.name;
            }

            localIndex += ReflectUtils.getSlotSize(paramInfo.type.asClass());
        }
    }

    private void applyJavadoc(List<String> javadoc, CommentableCode commentable) {
        if (javadoc != null && !javadoc.isEmpty()) {
            commentable.addComment(String.join("\n", javadoc));
        }
    }

    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        var info = this.infos.get(clazz.classPath.getOriginalName());
        if (info == null) {
            return;
        }

        this.applyJavadoc(info.parchment.javadoc, classDecl);
    }

    @Override
    public void transformField(Clazz clazz, FieldInfo fieldInfo, FieldDecl fieldDecl) {
        var field = this.getField(clazz.classPath.getOriginalName(), fieldInfo.name);
        if (field == null) {
            return;
        }

        this.applyJavadoc(field.javadoc, fieldDecl);
    }

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        var method = this.getMethod(clazz.classPath.getOriginalName(), MethodSignature.forMethod(methodInfo));
        if (method == null) {
            return;
        }

        this.applyParameterNames(method, methodDecl.params, methodInfo.params, methodInfo.attributes.isStatic);
        this.applyJavadoc(method.javadoc, methodDecl);
    }

    @Override
    public void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        var constructor = this.getMethod(clazz.classPath.getOriginalName(), MethodSignature.forConstructor(constructorInfo));
        if (constructor == null) {
            return;
        }

        this.applyParameterNames(constructor, constructorDecl.params, constructorInfo.params, false);
        this.applyJavadoc(constructor.javadoc, constructorDecl);
    }

    /// @param methods method descriptor -> method
    /// @param fields field name -> field
    private record ClassInfo(
        ParchmentData.ParchmentClass parchment,
        Map<String, ParchmentData.ParchmentMethod> methods,
        Map<String, ParchmentData.ParchmentField> fields
    ) {
        private static final ClassInfo EMPTY = new ClassInfo(null, Map.of(), Map.of());
    }
}
