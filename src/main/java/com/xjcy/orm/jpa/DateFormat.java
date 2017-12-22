package com.xjcy.orm.jpa;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface DateFormat
{
	String pattern() default "yyyy-MM-dd HH:mm:ss";
}
