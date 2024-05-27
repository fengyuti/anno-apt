package org.tbfeng.apt.utils;

import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 反射字段工具类
 */
public final class ReflectFieldUtil {

    /**
     * 是否可以设置
     *
     * @param sourceField 原始字段
     * @param targetField 目标字段
     * @return 结果
     */
    public static boolean isAssignable(final Field sourceField, final Field targetField) {
        // 如果有任何一个字段为空，直接返回
        if (Objects.isNull(sourceField)
                || Objects.isNull(targetField)) {
            return false;
        }

        // 如果 target 的字段为 final 则不进行设置
        if (Modifier.isFinal(targetField.getModifiers())) {
            return false;
        }

        final Class<?> sourceType = sourceField.getType();
        final Class<?> targetType = targetField.getType();

        return ClassUtil.isAssignable(sourceType, targetType);
    }

    /**
     * 判断字段为字符串类型
     *
     * @param field 字段
     * @return {@code true} 是
     */
    public static Boolean isString(Field field) {
        return field.getType() == String.class;
    }

    /**
     * 判断字段是否不为字符串类型
     *
     * @param field 字段
     * @return {@code true} 是
     */
    public static Boolean isNotString(Field field) {
        return !isString(field);
    }


    /**
     * 判断字段field,声明了clazz注解
     *
     * @param field 字段
     * @param clazz 注解
     * @return 是否声明了
     */
    public static boolean isAnnotationPresent(Field field, Class<? extends Annotation> clazz) {
        return field.isAnnotationPresent(clazz);
    }

    /**
     * 判断字段field,未声明clazz注解
     *
     * @param field 字段
     * @param clazz 注解
     * @return 是否未声明
     */
    public static boolean isNotAnnotationPresent(Field field, Class<? extends Annotation> clazz) {
        return !isAnnotationPresent(field, clazz);
    }


    /**
     * 当前类包含指定的注解信息
     *
     * @param clazz           类
     * @param annotationClass 注解类
     * @return 是否包含
     */
    public static boolean containsAnnotationField(final Class clazz,
                                                  final Class<? extends Annotation> annotationClass) {
        ArgUtil.notNull(clazz, "Clazz");
        ArgUtil.notNull(annotationClass, "Annotation class");

        List<Field> fieldList = ClassUtil.getAllFieldList(clazz);
        if (CollectionUtils.isEmpty(fieldList)) {
            return false;
        }

        for (Field field : fieldList) {
            if (field.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 设置字段的值
     *
     * @param field      字段
     * @param instance   实例对象
     * @param fieldValue 值
     * @see #setValue(Object, String, Object) 设置对象
     */
    public static void setValue(final Field field, final Object instance, final Object fieldValue) {
        try {
            field.setAccessible(true);
            field.set(instance, fieldValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置字段值
     *
     * @param instance   实例
     * @param fieldName  字段名称
     * @param fieldValue 字段值
     */
    public static void setValue(final Object instance, final String fieldName, final Object fieldValue) {
        ArgUtil.notNull(instance, "instance");

        try {
            final Class clazz = instance.getClass();
            Map<String, Field> fieldNameMap = ClassUtil.getAllFieldMap(clazz);
            Field field = fieldNameMap.get(fieldName);
            field.setAccessible(true);
            field.set(instance, fieldValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对应的值信息
     *
     * @param field    字段
     * @param instance 实例
     * @return 值
     */
    public static Object getValue(final Field field, final Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对应的值信息
     *
     * @param fieldName 字段名称
     * @param instance  实例
     * @return 值
     */
    public static Object getValue(final String fieldName, final Object instance) {
        Field field = getField(instance, fieldName);
        return getValue(field, instance);
    }

    /**
     * 获取指定字段名称的字段信息
     *
     * @param object    对象实例
     * @param fieldName 字段名称
     * @return 字段信息
     */
    public static Field getField(final Object object, final String fieldName) {
        ArgUtil.notNull(object, "object");

        final Class clazz = object.getClass();
        return getField(clazz, fieldName);
    }

    /**
     * 获取指定字段名称的字段信息
     *
     * @param clazz     类名称
     * @param fieldName 字段名称
     * @return 字段信息
     */
    public static Field getField(final Class clazz, final String fieldName) {
        ArgUtil.notNull(clazz, "clazz");
        ArgUtil.notEmpty(fieldName, "fieldName");

        List<Field> fieldList = ClassUtil.getAllFieldList(clazz);

        for (Field field : fieldList) {
            String name = field.getName();
            if (name.equals(fieldName)) {
                return field;
            }
        }
        throw new RuntimeException("Field not found for fieldName: " + fieldName);
    }

}
