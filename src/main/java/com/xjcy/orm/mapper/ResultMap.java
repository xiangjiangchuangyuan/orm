package com.xjcy.orm.mapper;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.xjcy.orm.core.FieldUtils;

/**
 * 查询结果和对象字段的映射
 * 
 * @author YYDF
 *
 */
public class ResultMap {

	private static final Object LOCK_OBJ = new Object();
	final Map<String, Field> map = new HashMap<>();

	public void put(String label, Field field) {
		map.put(label, field);
	}

	public Field get(String label) {
		return map.get(label);
	}

	public int size() {
		return map.size();
	}

	public Set<String> Keys() {
		return map.keySet();
	}

	/**
	 * 
	 * @param tt 实例化的对象
	 * @param t  对象的类型
	 * @param label 字段名
	 * @param obj	取到的数据库对应值
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 */
	public void setObject(Object tt, Class<?> t, String label, Object obj)
			throws IllegalArgumentException, IllegalAccessException, IntrospectionException {
		if (obj != null) {
			Field field = map.get(label);
			synchronized (LOCK_OBJ) {
				field.setAccessible(true);
				field.set(tt, FieldUtils.ConvertValue(field, t, obj));
				field.setAccessible(false);
			}
		}
	}
}
