package com.xjcy.orm.core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xjcy.util.STR;

public class JdbcUtils {

	public static void closeStatement(Statement stmt) {
		if (stmt != null)
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public static void closeConnection(Connection con) {
		if (con != null)
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public static void bindArgs(PreparedStatement stmt, Object[] objs) throws SQLException {
		if (objs != null && objs.length > 0) {
			for (int i = 0; i < objs.length; i++) {
				if (objs[i] instanceof List) {
					// Object[] arr = new Object[((List<?>) objs[i]).size()];
					// ((List<?>) objs[i]).toArray(arr);
					// stmt.setArray(i + 1,
					// stmt.getConnection().createArrayOf("VARCHAR", arr));
				} else {
					stmt.setObject(i + 1, objs[i]);
				}
			}
		}
	}

	public static String printArgs(Object[] objs) {
		if (objs == null || objs.length == 0)
			return STR.EMPTY;
		StringBuffer sb = new StringBuffer(" args:[");
		for (Object obj : objs) {
			sb.append(obj.toString());
			sb.append(",");
		}
		sb = sb.delete(sb.length() - 1, sb.length());
		sb.append("]");
		return sb.toString();
	}

	public static <T> T toBean(Class<T> target, Map<String, Field> mappings, ResultSet rs) throws SQLException {
		T t = null;
		try {
			t = target.newInstance();
			Set<String> labels = mappings.keySet();
			Object obj;
			for (String label : labels) {
				obj = rs.getObject(label);
				if (obj != null) {
					FieldUtils.setValue(mappings.get(label), t, obj);
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new SQLException("The object '" + target.getName() + "' cannot be initialized.");
		}
		return t;
	}

	public static Map<String, Field> buildFiledMappings(Class<?> target, ResultSetMetaData metaData)
			throws SQLException {
		Set<Field> fields = FieldUtils.getDeclaredFields(target);
		Map<String, Field> mappings = new HashMap<>(128);
		if (fields == null || fields.isEmpty()) {
			return mappings;
		}
		try {
			String fieldName, label, column;
			for (Field field : fields) {
				fieldName = field.getName();
				for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
					label = metaData.getColumnLabel(i);
					column = metaData.getColumnName(i);
					if (fieldName.equals(label) || fieldName.equals(column)
							|| fieldName.equals(FieldUtils.convert(label))
							|| fieldName.equals(FieldUtils.convert(column))) {
						mappings.put(label, field);
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new SQLException("获取对象和数据库的映射失败", e);
		}
		return mappings;
	}

}
