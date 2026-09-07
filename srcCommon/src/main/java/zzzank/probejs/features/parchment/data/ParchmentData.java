package zzzank.probejs.features.parchment.data;

import java.util.Comparator;
import java.util.List;

/**
 * Over-simplified container for parchment mapping data.
 *
 * @author ZZZank
 */
public class ParchmentData {
    public String version;
    public List<ParchmentPackage> packages = List.of();
    public List<ParchmentClass> classes = List.of();

    public static class ParchmentPackage {
        public String name;
        public List<String> javadoc = List.of();
    }

    public static class ParchmentClass {
        public String name;
        public List<String> javadoc = List.of();
        public List<ParchmentMethod> methods = List.of();
        public List<ParchmentField> fields = List.of();
    }

    public static class ParchmentMethod {
        public String name;
        public String descriptor;
        public List<String> javadoc = List.of();
        public List<ParchmentParameter> parameters = List.of();
    }

    public static class ParchmentField {
        public String name;
        public List<String> javadoc = List.of();
    }

    public static class ParchmentParameter {
        public static final Comparator<ParchmentParameter> COMPARE_BY_INDEX = Comparator.comparingInt(p -> p.localIndex);

        public int localIndex;
        public String name;
    }
}
