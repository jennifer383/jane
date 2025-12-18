package com.common.jane_ai.RepateExecuteLimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
//注解的属性其实是无参数、无方法体的方法，Java 设计它们看起来像字段，但语法上必须用方法 () 来定义和调用。
public @interface RepateExecuteLimit {

    String type();

    String userName() default "0";

    String userId() default "0";

    String chatId() default "0";

    long durationTime() default 5L;
}
