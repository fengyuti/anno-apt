package org.tbfeng.apt.annotation;

import java.lang.annotation.*;

/**
 * 注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ToString {

    /**
     * 转换为字符串的实现方式
     */
    String value() default "";

}
