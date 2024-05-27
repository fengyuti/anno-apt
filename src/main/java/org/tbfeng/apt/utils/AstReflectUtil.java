package org.tbfeng.apt.utils;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public final class AstReflectUtil {


    /**
     * 反射调用
     *
     * @param treeMaker 语法树
     * @param fieldName 字段名
     * @param lhs       左边
     * @param rhs       右边
     * @return 结果
     */
    public static JCTree.JCBinary invokeJcBinary(final TreeMaker treeMaker,
                                                 final String fieldName,
                                                 final JCTree.JCExpression lhs,
                                                 final JCTree.JCExpression rhs) {
        final Object treeTag = getTreeTag(fieldName);
        final Method method = getJcBinaryMethod();
        return (JCTree.JCBinary) ReflectMethodUtil.invoke(treeMaker, method, treeTag, lhs, rhs);
    }

    /**
     * 获取方法
     *
     * @return 方法
     */
    private static Method getJcBinaryMethod() {
        try {
            int version = JavacUtil.getJavaCompilerVersion();

            final String methodName = "Binary";
            if (version < 8) {
                return ClassUtil.getMethod(TreeMaker.class, methodName, int.class, JCTree.JCExpression.class, JCTree.JCExpression.class);
            }

            String className = "com.sun.tools.javac.tree.JCTree$Tag";
            Class tagClass = Class.forName(className);
            return ClassUtil.getMethod(TreeMaker.class, methodName, tagClass, JCTree.JCExpression.class, JCTree.JCExpression.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据名字获取真实的值
     *
     * @param fieldName 名字
     * @return 值
     */
    private static Object getTreeTag(final String fieldName) {
        //6-7
        int version = JavacUtil.getJavaCompilerVersion();
        if (version < 8) {
            String className = "com.sun.tools.javac.tree.JCTree";

            return getStaticFieldValue(className, fieldName);
        }

        String className = "com.sun.tools.javac.tree.JCTree$Tag";
        return getEnumConstValue(className, fieldName);
    }

    /**
     * 获取静态字段信息
     *
     * @param className 类名
     * @param fieldName 字段名
     * @return 结果
     */
    public static Object getStaticFieldValue(final String className,
                                             final String fieldName) {
        try {
            Class clazz = Class.forName(className);
            Field field = ReflectFieldUtil.getField(clazz, fieldName);
            return ReflectFieldUtil.getValue(field, null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取枚举常量值
     *
     * @param className 类名
     * @param enumName  枚举名
     * @return 结果
     */
    public static Object getEnumConstValue(final String className,
                                           final String enumName) {
        try {
            Class clazz = Class.forName(className);
            Object[] objects = clazz.getEnumConstants();
            for (Object object : objects) {
                if (enumName.equalsIgnoreCase(object.toString())) {
                    return object;
                }
            }

            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * opcode = [6-7] int  [8] JCTree.Tag
     * treeTag 对应的类
     *
     * @return 对应的类
     */
    private static String treeTagClass() {
        return JavacUtil.getJavaCompilerVersion() < 8 ? "com.sun.tools.javac.tree.JCTree" : "com.sun.tools.javac.tree.JCTree$Tag";
    }

}
