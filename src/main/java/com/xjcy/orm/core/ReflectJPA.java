package com.xjcy.orm.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
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
			table.setGenerateKey(getSpecialColumn(cla, Id.class));
			table.setColumnMethods(getColumnMethods(cla));
			return table;
		}
		return null;
	}

	static Map<String, Method> getColumnMethods(Class<?> cla)
	{
		Map<String, Method> columns = new HashMap<>();
		try
		{
			Method[] methods = cla.getDeclaredMethods();
			String colName;
			for (Method method : methods)
			{
				colName = getColumnName(method);
				if (colName != null && !"".equals(colName))
					columns.put(colName, method);
			}
		}
		catch (Exception e)
		{
			logger.error("获取所有的表字段失败", e);
		}
		return columns;
	}

	private static <T extends Annotation> String getSpecialColumn(Class<?> cla, Class<T> annotationClass)
	{
		Method[] methods = cla.getDeclaredMethods();
		String colName = null;
		T t;
		for (Method method : methods)
		{
			t = method.getAnnotation(annotationClass);
			if (t != null)
			{
				colName = getColumnName(method);
				break;
			}
		}
		return colName;
	}

	private static String getColumnName(Method method)
	{
		if (method == null)
			return null;
		Column col = method.getAnnotation(Column.class);
		if (col != null)
			return col.name();
		return null;
	}
}
