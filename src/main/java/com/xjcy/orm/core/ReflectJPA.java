package com.xjcy.orm.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xjcy.orm.jpa.Column;
import com.xjcy.orm.jpa.Id;
import com.xjcy.orm.jpa.Table;
import com.xjcy.orm.mapper.TableStruct;

public class ReflectJPA
{
	// 日志
	private static final Logger logger = Logger.getLogger(ReflectJPA.class);

	public static TableStruct loadTable(Class<?> cla)
	{
		// 仅获取自身的注解
		Table tableAnnotation = cla.getAnnotation(Table.class);
		if (tableAnnotation != null)
		{
			TableStruct table = new TableStruct(tableAnnotation.name());
			explanColumns(table, cla);
			return table;
		}
		return null;
	}

	private static void explanColumns(TableStruct table, Class<?> cla)
	{
		Map<String, Method> columns = new HashMap<>();
		List<String> primaryKeys = new ArrayList<>();
		try
		{
			Method[] methods = cla.getDeclaredMethods();
			String colName;
			for (Method method : methods)
			{
				colName = getColumnName(method);
				if (colName != null && !"".equals(colName))
				{
					columns.put(colName, method);
					Id id = method.getAnnotation(Id.class);
					if (id != null)
					{
						primaryKeys.add(colName);
						if (id.auto()) 
							table.setGenerateKey(colName);
					}
				}
			}
			table.setColumnMethods(columns);
			table.setPrimaryKeys(primaryKeys);
		}
		catch (Exception e)
		{
			logger.error("获取所有的表字段失败", e);
		}
	}

	private static String getColumnName(Method method)
	{
		if (method == null) return null;
		Column col = method.getAnnotation(Column.class);
		if (col != null) return col.name();
		return null;
	}
}
