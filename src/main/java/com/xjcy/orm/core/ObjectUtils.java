package com.xjcy.orm.core;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.xjcy.orm.event.Sql;
import com.xjcy.orm.mapper.ResultMap;
import com.xjcy.orm.mapper.TableStruct;

public class ObjectUtils {
	private static final Logger logger = Logger.getLogger(ObjectUtils.class);

	private static final Object LOCK_OBJ = new Object();
	private static final Integer QUERY_TIMEOUT = 5;

	public static CallableStatement buildStatement(Connection conn, String sql) throws SQLException {
		return conn.prepareCall(sql);
	}

	public static PreparedStatement buildStatement(Connection conn, TableStruct struct) throws SQLException {
		PreparedStatement ps;
		Map<Integer, Object> sqlMap = struct.getSqlMap();
		String sql = sqlMap.remove(0).toString();
		if (struct.hasGenerageKey())
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		else
			ps = conn.prepareStatement(sql);
		Set<Entry<Integer, Object>> entries = sqlMap.entrySet();
		for (Entry<Integer, Object> entry : entries) {
			ps.setObject(entry.getKey(), entry.getValue());
		}
		return ps;
	}

	public static ResultSet buildResultSet(Connection conn, Sql sql) throws SQLException {
		if (sql.noData()) {
			Statement st = conn.createStatement();
			st.setQueryTimeout(QUERY_TIMEOUT);
			return st.executeQuery(sql.toString());
		}
		Object[] objects = sql.getData();
		PreparedStatement ps = conn.prepareStatement(sql.toString());
		for (int i = 1; i < objects.length + 1; i++) {
			ps.setObject(i, objects[i - 1]);
		}
		ps.setQueryTimeout(QUERY_TIMEOUT);
		return ps.executeQuery();
	}

	public static ResultMap buildColumnMap(Class<?> t, ResultSetMetaData metaData) {
		Set<Field> fields = FieldUtils.getDeclaredFields(t);
		ResultMap map = new ResultMap();
		if (fields == null || fields.isEmpty()) {
			return map;
		}
		if (logger.isDebugEnabled())
			logger.debug("Build object map with metadata");
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

	public static <T> T copyValue(ResultMap map, ResultSet rs, Class<T> t) {
		try {
			T tt = t.newInstance();
			Set<String> keys = map.Keys();
			Object obj;
			Field field;
			for (String label : keys) {
				obj = rs.getObject(label);
				if (obj != null) {
					field = map.get(label);
					synchronized (LOCK_OBJ) {
						field.setAccessible(true);
						field.set(tt, FieldUtils.ConvertValue(field, t, obj));
						field.setAccessible(false);
					}
				}
			}
			return tt;
		} catch (Exception e) {
			logger.error("数据库对象转换失败,传入对象 => " + t.getName(), e);
		}
		return null;
	}

	public static Integer executeUpdate(Connection conn, Sql sql) throws SQLException {
		int num;
		if (sql.noData()) {
			Statement st = conn.createStatement();
			st.setQueryTimeout(QUERY_TIMEOUT);
			num = st.executeUpdate(sql.toString());
			st.close();
			return num;
		}
		Object[] objects = sql.getData();
		PreparedStatement ps = conn.prepareStatement(sql.toString());
		for (int i = 1; i < objects.length + 1; i++) {
			ps.setObject(i, objects[i - 1]);
		}
		ps.setQueryTimeout(QUERY_TIMEOUT);
		num = ps.executeUpdate();
		ps.close();
		return num;
	}
}
