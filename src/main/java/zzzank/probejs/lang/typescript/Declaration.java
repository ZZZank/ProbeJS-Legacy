package zzzank.probejs.lang.typescript;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.refer.ImportInfo;
import zzzank.probejs.lang.typescript.refer.Reference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Declaration {
    public static final String UNIQUE_TEMPLATE = "%s$%d";

    public final Map<ClassPath, Reference> references = new HashMap<>();
    public final Set<String> usedNames = new HashSet<>();
    public final Set<String> excludedNames = new HashSet<>();

    public void addImport(ImportInfo info) {
        val existed = references.get(info.path);
        if (existed != null) {//already resolved
            existed.info.mergeWith(info);
            return;
        }
        // So we determine a unique original that is safe to use at startup
        val uniqueName = computeSymbol(info.path);
        usedNames.add(uniqueName);
        this.references.put(info.path, new Reference(info, uniqueName));
    }

    public void exclude(String name) {
        excludedNames.add(name);
    }

    public boolean containsSymbol(String name) {
        return excludedNames.contains(name) || usedNames.contains(name);
    }

    private String computeSymbol(ClassPath path) {
        val original = path.getName();
        //try original, then try template
        var deduped = original;
        int counter = 0;
        while (containsSymbol(deduped)) {
            deduped = String.format(UNIQUE_TEMPLATE, original, counter++);
        }
        return deduped;
    }

    public String getSymbol(ClassPath path) {
        val reference = this.references.get(path);
        if (reference == null) {
            throw new RuntimeException(String.format("Trying to get a symbol of unresolved classpath: %s", path));
        }
        return reference.deduped;
    }
}
