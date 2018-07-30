package com.xjcy.orm.mapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 查询结果和对象字段的映射
 * @author YYDF
 *
 */
public class ResultMap {
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
}
