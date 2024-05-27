package org.tbfeng.apt.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.util.CollectionUtils;
import org.tbfeng.apt.constant.FieldConst;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class 工具类
 */
public final class ClassUtil {


    /**
     * 序列版本编号常量
     */
    public static final String SERIAL_VERSION_UID = "serialVersionUID";

    /**
     * 获取对应类的默认变量名：
     * 1. 首字母小写
     * String=》string
     *
     * @param className 类名称
     * @return 类的默认变量名
     */
    public static String getClassVar(final String className) {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    /**
     * 获取类所有的字段信息
     * ps: 这个方法有个问题 如果子类和父类有相同的字段 会不会重复
     * 1. 还会获取到 serialVersionUID 这个字段。 0.1.77 移除
     *
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> getAllFieldList(final Class clazz) {
        List<Field> allFieldList = new ArrayList<>();
        Class tempClass = clazz;
        while (tempClass != null) {
            allFieldList.addAll(Lists.newArrayList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }

        List<Field> resultList = new ArrayList<>();
        for (Field field : allFieldList) {
            String fieldName = field.getName();

            // 跳过序列化字段
            if (FieldConst.SERIAL_VERSION_UID.equals(fieldName)) {
                continue;
            }

            field.setAccessible(true);
            resultList.add(field);
        }
        return resultList;
    }

    /**
     * 获取可变更的字段信息
     * （1）过滤掉 final 的字段信息
     *
     * @param clazz 类信息
     * @return 0.1.35
     */
    public static List<Field> getModifyableFieldList(final Class clazz) {
        List<Field> allFieldList = getAllFieldList(clazz);
        if (CollectionUtils.isEmpty(allFieldList)) {
            return allFieldList;
        }

        // 过滤掉 final 的字段
        return allFieldList.stream()
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .collect(Collectors.toList());

    }

    /**
     * 获取类所有的字段信息 map
     * ps: 这个方法有个问题 如果子类和父类有相同的字段 会不会重复
     * 1. 还会获取到 serialVersionUID 这个字段。
     *
     * @param clazz 类
     * @return 字段列表 map
     */
    public static Map<String, Field> getAllFieldMap(final Class clazz) {
        List<Field> fieldList = ClassUtil.getAllFieldList(clazz);
        return fieldList.stream()
                .collect(Collectors.toMap(Field::getName, Function.identity()));

    }


    /**
     * bean 转换为 map
     *
     * @param bean 原始对象
     * @return 结果
     * @deprecated 已废弃
     */
    @Deprecated
    public static Map<String, Object> beanToMap(Object bean) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            List<Field> fieldList = ClassUtil.getAllFieldList(bean.getClass());

            for (Field field : fieldList) {
                final String fieldName = field.getName();
                final Object fieldValue = field.get(bean);
                map.put(fieldName, fieldValue);
            }
            return map;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对象的实例化
     *
     * @param clazz 类
     * @param <T>   泛型
     * @return 实例化对象
     */
    public static <T> T newInstance(final Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取所有字段的 read 方法列表
     *
     * @param clazz 类信息
     * @return 方法列表
     * @throws IntrospectionException if any
     */
    public static List<Method> getAllFieldsReadMethods(final Class clazz) throws IntrospectionException {
        List<Field> fieldList = getAllFieldList(clazz);
        if (CollectionUtils.isEmpty(fieldList)) {
            return Collections.emptyList();
        }

        List<Method> methods = new ArrayList<>();
        for (Field field : fieldList) {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
            //获得get方法
            Method getMethod = pd.getReadMethod();
            methods.add(getMethod);
        }
        return methods;
    }

    /**
     * 获取当前的 class loader
     *
     * @return 当前的 class loader
     */
    public static ClassLoader currentClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 获取类信息
     *
     * @param className 类名称信息
     * @return 构建后的类信息
     */
    public static Class getClass(final String className) {
        ArgUtil.notEmpty(className, "className");

        try {
            return currentClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取方法信息
     *
     * @param clazz      类信息
     * @param methodName 方法名称
     * @param paramTypes 参数类型
     * @return 方法信息
     */
    @SuppressWarnings("unchecked")
    public static Method getMethod(final Class clazz,
                                   final String methodName,
                                   final Class... paramTypes) {
        ArgUtil.notNull(clazz, "clazz");
        ArgUtil.notEmpty(methodName, "methodName");

        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取构造器信息
     *
     * @param clazz      类
     * @param paramTypes 参数类型数组
     * @return 构造器
     */
    @SuppressWarnings("unchecked")
    public static Constructor getConstructor(final Class clazz,
                                             final Class... paramTypes) {
        ArgUtil.notNull(clazz, "clazz");

        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取方法列表
     * （1）当前类和超类的 public 方法
     *
     * @param tClass 类型
     * @return 结果列表
     */
    public static List<Method> getMethodList(final Class tClass) {
        ArgUtil.notNull(tClass, "tClass");

        Method[] methods = tClass.getMethods();
        return Arrays.asList(methods);
    }

    /**
     * 获取方法列表
     * （1）当前类的方法，包括私有。
     * （2）暂时不进行递归处理，后期看是否有需要。
     *
     * @param tClass 类型
     * @return 结果列表
     */
    public static List<Method> getDeclaredMethodList(final Class tClass) {
        ArgUtil.notNull(tClass, "tClass");

        Method[] methods = tClass.getDeclaredMethods();
        return Arrays.asList(methods);
    }

    /**
     * 获取所有父类信息
     *
     * @param clazz 类型
     * @return 所有父类信息
     */
    public static List<Class> getAllSuperClass(final Class clazz) {
        ArgUtil.notNull(clazz, "clazz");

        Set<Class> classSet = Sets.newHashSet();

        // 添加所有父类
        Class tempClass = clazz.getSuperclass();
        while (tempClass != null) {
            classSet.add(tempClass);
            tempClass = tempClass.getSuperclass();
        }

        return Lists.newArrayList(classSet);
    }

    /**
     * 获取所有接口信息
     *
     * @param clazz 类型
     * @return 所有父类信息
     */
    public static List<Class> getAllInterfaces(final Class clazz) {
        ArgUtil.notNull(clazz, "clazz");

        Set<Class> classSet = Sets.newHashSet();

        // 添加所有父类
        Class[] interfaces = clazz.getInterfaces();
        if (!Objects.isNull(interfaces)) {
            classSet.addAll(Arrays.asList(interfaces));

            for (Class interfaceClass : interfaces) {
                List<Class> classList = getAllInterfaces(interfaceClass);
                if (!CollectionUtils.isEmpty(classList)) {
                    classSet.addAll(classList);
                }
            }
        }

        return Lists.newArrayList(classSet);
    }

    /**
     * 获取所有接口信息和父类信息
     *
     * @param clazz 类型
     * @return 接口信息和父类信息
     */
    public static List<Class> getAllInterfacesAndSuperClass(final Class clazz) {
        ArgUtil.notNull(clazz, "clazz");

        Set<Class> classSet = Sets.newHashSet();
        classSet.addAll(getAllInterfaces(clazz));
        classSet.addAll(getAllSuperClass(clazz));

        return Lists.newArrayList(classSet);
    }

    /**
     * 是否可以设置
     *
     * @param sourceType 原始类型
     * @param targetType 目标类型
     * @return 结果
     */
    public static boolean isAssignable(final Class<?> sourceType, final Class<?> targetType) {
        // 如果有任何一个字段为空，直接返回
        if (Objects.isNull(sourceType)
                || Objects.isNull(targetType)) {
            return false;
        }

        if (sourceType.isAssignableFrom(targetType)) {
            return true;
        }

        // 基础类型的判断
        Class resolvedPrimitive;
        if (sourceType.isPrimitive()) {
            resolvedPrimitive = PrimitiveUtil.getPrimitiveType(targetType);
            return sourceType == resolvedPrimitive;
        } else {
            resolvedPrimitive = PrimitiveUtil.getPrimitiveType(targetType);
            return resolvedPrimitive != null && sourceType.isAssignableFrom(resolvedPrimitive);
        }

    }

}
