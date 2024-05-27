package org.tbfeng.apt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 注解用在方法上
@Retention(RetentionPolicy.SOURCE) // 尽在Source处理期间可用，运行期不可用
public @interface BuildProperty {
}
