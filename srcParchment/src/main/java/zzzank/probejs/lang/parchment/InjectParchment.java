package zzzank.probejs.lang.parchment;

import org.jetbrains.annotations.Nullable;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.parchment.data.IndexedMappingData;
import zzzank.probejs.lang.parchment.data.StringIndexer;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.ConstructorDecl;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.lang.typescript.code.member.ParamDecl;
import zzzank.probejs.utils.CollectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZZZank
 */
public class InjectParchment implements ClassTransformer {

    private final Map<String, IndexedMappingData.IndexedClass> classes;
    private final Map<String, Map<String, IndexedMappingData.IndexedMethod>> methodsByClass;

    public InjectParchment(IndexedMappingData data) {
        var mapExpectedSize = CollectUtils.calcMapExpectedSize(data.classes.size());
        this.classes = new HashMap<>(mapExpectedSize);
        this.methodsByClass = new HashMap<>(mapExpectedSize);

        var indexer = data.indexer;
        for (var clazz : data.classes) {
            var className = indexer.getValue(clazz.name.intValue()).replace('/', '.');
            classes.put(className, clazz);

            if (clazz.methods != null) {
                var methodMap = new HashMap<String, IndexedMappingData.IndexedMethod>(clazz.methods.size());
                for (var method : clazz.methods) {
                    methodMap.put(reconstructSignature(method, indexer), method);
                }
                methodsByClass.put(className, methodMap);
            }
        }
    }

    @Nullable
    public IndexedMappingData.IndexedClass getClass(String className) {
        return classes.get(className);
    }

    @Nullable
    public IndexedMappingData.IndexedMethod getMethod(String className, String signature) {
        return methodsByClass.getOrDefault(className, Map.of()).get(signature);
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
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        var className = getClassName(clazz);
        if (className == null) {
            return;
        }

        var methodData = this.getMethod(className, MethodSignature.forMethod(methodInfo));
        if (methodData == null) {
            return;
        }

        this.applyParameterNames(methodData, methodDecl.params);
    }

    @Override
    public void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        var className = getClassName(clazz);
        if (className == null) {
            return;
        }

        var methodData = this.getMethod(className, MethodSignature.forConstructor(constructorInfo));
        if (methodData == null) {
            return;
        }

        this.applyParameterNames(methodData, constructorDecl.params);
    }

    private String getClassName(Clazz clazz) {
        var path = clazz.classPath;
        var name = path.getOriginalName();
        // skip artificial ClassPath that has no original name
        if (name.isEmpty()) {
            return null;
        }
        return name;
    }
}
