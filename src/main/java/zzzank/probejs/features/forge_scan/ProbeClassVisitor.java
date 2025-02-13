package zzzank.probejs.features.forge_scan;

import lombok.val;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class ProbeClassVisitor extends ClassVisitor {

    /**
     * The runtime visible annotations of this class. May be {@code null}.
     */
    public List<AnnotationNode> visibleAnnotations;

    /**
     * The runtime invisible annotations of this class. May be {@code null}.
     */
    public List<AnnotationNode> invisibleAnnotations;

    public ProbeClassVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        val annotation = new AnnotationNode(descriptor);
        if (visible) {
            if (visibleAnnotations == null) {
                visibleAnnotations = new ArrayList<>();
            }
            visibleAnnotations.add(annotation);
        } else {
            if (invisibleAnnotations == null) {
                invisibleAnnotations = new ArrayList<>();
            }
            invisibleAnnotations.add(annotation);
        }
        return annotation;
    }
}
