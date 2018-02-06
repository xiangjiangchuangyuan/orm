package com.xjcy.orm.core;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.xjcy.orm.jpa.DateFormat;

public class FieldUtils
{
	private static final Logger logger = Logger.getLogger(FieldUtils.class);

	public static Set<Field> getDeclaredFields(Class<?> t)
	{
		Set<Field> fieldList = new HashSet<>();
		getDeclaredFields(t, fieldList);
		return fieldList;
	}

	private static void getDeclaredFields(Class<?> cla, Set<Field> fieldList)
	{
		if (cla != null)
		{
			Field[] fields = cla.getDeclaredFields();
			for (Field field : fields)
			{
				fieldList.add(field);
			}
			getDeclaredFields(cla.getSuperclass(), fieldList);
		}
	}

	public static String ConvertName(String column)
	{
		column = column.toLowerCase();
		String[] strs = column.split("_");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strs.length; i++)
		{
			if (i == 0)
			{
				sb.append(strs[i]);
			}
			else
			{
				char[] c = strs[i].toCharArray();
				for (int j = 0; j < c.length; j++)
				{
					if (j == 0)
					{
						sb.append((c[j] + "").toUpperCase());
					}
					else
					{
						sb.append(c[j]);
					}
				}
			}
		}
		return sb.toString();
	}

	public static Object ConvertValue(Field field, Class<?> t, Object obj) throws IntrospectionException
	{
		Type genericType = field.getGenericType();
		if (obj != null)
		{
			if(field.getType().isEnum())
			{
				Object[] objs = field.getType().getEnumConstants();
				if(obj instanceof Integer)
					return objs[Integer.parseInt(obj.toString())];
				for (Object e : objs)
				{
					if(e.toString().equals(obj.toString()))
						return e;
				}  
			}
			else if (genericType == String.class)
			{
				if (obj instanceof Timestamp)
				{
					String format = "yyyy-MM-dd HH:mm:ss";
					PropertyDescriptor pd = new PropertyDescriptor(field.getName(), t);
					Method getMethod = pd.getReadMethod();// 获得get方法
					if (pd != null)
					{
						DateFormat df = getMethod.getAnnotation(DateFormat.class);
						if (df != null)
							format = df.pattern();
					}
					return getDateFormat((Date) obj, format);
				}
				return obj.toString();
			}
			if (!"".equals(obj.toString()))
			{
				if (genericType == Date.class)
					return (Date) obj;
				if (genericType == Integer.class)
					return Integer.parseInt(obj.toString());
				if (genericType == Double.class)
					return Double.parseDouble(obj.toString());
				if (genericType == Long.class)
					return Long.parseLong(obj.toString());
				if (genericType == Float.class)
					return Float.parseFloat(obj.toString());
				if (genericType == char.class)
					return obj.toString().charAt(0);
				if (genericType == Boolean.class)
					return Boolean.parseBoolean(obj.toString());
			}
			return obj;
		}
		return null;
	}

	private static String getDateFormat(Date date, String format)
	{
		return new SimpleDateFormat(format).format(date);
	}

	public static Object getValue(Object arg0, Method method)
	{
		Object obj = null;
		try
		{
			obj = method.invoke(arg0);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			logger.error("获取entity的值失败", e);
		}
		if (obj != null && !"".equals(obj) && !"null".equals(obj))
		{
			return obj;
		}
		return obj;
	}

	public static Object getValue(Object obj, String field)
	{
		Object result = null;
		try
		{
			Field f = obj.getClass().getDeclaredField(ConvertName(field));
			f.setAccessible(true);
			result = f.get(obj);
			f.setAccessible(false);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			logger.error("获取主键值失败", e);
		}
		return result;
	}

	public static void setValue(Object obj, String fieldName, Object result)
	{
		try
		{
			fieldName = ConvertName(fieldName);
			Field field = obj.getClass().getDeclaredField(fieldName);
			if (field != null)
			{
				field.setAccessible(true);
				if(field.getGenericType() == Integer.class)
					field.set(obj, Integer.parseInt(result.toString()));
				else
					field.set(obj, result);
				field.setAccessible(false);
			}
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			logger.error("PrimaryKey赋值失败", e);
		}
	}
}
