package com.xjcy.orm.core;

import java.lang.reflect.Field;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.xjcy.orm.mapper.ResultMap;
import com.xjcy.orm.mapper.TableStruct;

public class SqlCache
{
	private static final Logger logger = Logger.getLogger(SqlCache.class);
	
	private static final Map<String, ResultMap> buildMap = new ConcurrentHashMap<>();
	private static final Map<String, TableStruct> entityMap = new ConcurrentHashMap<>();

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

	public synchronized static ResultMap get(Class<?> o, String sql, ResultSetMetaData metaData) {
		String key = o.getName() + sql;
		if (buildMap.containsKey(key))
			return buildMap.get(key);
		ResultMap map = buildColumnMap(o, metaData);
		buildMap.put(key, map);
		return map;
	}

	private static ResultMap buildColumnMap(Class<?> t, ResultSetMetaData metaData) {
		Set<Field> fields = FieldUtils.getDeclaredFields(t);
		ResultMap map = new ResultMap();
		if (fields == null || fields.isEmpty()) {
			return map;
		}
		try {
			String fieldName, label, column;
			for (Field field : fields) {
				fieldName = field.getName();
				for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
					label = metaData.getColumnLabel(i);
					column = metaData.getColumnName(i);
					if (fieldName.equals(label) || fieldName.equals(column)
							|| fieldName.equals(FieldUtils.ConvertName(label))
							|| fieldName.equals(FieldUtils.ConvertName(column))) {
						map.put(label, field);
						break;
					}
				}
			}
		} catch (SQLException e) {
			logger.error("获取对象和数据库的映射失败", e);
		}
		return map;
	}
}
