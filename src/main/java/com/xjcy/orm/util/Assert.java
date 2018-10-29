package com.xjcy.orm.util;

public class Assert {

	public static void notNull(Object obj) {
		if(obj == null)
			throw new IllegalArgumentException("The value can not be null");
	}

}
