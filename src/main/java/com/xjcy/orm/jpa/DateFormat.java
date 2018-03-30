package com.xjcy.orm.jpa;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;

@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface DateFormat
{
	String pattern() default "yyyy-MM-dd HH:mm:ss";
}
