package com.xjcy.orm;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.ObjectUtils;
import com.xjcy.orm.core.SqlCache;
import com.xjcy.orm.event.Sql;
import com.xjcy.orm.event.SqlTranction;
import com.xjcy.orm.mapper.PageInfo;
import com.xjcy.orm.mapper.PageParamater;
import com.xjcy.orm.mapper.ProcParamater;
import com.xjcy.orm.mapper.ProcParamater.ParameterType;
import com.xjcy.orm.mapper.TableStruct;
import com.xjcy.orm.mapper.TableStruct.SQLType;

public abstract class AbstractSession {
	private static final Logger logger = Logger.getLogger(AbstractSession.class);

	protected DataSource ds;

	// 获取带事务的连接
	public SqlTranction beginTranction() throws Exception {
		return new DefaultTranctionImpl(ds.getConnection());
	}

	public <T> List<T> selectList(SqlTranction tran, Class<T> t, String sql, Object... objects) throws SQLException {
		return selectList(tran, t, Sql.parse(sql, objects));
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> selectList(SqlTranction tran, Class<T> t, Sql sql) throws SQLException {
		return (List<T>) doQuery(tran.Connection(), t, sql, Result.LIST);
	}

	/***
	 * 
	 * @param t
	 *            要查询的对象
	 * @param sql
	 *            要执行的sql
	 * @param autoMap
	 *            是否字段转换列名进行映射
	 * @return 查询的对象集合
	 */
	public <T> List<T> selectList(Class<T> t, String sql, Object... objects) {
		return selectList(t, Sql.parse(sql, objects));
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> selectList(Class<T> t, Sql sql) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return (List<T>) doQuery(conn, t, sql, Result.LIST);
		} catch (SQLException e) {
			logger.error("Get result faild => " + sql, e);
			return null;
		} finally {
			close(conn);
		}
	}

	protected abstract Object doQuery(Connection conn, Object o, Sql sql, Result result) throws SQLException;

	/***
	 * 
	 * @param t
	 *            要查询的对象
	 * @param sql
	 *            要执行的sql
	 * @param autoMap
	 *            是否字段转换列名进行映射
	 * @return 返回单个对象
	 * 
	 */
	public <T> T selectOne(Class<T> t, String sql, Object... objects) {
		return selectOne(t, Sql.parse(sql, objects));
	}

	public <T> T selectOne(Class<T> t, Sql sql) {
		List<T> dataList = selectList(t, sql);
		if (dataList != null && dataList.size() == 1) {
			return dataList.get(0);
		}
		return null;
	}

	public <T> T selectOne(SqlTranction tran, Class<T> t, String sql, Object... objects) throws SQLException {
		return selectOne(tran, t, Sql.parse(sql, objects));
	}

	public <T> T selectOne(SqlTranction tran, Class<T> t, Sql sql) throws SQLException {
		List<T> dataList = selectList(tran, t, sql);
		if (dataList != null) {
			if (dataList.size() == 1)
				return dataList.get(0);
			if (dataList.size() > 1)
				throw new SQLException("Too many results were found :" + dataList.size());
		}
		return null;
	}

	public <E> List<E> selectList(SqlTranction tran, String sql, Object... objects) throws SQLException {
		return selectList(tran, Sql.parse(sql, objects));
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> selectList(SqlTranction tran, Sql sql) throws SQLException {
		return (List<E>) doQuery(tran.Connection(), null, sql, Result.LIST);
	}

	/**
	 * 
	 * @param sql
	 * @param objects
	 *            parameters
	 * @return 查询的单列集合
	 */
	public <E> List<E> selectList(String sql, Object... objects) {
		return selectList(Sql.parse(sql, objects));
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> selectList(Sql sql) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return (List<E>) doQuery(conn, null, sql, Result.LIST);
		} catch (SQLException e) {
			logger.error("获取查询结果失败 =>" + sql, e);
			return null;
		} finally {
			close(conn);
		}
	}

	public <T> PageInfo<T> selectPage(Class<T> t, PageParamater paras, Object... objects) {
		PageInfo<T> page = new PageInfo<T>(paras.getPageNum(), paras.getPageSize());
		page.setResult(selectList(t, paras.getSelectSql(objects, page.getStartRow())));
		page.setTotal(Long.parseLong(getSingle(paras.getCountSql(objects)).toString()));
		return page;
	}

