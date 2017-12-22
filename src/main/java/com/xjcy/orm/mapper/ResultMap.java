package com.xjcy.orm.mapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResultMap
{
	Map<String, Field> map = new HashMap<>();
	public void put(String name, Field field)
	{
		map.put(name, field);
	}

	public Set<String> keySet()
	{
		return map.keySet();
	}

	public Field get(String key)
	{
		return map.get(key);
	}

	public int size()
	{
		return map.size();
	}

}
