package com.xjcy.orm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.xjcy.orm.core.FieldUtils;
import com.xjcy.orm.core.JdbcUtils;
import com.xjcy.orm.core.Selector;
import com.xjcy.orm.mapper.ResultMapper;
import com.xjcy.util.LoggerUtils;
import com.xjcy.util.MD5;

public abstract class SqlSessionBase {
	private static final LoggerUtils logger = LoggerUtils.from(SqlSessionBase.class);
	private final DataSource ds;
	private final Map<String, Map<String, Field>> resultMappings = new HashMap<>();

	public SqlSessionBase(DataSource dataSource) {
		this.ds = dataSource;
	}

	public SqlTranction beginTranction() throws Exception {
		return new SqlTranction(ds.getConnection());
	}

	public void close(SqlTranction tran) {
		tran.close();
	}

	protected <T> T execute(ResultMapper<T> mapper) {
		try {
			return execute(mapper, ds.getConnection(), true);
		} catch (SQLException e) {
			logger.error("Execute faild", e);
			return null;
		}
	}

	protected <T> T execute(SqlTranction tran, ResultMapper<T> mapper) throws SQLException {
		return execute(mapper, tran.Connection(), false);
	}

	protected <T> T execute(ResultMapper<T> mapper, Connection con, boolean close) throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("Run:[" + mapper.getSql() + "]" + JdbcUtils.printArgs(mapper.getArgs()));
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(mapper.getSql());
			applySettings(stmt);
			T result = mapper.doStatement(stmt);
			handleWarnings(stmt);
			return result;
		} catch (SQLException ex) {
			throw new SQLException("StatementCallback", mapper.getSql(), ex);
		} finally {
			mapper.clean();
			JdbcUtils.closeStatement(stmt);
			if (close) {
				JdbcUtils.closeConnection(con);
			}
		}
	}

	private <T> Map<String, Field> buildFiledMappings(Class<T> target, String sql, ResultSetMetaData metaData)
			throws SQLException {
		String key = MD5.encodeByMD5(target.getName() + "_" + sql);
		if(resultMappings.containsKey(key))
			return resultMappings.get(key);
		Map<String, Field> mapping = JdbcUtils.buildFiledMappings(target, metaData);
		resultMappings.put(key, mapping);
		return mapping;
	}

	private void applySettings(PreparedStatement stmt) throws SQLException {
		stmt.setQueryTimeout(5);
	}

	private void handleWarnings(PreparedStatement stmt) throws SQLException {
		if (logger.isDebugEnabled()) {
			SQLWarning warningToLog = stmt.getWarnings();
			while (warningToLog != null) {
				logger.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState() + "', error code '"
						+ warningToLog.getErrorCode() + "', message [" + warningToLog.getMessage() + "]");
				warningToLog = warningToLog.getNextWarning();
			}
		}
	}

	protected final class defaultResultMapper implements ResultMapper<Integer> {

		private String sql;
		private Object[] objs;

		public defaultResultMapper(Selector selector) {
			this.sql = selector.toString();
			this.objs = selector.array();
		}

		public defaultResultMapper(String sql, Object[] array) {
			this.sql = sql;
			this.objs = array;
		}

		@Override
		public Integer doStatement(PreparedStatement stmt) throws SQLException {
			JdbcUtils.bindArgs(stmt, objs);
			return stmt.executeUpdate();
		}

		@Override
		public String getSql() {
			return sql;
		}

		@Override
		public Object[] getArgs() {
			return objs;
		}

		@Override
		public void clean() {
			sql = null;
			objs = null;
		}

	}

	protected final class queryResultMapper<T> implements ResultMapper<List<T>> {

		private Object[] objs;
		private String sql;
		private Class<T> target;

		public queryResultMapper(Class<T> target, Selector selector) {
			this.sql = selector.toString();
			this.objs = selector.array();
			this.target = target;
		}

		public queryResultMapper(Class<T> target, String sql, Object[] array) {
			this.sql = sql;
			this.objs = array;
			this.target = target;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<T> doStatement(PreparedStatement stmt) throws SQLException {
			JdbcUtils.bindArgs(stmt, objs);
			List<T> dataList = new ArrayList<>();
			ResultSet rs = stmt.executeQuery();
			Map<String, Field> mappings = null;
			T t;
			while (rs.next()) {
				if (FieldUtils.isPrimitive(target))
					t = (T) FieldUtils.toValue(target.getName(), rs.getObject(1));
				else {
					if (mappings == null)
						mappings = buildFiledMappings(target, sql, rs.getMetaData());
					t = JdbcUtils.toBean(target, mappings, rs);
				}
				dataList.add(t);
			}
			rs.close();
			return dataList;
		}

		@Override
		public String getSql() {
			return sql;
		}

		@Override
		public Object[] getArgs() {
			return objs;
		}

		@Override
		public void clean() {
			sql = null;
			objs = null;
			target = null;
		}

	}

	protected final class singleResultMapper<T> implements ResultMapper<T> {

		private Object[] objs;
		private Class<T> target;
		private String sql;

		public singleResultMapper(Class<T> target, Selector selector) {
			this.objs = selector.array();
			this.target = target;
			this.sql = selector.toString();
		}

		public singleResultMapper(Class<T> target, String sql, Object[] array) {
			this.objs = array;
			this.target = target;
			this.sql = sql;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T doStatement(PreparedStatement stmt) throws SQLException {
			JdbcUtils.bindArgs(stmt, objs);
			ResultSet rs = stmt.executeQuery();
			T t = null;
			while (rs.next()) {
				if (FieldUtils.isPrimitive(target))
					t = (T) FieldUtils.toValue(target.getName(), rs.getObject(1));
				else {
					Map<String, Field> mappings = buildFiledMappings(target, sql, rs.getMetaData());
					t = JdbcUtils.toBean(target, mappings, rs);
				}
				break;
			}
			rs.close();
			return t;
		}

		@Override
		public String getSql() {
			return sql;
		}

		@Override
		public Object[] getArgs() {
			return objs;
		}

		@Override
		public void clean() {
			sql = null;
			objs = null;
			target = null;
		}

	}
}
