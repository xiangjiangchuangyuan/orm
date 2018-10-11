package com.xjcy.orm.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.xjcy.orm.jpa.Column;
import com.xjcy.orm.jpa.Id;
import com.xjcy.orm.jpa.Table;
import com.xjcy.util.LoggerUtils;

public class ScriptUtils {
	static final LoggerUtils logger = LoggerUtils.from(ScriptUtils.class);

	public static Entry<String, Object[]> buildInsert(Object data) {
		if (data == null)
			return null;
		Table table = data.getClass().getAnnotation(Table.class);
		if (table == null)
			return null;
		Set<Field> fields = FieldUtils.getDeclaredFields(data.getClass());
		Map<String, Object> columns = getColumns(data, fields);
		if(columns.isEmpty())
			return null;

		Map<String, Object[]> map = new HashMap<>();
		try {
			long start = System.nanoTime();
			List<Object> args = new ArrayList<>();
			StringBuffer sql1 = new StringBuffer("(");
			StringBuffer sql2 = new StringBuffer("(");
			Object obj = null;
			Set<String> keys = columns.keySet();
			for (String key : keys) {
				obj = columns.get(key);
				if (obj != null) {
					sql1.append("`" + key + "`,");
					sql2.append("?,");
					args.add(obj);
				}
			}
			sql1.deleteCharAt(sql1.length() - 1);
			sql1.append(")");
			sql2.deleteCharAt(sql2.length() - 1);
			sql2.append(")");
			String sql = "insert into " + table.value() + sql1.toString() + " values " + sql2.toString();
			if (logger.isDebugEnabled())
				logger.debug("Insert(" + (System.nanoTime() - start) + "ns) => " + sql);
			Object[] temp = new Object[args.size()];
			map.put(sql, args.toArray(temp));
		} catch (Exception e) {
			logger.error("构造Insert语句失败", e);
			return null;
		}
		return map.entrySet().iterator().next();
	}

	private static Map<String, Object> getColumns(Object data, Set<Field> fields) {
		Map<String, Object> columns = new HashMap<>();
		Column col;
		for (Field field : fields) {
			col = field.getAnnotation(Column.class);
			if (col != null) {
				if(!field.isAccessible())
					field.setAccessible(true);
				try {
					columns.put(col.value(), field.get(data));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return columns;
	}

	public static Entry<String, Object[]> buildUpdate(Object data) {
		if (data == null)
			return null;
		Table table = data.getClass().getAnnotation(Table.class);
		if (table == null)
			return null;
		Set<Field> fields = FieldUtils.getDeclaredFields(data.getClass());
		Map<String, Object> columns = getColumns(data, fields);
		if(columns.isEmpty())
			return null;
		List<String> primaryKeys = getPrimaryKeys(data, fields);
		if(primaryKeys.size() == 0)
			return null;
		
		Map<String, Object[]> map = new HashMap<>();
		try {
			long start = System.nanoTime();
			List<Object> args = new ArrayList<>();
			StringBuffer sql1 = new StringBuffer(" set ");
			Object obj = null;
			Set<String> keys = columns.keySet();
			for (String key : keys) {
				obj = columns.get(key);
				if ( obj != null && !primaryKeys.contains(key)) {
					sql1.append("`" + key + "`=?,");
					args.add(obj);
				}
			}
			sql1.deleteCharAt(sql1.length() - 1);
			StringBuffer sql2 = new StringBuffer();
			if (!primaryKeys.isEmpty()) {
				for (String key : primaryKeys) {
					sql2.append("`" + key + "`=? AND ");
					args.add(columns.get(key));
				}
			}
			if (sql2.length() > 0)
				sql2.delete(sql2.length() - 4, sql2.length());
			String sql = "update " + table.value() + sql1.toString() + " where " + sql2.toString();
			if (logger.isDebugEnabled())
				logger.debug("Update(" + (System.nanoTime() - start) + "ns) => " + sql);
			Object[] temp = new Object[args.size()];
			map.put(sql, args.toArray(temp));
		} catch (Exception e) {
			logger.error("构造UPDATE语句失败", e);
			return null;
		}
		return map.entrySet().iterator().next();
	}

	private static List<String> getPrimaryKeys(Object data, Set<Field> fields) {
		List<String> columns = new ArrayList<>();
		Id id;
		for (Field field : fields) {
			id = field.getAnnotation(Id.class);
			if (id != null) {
				columns.add(field.getAnnotation(Column.class).value());
			}
		}
		return columns;
	}

}
