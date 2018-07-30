package com.xjcy.orm.jpa;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.xjcy.util.STR;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;

@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Column
{
	String name() default STR.EMPTY;
}
