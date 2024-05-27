package org.tbfeng.apt.processor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import org.tbfeng.apt.annotation.ToString;
import org.tbfeng.apt.constant.MethodConst;
import org.tbfeng.apt.domian.LClass;
import org.tbfeng.apt.utils.AstReflectUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * toString() 实现策略
 */
@SupportedAnnotationTypes("org.tbfeng.apt.annotation.ToString")
public class ToStringProcessor extends BaseProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        System.out.println("-----ToStringProcessor-----");
        super.init(processingEnv);
        System.out.println("-----" + processingEnv.getOptions() + "-----");
    }


    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ToString.class;
    }

    @Override
    protected void handleClass(LClass lClass) {
        if (!lClass.containsMethod(MethodConst.TO_STRING)) {
            generateToStringMethod(lClass);
        }
    }

    /**
     * 创建一个 toString() 方法
     */
    private void generateToStringMethod(LClass lClass) {
        ListBuffer<JCTree> defBufferList = new ListBuffer<JCTree>();
        List<JCTree> defList = lClass.classDecl().defs;
        // 添加旧方法
        defBufferList.addAll(defList);

        // 新增一个方法
        final JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
        Name name = names.fromString(MethodConst.TO_STRING);

        // 表达式
        // 这里缺少了 @Override 注解
        List<JCTree.JCStatement> statements = createToStringStatements(lClass);

        JCTree.JCBlock jcBlock = treeMaker.Block(0, statements);
        JCTree.JCExpression restype = treeMaker.Ident(names.fromString("String"));
        JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(modifiers,
                name,
                restype,
                List.<JCTree.JCTypeParameter>nil(),
                List.<JCTree.JCVariableDecl>nil(),
                List.<JCTree.JCExpression>nil(),
                jcBlock,
                null);


        // 重新赋值
        defBufferList.add(methodDecl);

        lClass.classDecl().defs = defBufferList.toList();
    }

    /**
     * 构建 toString() 语句
     *
     * @param lClass 类
     * @return 结果
     */
    private List<JCTree.JCStatement> createToStringStatements(final LClass lClass) {
        // 基于字符串拼接的实现
        return createStringConcatStatements(lClass);
    }

    /**
     * 创建
     * <p>
     * 遍历字段，拼接。
     *
     * <pre>
     *
     * </pre>
     * <p>
     * 说明: 这里因为 jdk tools 版本不同，导致包冲突。
     * 利用反射调用
     * <p>
     * 暂时不做处理。
     */
    private List<JCTree.JCStatement> createStringConcatStatements(final LClass lClass) {
        String fullClassName = lClass.classSymbol().fullname.toString();
        String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

        //2. 构建 statement
        // 所有的字符串都是一个 Literal
        JCTree.JCLiteral start = treeMaker.Literal(className + "{");
        // 输出字段信息
        JCTree.JCBinary lhs = null;

        // 兼容 jdk tools 版本差异
        final String treeTagName = "PLUS";
        for (JCTree jcTree : lClass.classDecl().defs) {
            if (jcTree.getKind() == Tree.Kind.VARIABLE) {
                JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) jcTree;
                String varName = variableDecl.name.toString();

                // 初次加載
                if (lhs == null) {
                    JCTree.JCLiteral fieldName = treeMaker.Literal(varName + "=");
                    lhs = AstReflectUtil.invokeJcBinary(treeMaker, treeTagName,
                            start, fieldName);
                } else {
                    JCTree.JCLiteral fieldName = treeMaker.Literal(", " + varName + "=");
                    lhs = AstReflectUtil.invokeJcBinary(treeMaker, treeTagName,
                            lhs, fieldName);
                }

                // 类型为 String 可以考虑加单引号，但是没必要。，判断逻辑比较麻烦
                String typeName = variableDecl.vartype.toString();
                if (typeName.endsWith("[]")) {
                    JCTree.JCMethodInvocation methodInvocation = buildArraysToString(lClass, varName);
                    lhs = AstReflectUtil.invokeJcBinary(treeMaker, treeTagName,
                            lhs, methodInvocation);
                } else {
                    // 默认直接使用字符串
                    JCTree.JCIdent fieldValue = treeMaker.Ident(names.fromString(varName));
                    lhs = AstReflectUtil.invokeJcBinary(treeMaker, treeTagName,
                            lhs, fieldValue);
                }
            }
        }

        JCTree.JCLiteral rhs = treeMaker.Literal("}");
        JCTree.JCBinary binary = AstReflectUtil.invokeJcBinary(treeMaker, treeTagName,
                lhs, rhs);
        JCTree.JCStatement statement = treeMaker.Return(binary);
        return List.of(statement);
    }

    /**
     * 构建数组调用
     *
     * <pre>
     *     Arrays.toString("xxx");
     * </pre>
     *
     * @param lClass  类
     * @param varName 命名
     */
    private JCTree.JCMethodInvocation buildArraysToString(final LClass lClass,
                                                          final String varName) {
        lClass.importPackage(lClass, Arrays.class);

        //2. 构建 statement
        JCTree.JCFieldAccess fieldAccess = treeMaker.Select(treeMaker.Ident(names.fromString("Arrays")), names.fromString("toString"));
        // 避免类型擦除
        ListBuffer<JCTree.JCExpression> identBuffers = new ListBuffer<JCTree.JCExpression>();
        identBuffers.add(treeMaker.Ident(names.fromString(varName)));

        return treeMaker.Apply(List.<JCTree.JCExpression>nil(),
                fieldAccess, identBuffers.toList());
    }

}
