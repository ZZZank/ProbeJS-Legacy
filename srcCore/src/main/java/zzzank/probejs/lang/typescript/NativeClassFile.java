package zzzank.probejs.lang.typescript;

import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.InterfaceDecl;

/**
 * @author ZZZank
 */
public class NativeClassFile extends TypeScriptFile {
    public ClassDecl nativeClass;

    public NativeClassFile(ClassPath self) {
        super(self);
    }

    public void addNativeClass(ClassDecl decl) {
        if (decl.nativeClazz == null) {
            throw new IllegalArgumentException("provided ClassDecl does not have `nativeClazz`");
        }
        nativeClass = decl;
        addCode(nativeClass);
        if (nativeClass instanceof InterfaceDecl interfaceDecl && interfaceDecl.withStatic) {
            addCode(interfaceDecl.getStaticClass());
        }
    }
}
