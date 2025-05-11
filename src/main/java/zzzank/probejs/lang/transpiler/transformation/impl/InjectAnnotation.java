package zzzank.probejs.lang.transpiler.transformation.impl;

import dev.latvian.mods.rhino.annotations.typing.JSInfo;
import dev.latvian.mods.rhino.annotations.typing.JSParams;
import lombok.val;
import net.minecraftforge.api.distmarker.OnlyIn;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.java.base.AnnotationHolder;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.member.*;

import java.util.Arrays;
import java.util.Collection;

public class InjectAnnotation implements ClassTransformer {

    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        applyInfo(clazz, classDecl);
        applyDeprecated(clazz, classDecl);
    }

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl decl) {
        applyInfo(methodInfo, decl);
        applyDeprecated(methodInfo, decl);
        applyJSParams(methodInfo, decl.params);
        applySideOnly(methodInfo, decl);

        if (RhizoState.INFO_ANNOTATION) {
            val paramLines = methodInfo.params.stream()
                .filter(p -> p.hasAnnotation(JSInfo.class))
                .map(p -> String.format("@param %s - %s", p.name, p.getAnnotation(JSInfo.class).value()))
                .toArray(String[]::new);
            if (paramLines.length != 0) {
                decl.linebreak();
                decl.addComment(paramLines);
            }
        }
    }

    @Override
    public void transformField(Clazz clazz, FieldInfo fieldInfo, FieldDecl decl) {
        applyInfo(fieldInfo, decl);
        applyDeprecated(fieldInfo, decl);
        applySideOnly(fieldInfo, decl);
    }

    @Override
    public void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl decl) {
        applyInfo(constructorInfo, decl);
        applyDeprecated(constructorInfo, decl);
        applyJSParams(constructorInfo, decl.params);
        applySideOnly(constructorInfo, decl);
    }

    public void applyDeprecated(AnnotationHolder info, CommentableCode decl) {
        val anno = info.getAnnotation(Deprecated.class);
        if (anno != null) {
            decl.newline("@deprecated");
        }
    }

    public void applyInfo(AnnotationHolder info, CommentableCode decl) {
        if (!RhizoState.INFO_ANNOTATION) {
            return;
        }
        val infoAnnotation = info.getAnnotation(JSInfo.class);
        if (infoAnnotation != null) {
            decl.addComment(infoAnnotation.value());
        }
    }

    public void applyJSParams(AnnotationHolder parent, Collection<ParamDecl> params) {
        if (!RhizoState.INFO_ANNOTATION) {
            return;
        }
        val jsParams = parent.getAnnotation(JSParams.class);
        if (jsParams == null || jsParams.params().length == 0) {
            return;
        }

        val jsParamIterator = Arrays.asList(jsParams.params()).iterator();
        val declIterator = params.iterator();

        while (jsParamIterator.hasNext() && declIterator.hasNext()) {
            val jsParam = jsParamIterator.next();
            if (jsParam != null && !jsParam.rename().isEmpty()) {
                declIterator.next().name = jsParam.rename();
            }
        }
    }

    public void applySideOnly(AnnotationHolder target, CommentableCode decl) {
        val dist = target.getAnnotationOptional(OnlyIn.class)
            .map(OnlyIn::value)
            .orElse(null);
        if (dist == null) {
            return;
        }
        decl.linebreak();
        if (dist.isClient()) {
            decl.addComment("Client only, do not use in server scripts");
        } else if (dist.isDedicatedServer()) {
            decl.addComment("Server only, do not use in client scripts");
        }
    }
}
