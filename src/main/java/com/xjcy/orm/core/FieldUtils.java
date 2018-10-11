package com.xjcy.orm.core;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xjcy.util.STR;

public class FieldUtils {
	private static final Pattern linePattern = Pattern.compile("_(\\w)");
	private static final Map<String, String> convertedNames = new HashMap<>(1024);
	private static final SimpleDateFormat sdf = new SimpleDateFormat(STR.DATE_LONG);

	public static Set<Field> getDeclaredFields(Class<?> t) {
		Set<Field> fieldList = new HashSet<>();
		getDeclaredFields(t, fieldList);
		return fieldList;
	}

	private static void getDeclaredFields(Class<?> cla, Set<Field> fieldList) {
		if (cla != null) {
			Field[] fields = cla.getDeclaredFields();
			for (Field field : fields) {
				fieldList.add(field);
			}
			getDeclaredFields(cla.getSuperclass(), fieldList);
		}
	}

	public synchronized static String convert(String str) {
		String name;
		if (convertedNames.containsKey(str))
			name = convertedNames.get(str);
		else {
			name = str.toLowerCase();
			Matcher matcher = linePattern.matcher(name);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
			}
			matcher.appendTail(sb);
			name = sb.toString();
			convertedNames.put(str, name);
		}
		return name;
	}

	public static Object toValue(String targetType, Object obj) throws SQLException {
		switch (targetType) {
		case "java.lang.String":
			if (obj instanceof Timestamp)
				return sdf.format(obj);
			return obj.toString();
		case "java.lang.Integer":
			return Integer.parseInt(obj.toString());
		case "java.lang.Long":
			return Long.parseLong(obj.toString());
		case "java.lang.Boolean":
			return Boolean.parseBoolean(obj.toString());
		default:
			throw new SQLException("This type '" + targetType + "' cannot be converted.");
		}
	}

	public static void setValue(Field field, Object obj, Object value) throws SQLException {
		if (!field.isAccessible())
			field.setAccessible(true);
		try {
			field.set(obj, toValue(field.getType().getName(), value));
		} catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
			throw new SQLException("Assignment failed in '" + field.getName() + "' field.", e);
		}
	}

	static final List<String> PRIMITIVE_TYPE = Arrays.asList("java.lang.String", "java.lang.Long");

	public static boolean isPrimitive(Class<?> target) {
		if (target.isPrimitive())
			return true;
		if (PRIMITIVE_TYPE.contains(target.getName()))
			return true;
		return false;
	}
}
