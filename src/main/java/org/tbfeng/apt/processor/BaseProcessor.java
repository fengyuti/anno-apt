package org.tbfeng.apt.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import org.tbfeng.apt.domian.LClass;
import org.tbfeng.apt.domian.ProcessContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 抽象执行器
 */
public abstract class BaseProcessor extends AbstractProcessor {

    /**
     * Messager主要是用来在编译期打log用的
     */
    protected Messager messager;

    /**
     * JavacTrees提供了待处理的抽象语法树
     */
    protected JavacTrees trees;

    /**
     * TreeMaker封装了创建AST节点的一些方法
     */
    protected TreeMaker treeMaker;

    /**
     * Names提供了创建标识符的方法
     */
    protected Names names;

    /**
     * 执行上下文
     */
    protected ProcessContext processContext;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.processContext = ProcessContext.newInstance()
                .messager(messager)
                .names(names)
                .treeMaker(treeMaker)
                .trees(trees);
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<LClass> classList = getClassList(roundEnv, getAnnotationClass());

        for (LClass lClass : classList) {
            handleClass(lClass);
        }
        return true;
    }

    /**
     * 获取对应的 class 信息列表
     *
     * @param roundEnv 环境信息
     * @param clazz    注解类型
     * @return 列表
     */
    protected List<LClass> getClassList(final RoundEnvironment roundEnv,
                                        final Class<? extends Annotation> clazz) {
        List<LClass> classList = new ArrayList<LClass>();
        Set<? extends Element> serialSet = roundEnv.getElementsAnnotatedWith(clazz);
        // 对于每一个类可以分开，使用多线程进行处理。
        for (Element element : serialSet) {
            if (element instanceof Symbol.ClassSymbol) {
                LClass lClass = new LClass(processContext, (Symbol.ClassSymbol) element);
                classList.add(lClass);
            }
        }
        return classList;
    }


    /**
     * 获取抽象的注解类型
     */
    protected abstract Class<? extends Annotation> getAnnotationClass();

    /**
     * 处理单个类信息
     */
    protected abstract void handleClass(final LClass lClass);

}
