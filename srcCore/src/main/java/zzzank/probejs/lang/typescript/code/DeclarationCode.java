package zzzank.probejs.lang.typescript.code;

import java.util.Set;

/**
 * @author ZZZank
 */
public interface DeclarationCode {

    void reportDeclaredNames(Set<String> existed);
}
