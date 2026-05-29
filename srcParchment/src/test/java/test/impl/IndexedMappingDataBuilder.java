package test.impl;

import org.objectweb.asm.Type;
import org.parchmentmc.feather.mapping.MappingDataContainer;
import zzzank.probejs.lang.parchment.data.IndexedMappingData;
import zzzank.probejs.lang.parchment.data.StringIndexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds {@link IndexedMappingData} from a Parchment {@link MappingDataContainer}.
 * <p>
 * All the mapping/population logic lives here, keeping {@code IndexedMappingData}
 * and its nested classes as pure data containers.
 *
 * @author ZZZank
 */
public class IndexedMappingDataBuilder {

    public static IndexedMappingData build(MappingDataContainer container) {
        var result = new IndexedMappingData();
        var indexer = result.indexer;

        var classes = new ArrayList<IndexedMappingData.IndexedClass>(container.getClasses().size());
        for (var classData : container.getClasses()) {
            classes.add(buildClass(classData, indexer));
        }
        result.classes = classes;

        indexer.freeze();

        result.indexedDiff = indexer.buildDiffs();
        return result;
    }

    private static IndexedMappingData.IndexedClass buildClass(
        MappingDataContainer.ClassData classData,
        StringIndexer indexer
    ) {
        var indexedClass = new IndexedMappingData.IndexedClass();

        indexedClass.name = indexer.addOrGetIndex(classData.getName());
        indexedClass.doc = joinLines(classData.getJavadoc());

        List<IndexedMappingData.IndexedMethod> methods = null;
        if (!classData.getMethods().isEmpty()) {
            methods = new ArrayList<>(classData.getMethods().size());
            for (var method : classData.getMethods()) {
                methods.add(buildMethod(method, indexer));
            }
        }
        indexedClass.methods = methods;

        List<IndexedMappingData.IndexedNamedType> fields = null;
        if (!classData.getFields().isEmpty()) {
            fields = new ArrayList<>(classData.getFields().size());
            for (var field : classData.getFields()) {
                var indexed = new IndexedMappingData.IndexedNamedType();
                indexed.type = indexer.addOrGetIndex(
                    Type.getType(field.getDescriptor()).getInternalName()
                );
                indexed.name = field.getName();
                indexed.doc = joinLines(field.getJavadoc());
                fields.add(indexed);
            }
        }
        indexedClass.fields = fields;

        return indexedClass;
    }

    private static IndexedMappingData.IndexedMethod buildMethod(
        MappingDataContainer.MethodData method,
        StringIndexer indexer
    ) {
        var indexedMethod = new IndexedMappingData.IndexedMethod();
        indexedMethod.name = method.getName();
        indexedMethod.doc = joinLines(method.getJavadoc());

        var methodType = Type.getMethodType(method.getDescriptor());
        var argumentTypes = methodType.getArgumentTypes();

        var desc = new ArrayList<IndexedMappingData.IndexedNamedType>(argumentTypes.length + 1);
        for (var argumentType : argumentTypes) {
            var nt = new IndexedMappingData.IndexedNamedType();
            nt.type = indexer.addOrGetIndex(argumentType.getInternalName());
            desc.add(nt);
        }
        {
            var nt = new IndexedMappingData.IndexedNamedType();
            nt.type = indexer.addOrGetIndex(methodType.getReturnType().getInternalName());
            desc.add(nt);
        }

        // Map ParameterData bytecode slot index -> desc argument index
        // Handles: min offset (0=static, 1=instance) + long/double 2-slot gap
        var min = method.getParameters()
            .stream()
            .mapToInt(MappingDataContainer.ParameterData::getIndex)
            .min()
            .orElse(0);
        if (min < 2) {
            // build slot → argument lookup (long/double take 2 local variable slots)
            int totalSlots = 0;
            for (var argType : argumentTypes) {
                totalSlots += argType.getSize();
            }
            var slotToArg = new int[totalSlots];
            int slot = 0;
            for (int i = 0; i < argumentTypes.length; i++) {
                slotToArg[slot] = i;
                slot += argumentTypes[i].getSize();
            }
            for (var parameter : method.getParameters()) {
                var indexedParam = desc.get(slotToArg[parameter.getIndex() - min]);
                indexedParam.name = parameter.getName();
                indexedParam.doc = parameter.getJavadoc();
            }
        }
        indexedMethod.desc = desc;

        return indexedMethod;
    }

    static String joinLines(List<String> lines) {
        return lines.isEmpty() ? null : String.join("\n", lines);
    }
}
