package com.xjcy.orm.core;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.xjcy.orm.event.Sql;
import com.xjcy.orm.mapper.ResultMap;
import com.xjcy.orm.mapper.TableStruct;
import com.xjcy.orm.mapper.TableStruct.SQLType;

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
		if (struct.hasGenerageKey() && struct.getSqlType() == SQLType.INSERT)
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		else
			ps = conn.prepareStatement(sql);
		Set<Integer> keys = sqlMap.keySet();
		for (Integer index : keys) {
			ps.setObject(index, sqlMap.get(index));
		}
		return ps;
	}

	public static ResultSet buildResultSet(Connection conn, Sql sql) throws SQLException {
		if (sql.noData()) {
			Statement st = conn.createStatement();
			st.setQueryTimeout(QUERY_TIMEOUT);
			return st.executeQuery(sql.getSql());
		}
		Object[] objects = sql.getData();
		PreparedStatement ps = conn.prepareStatement(sql.getSql());
		for (int i = 1; i < objects.length + 1; i++) {
			ps.setObject(i, objects[i - 1]);
		}
		ps.setQueryTimeout(QUERY_TIMEOUT);
		return ps.executeQuery();
	}

	public static Object copyValue(ResultMap map, ResultSet rs, Class<?> t) {
		try {
			Object tt = t.newInstance();
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

	public static int executeUpdate(Connection conn, Sql sql) throws SQLException {
		int num;
		if (sql.noData()) {
			Statement st = conn.createStatement();
			st.setQueryTimeout(QUERY_TIMEOUT);
			num = st.executeUpdate(sql.getSql());
			st.close();
			return num;
		}
		Object[] objects = sql.getData();
		PreparedStatement ps = conn.prepareStatement(sql.getSql());
		for (int i = 1; i < objects.length + 1; i++) {
			ps.setObject(i, objects[i - 1]);
		}
		ps.setQueryTimeout(QUERY_TIMEOUT);
		num = ps.executeUpdate();
		ps.close();
		return num;
	}
}