	/***
	 * 
	 * @param sql
	 *            要执行的sql
	 * @return 查询的键值对
	 */
	public <K, V> Map<K, V> selectMap(String sql, Object... objects) {
		return selectMap(Sql.parse(sql, objects));
	}

	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> selectMap(Sql sql) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return (Map<K, V>) doQuery(conn, null, sql, Result.MAP);
		} catch (SQLException e) {
			logger.error("Get map<k,v> faild =>" + sql, e);
			return null;
		} finally {
			close(conn);
		}
	}

	public <K, V> Map<K, V> selectMap(SqlTranction tran, String sql, Object... objects) throws SQLException {
		return selectMap(tran, Sql.parse(sql, objects));
	}

	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> selectMap(SqlTranction tran, Sql sql) throws SQLException {
		return (Map<K, V>) doQuery(tran.Connection(), null, sql, Result.MAP);
	}

	public Object getSingle(SqlTranction tran, String sql, Object... objects) throws SQLException {
		return getSingle(tran, Sql.parse(sql, objects));
	}

	public Object getSingle(SqlTranction tran, Sql sql) throws SQLException {
		return doQuery(tran.Connection(), null, sql, Result.SINGLE);
	}

	/***
	 * 
	 * @param sql
	 *            要执行的sql
	 * @return 返回单个值
	 */
	public Object getSingle(String sql, Object... objects) {
		return getSingle(Sql.parse(sql, objects));
	}

	public Object getSingle(Sql sql) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return doQuery(conn, null, sql, Result.SINGLE);
		} catch (SQLException e) {
			logger.error("Get single faild =>" + sql, e);
			return null;
		} finally {
			close(conn);
		}
	}

	public boolean execute(SqlTranction tran, String sql, Object... objects) throws SQLException {
		return execute(tran, Sql.parse(sql, objects));
	}

	public boolean execute(SqlTranction tran, Sql sql) throws SQLException {
		return doExecute(tran.Connection(), null, sql);
	}

	/***
	 * 
	 * @param sql
	 *            要执行的sql
	 * @param objects
	 *            执行sql语句对应的参数
	 * @return boolean
	 */
	public boolean execute(String sql, Object... objects) {
		return execute(Sql.parse(sql, objects));
	}

	public boolean execute(Sql sql) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return doExecute(conn, null, sql);
		} catch (SQLException e) {
			logger.error("Execute SQL faild => " + sql, e);
			return false;
		} finally {
			close(conn);
		}
	}

	protected abstract boolean doExecute(Connection conn, TableStruct struct, Object obj) throws SQLException;

	public boolean save(SqlTranction tran, Object obj) throws SQLException {
		if (obj instanceof List)
			throw new SQLException("不支持List对象的事务处理");
		TableStruct struct = getEntity(obj.getClass());
		struct.setColumns(obj, SQLType.INSERT);
		return doExecute(tran.Connection(), struct, obj);
	}

	public boolean save(Object obj) {
		if (obj instanceof List)
			return saveList((List<?>) obj);

		List<Object> objs = new ArrayList<>();
		objs.add(obj);
		return saveList(objs);
	}

	protected boolean saveList(List<?> objs) {
		SqlTranction tran = null;
		try {
			tran = beginTranction();
			for (Object obj : objs) {
				save(tran, obj);
			}
			tran.commit();
			return true;
		} catch (Exception e) {
			tran.rollback();
			logger.error("Save object faild", e);
			return false;
		} finally {
			close(tran);
		}
	}

	public boolean update(Object obj) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			TableStruct struct = getEntity(obj.getClass());
			struct.setColumns(obj, SQLType.UPDATE);
			return doExecute(conn, struct, obj);
		} catch (SQLException e) {
			logger.error("Update object faild", e);
			return false;
		} finally {
			close(conn);
		}
	}

	public boolean update(SqlTranction tran, Object obj) throws SQLException {
		TableStruct struct = getEntity(obj.getClass());
		struct.setColumns(obj, SQLType.UPDATE);
		return doExecute(tran.Connection(), struct, obj);
	}

	public boolean updateNotNull(Object obj) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			TableStruct struct = getEntity(obj.getClass());
			struct.setColumns(obj, SQLType.UPDATENOTNULL);
			return doExecute(conn, struct, obj);
		} catch (SQLException e) {
			logger.error("Update object faild", e);
			return false;
		} finally {
			close(conn);
		}
	}

	public boolean updateNotNull(SqlTranction tran, Object obj) throws SQLException {
		TableStruct struct = getEntity(obj.getClass());
		struct.setColumns(obj, SQLType.UPDATENOTNULL);
		return doExecute(tran.Connection(), struct, obj);
	}

	public boolean delete(Object obj) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			TableStruct struct = getEntity(obj.getClass());
			struct.setColumns(obj, SQLType.DELETE);
			return doExecute(conn, struct, obj);
		} catch (SQLException e) {
			logger.error("Delete object faild", e);
			return false;
		} finally {
			close(conn);
		}
	}

	public boolean delete(SqlTranction tran, Object obj) throws SQLException {
		TableStruct struct = getEntity(obj.getClass());
		struct.setColumns(obj, SQLType.DELETE);
		return doExecute(tran.Connection(), struct, obj);
	}

	public boolean saveOrUpdate(Object obj) {
		try {
			TableStruct struct = getEntity(obj.getClass());
			if (struct.hasPrimaryKey(obj))
				return updateNotNull(obj);
			else
				return save(obj);
		} catch (SQLException e) {
			logger.error("Get entity faild", e);
			return false;
		}
	}

	public boolean saveOrUpdate(SqlTranction tran, Object obj) throws SQLException {
		TableStruct struct = getEntity(obj.getClass());
		if (struct.hasPrimaryKey(obj))
			return updateNotNull(tran, obj);
		else
			return save(tran, obj);
	}

	/***
	 * 执行存储过程
	 * 
	 * @param sql
	 *            存储过程名
	 * @return 执行结果
	 */
	public boolean callProcdure(String sql) {
		return callProcdure(sql, null);
	}

	/***
	 * 执行存储过程
	 * 
	 * @param sql
	 *            存储过程名
	 * @param paras
	 *            存储过程参数
	 * @return 执行结果
	 */
	public boolean callProcdure(String sql, List<ProcParamater> paras) {
		long start = getNow();
		Connection conn = null;
		try {
			conn = ds.getConnection();
			CallableStatement cs = ObjectUtils.buildStatement(conn, sql);
			if (paras != null && paras.size() > 0) {
				for (ProcParamater para : paras) {
					switch (para.getParameterType()) {
					case IN:
						cs.setObject(para.getSort(), para.getValue());
						break;
					case OUT:
						cs.registerOutParameter(para.getSort(), Types.JAVA_OBJECT);
						break;
					default:
						cs.setObject(para.getSort(), para.getValue());
						cs.registerOutParameter(para.getSort(), Types.JAVA_OBJECT);
						break;
					}
				}
				// 执行存储过程
				cs.execute();
				for (ProcParamater para : paras) {
					if (para.getParameterType() == ParameterType.OUT
							|| para.getParameterType() == ParameterType.INOUT) {
						para.setValue(cs.getObject(para.getSort()));
					}
				}
			} else {
				cs.execute();
			}
			cs.close();
			if (logger.isDebugEnabled()) {
				logger.debug("Procdure => " + sql);
				logger.debug("Times => " + (getNow() - start) + "ns");
			}
			return true;
		} catch (SQLException e) {
			logger.error("Run procdure faild =>" + sql, e);
			return false;
		} finally {
			close(conn);
		}
	}

	private static void close(Connection conn) {
		try {
			if (conn == null || conn.isClosed())
				return;
			conn.close();
		} catch (SQLException e) {
			logger.error("关闭连接失败", e);
		}
	}

	public void close(SqlTranction tran) {
		try {
			Connection conn = tran.Connection();
			if (!conn.getAutoCommit()) {
				logger.debug("Set autocommit true on tranction close");
				conn.setAutoCommit(true);
			}
			close(conn);
		} catch (SQLException e) {
			logger.error("Tranction close faild", e);
		}
	}

	private static TableStruct getEntity(Class<?> cla) throws SQLException {
		TableStruct struct = SqlCache.getEntity(cla.getName());
		if (struct == null)
			throw new SQLException("No annotations for '" + cla.getName() + "' were found");
		return struct;
	}

	protected static long getNow() {
		return System.nanoTime();
	}

	protected enum Result {
		LIST, MAP, SINGLE
	}
}
