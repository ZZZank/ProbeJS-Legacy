package zzzank.probejs.lang.parchment;

import org.jetbrains.annotations.Nullable;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.parchment.data.IndexedMappingData;
import zzzank.probejs.lang.parchment.data.StringIndexer;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.member.*;
import zzzank.probejs.utils.CollectUtils;

import java.util.List;
import java.util.Map;

/**
 * @author ZZZank
 */
public class InjectParchment implements ClassTransformer {

    private final Map<String, ClassInfo> infos;

    public InjectParchment(IndexedMappingData data) {
        this.infos = CollectUtils.ofSizedMap(data.classes.size());

        var indexer = data.indexer;
        for (var indexedClass : data.classes) {
            var className = indexer.getValue(indexedClass.name.intValue()).replace('/', '.');

            Map<String, IndexedMappingData.IndexedMethod> methods;
            if (indexedClass.methods != null) {
                methods = CollectUtils.ofSizedMap(indexedClass.methods.size());
                for (var method : indexedClass.methods) {
                    methods.put(reconstructSignature(method, indexer), method);
                }
            } else {
                methods = Map.of();
            }

            Map<String, IndexedMappingData.IndexedNamedType> fields;
            if (indexedClass.fields != null) {
                fields = CollectUtils.ofSizedMap(indexedClass.fields.size());
                for (var field : indexedClass.fields) {
                    fields.put(field.name, field);
                }
            } else {
                fields = Map.of();
            }

            this.infos.put(className, new ClassInfo(indexedClass, methods, fields));
        }
    }

    @Nullable
    public IndexedMappingData.IndexedClass getClass(String className) {
        var info = infos.get(className);
        if (info == null) {
            return null;
        }
        return info.indexed;
    }

    @Nullable
    public IndexedMappingData.IndexedMethod getMethod(String className, String signature) {
        return infos.getOrDefault(className, ClassInfo.EMPTY).methods.get(signature);
    }

    @Nullable
    public IndexedMappingData.IndexedNamedType getField(String className, String fieldName) {
        return infos.getOrDefault(className, ClassInfo.EMPTY).fields.get(fieldName);
    }

    /**
     * Apply parameter names from an {@link IndexedMappingData.IndexedMethod} to a parameter declaration list.
     * <p>
     * Unlike the old {@code ParameterData.getIndex()} path (which uses bytecode slot indices),
     * {@code IndexedMethod.desc} already has parameter names at the correct argument position
     * after the builder's slot → arg mapping.
     */
    public void applyParameterNames(IndexedMappingData.IndexedMethod method, List<ParamDecl> declParams) {
        // desc has N arguments + 1 return type at the end — skip the return
        int limit = Math.min(method.desc.size() - 1, declParams.size());
        for (int i = 0; i < limit; i++) {
            var name = method.desc.get(i).name;
            if (name != null && !name.isEmpty()) {
                declParams.get(i).name = name;
            }
        }
    }

    public void applyJavadoc(IndexedMappingData.JavaDocHolder docHolder, CommentableCode commentable) {
        if (docHolder.doc != null) {
            commentable.addComment(docHolder.doc);
        }
    }

    private static String reconstructSignature(IndexedMappingData.IndexedMethod method, StringIndexer indexer) {
        var desc = method.desc;
        var sb = new StringBuilder().append(method.name).append('(');
        // all entries except the last are argument types
        var limit = desc.size() - 1;
        for (int i = 0; i < limit; i++) {
            sb.append(toDescriptor(indexer.getValue(desc.get(i).type.intValue())));
        }
        sb.append(')');
        // last entry is return type
        sb.append(toDescriptor(indexer.getValue(desc.get(limit).type.intValue())));
        return sb.toString();
    }

    /**
     * Convert a type's internal name (as stored in {@link StringIndexer}) back to JVM descriptor format.
     * <ul>
     *   <li>Primitives ({@code I}, {@code V}, …) → pass through unchanged</li>
     *   <li>Arrays starting with {@code [} → pass through unchanged</li>
     *   <li>Objects → wrap as {@code L…;}</li>
     * </ul>
     */
    static String toDescriptor(String internalName) {
        if (internalName.length() == 1) {
            char c = internalName.charAt(0);
            switch (c) {
                case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'V' -> {
                    return internalName;
                }
            }
        }
        if (internalName.startsWith("[")) {
            return internalName;
        }
        return "L" + internalName + ";";
    }

    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        var info = this.infos.get(clazz.classPath.getOriginalName());
        if (info == null) {
            return;
        }

        this.applyJavadoc(info.indexed, classDecl);
    }

    @Override
    public void transformField(Clazz clazz, FieldInfo fieldInfo, FieldDecl fieldDecl) {
        var field = this.getField(clazz.classPath.getOriginalName(), fieldInfo.name);
        if (field == null) {
            return;
        }

        this.applyJavadoc(field, fieldDecl);
    }

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        var method = this.getMethod(clazz.classPath.getOriginalName(), MethodSignature.forMethod(methodInfo));
        if (method == null) {
            return;
        }

        this.applyParameterNames(method, methodDecl.params);
        this.applyJavadoc(method, methodDecl);
    }

    @Override
    public void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        var constructor = this.getMethod(clazz.classPath.getOriginalName(), MethodSignature.forConstructor(constructorInfo));
        if (constructor == null) {
            return;
        }

        this.applyParameterNames(constructor, constructorDecl.params);
        this.applyJavadoc(constructor, constructorDecl);
    }

    /// @param methods method signature -> method
    /// @param fields field name -> field
    private record ClassInfo(
        IndexedMappingData.IndexedClass indexed,
        Map<String, IndexedMappingData.IndexedMethod> methods,
        Map<String, IndexedMappingData.IndexedNamedType> fields
    ) {
        private static final ClassInfo EMPTY = new ClassInfo(null, Map.of(), Map.of());
    }
}
