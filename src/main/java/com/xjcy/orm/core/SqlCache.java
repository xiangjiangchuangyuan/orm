package com.xjcy.orm.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xjcy.orm.mapper.ResultMap;
import com.xjcy.orm.mapper.TableStruct;

public class SqlCache
{
	private static final Map<String, ResultMap> buildMap = new ConcurrentHashMap<>();
	private static final Map<String, TableStruct> entityMap = new ConcurrentHashMap<>();

	public static ResultMap get(String key)
	{
		return buildMap.get(key);
	}

	public static void put(String key, ResultMap map)
	{
		buildMap.put(key, map);
	}

	public static boolean findEntity(String key)
	{
		return entityMap.containsKey(key);
	}

	public static void addEntity(String key, TableStruct struct)
	{
		entityMap.put(key, struct);
	}

	public static TableStruct getEntity(String key)
	{
		return entityMap.get(key);
	}

	public static int size() {
		return buildMap.size();
	}

}
