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
import com.xjcy.util.StringUtils;
import com.xjcy.orm.mapper.TableStruct;

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

	public <T> List<T> selectList(SqlTranction tran, Class<T> t, Sql sql) throws SQLException {
		return buildQuery(tran.Connection(), t, sql);
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

	/**
	 * 查询列表 判断参数是否为null
	 * 
	 * @param t
	 * @param sql
	 * @param objects
	 *            示例："name=?","张三","age like ?",null
	 * @return
	 */
	public <T> List<T> selectListEx(Class<T> t, String sql, Object... objects) {
		String key = null;
		StringBuilder builder = new StringBuilder(sql);
		List<Object> objs = new ArrayList<>();
		for (int i = 0; i < objects.length; i++) {
			if (i % 2 == 0) {
				key = (String) objects[i];
			} else {
				if (objects[i] != null && StringUtils.isNotBlank(objects[i].toString())) {
					// 判断sql中是否有where关键字
					if (builder.indexOf(" WHERE ") > -1)
						builder.append(" AND ").append(key);
					else
						builder.append(" WHERE ").append(key);
					objs.add(objects[i]);
				}
			}
		}
		return selectList(t, Sql.parse(builder.toString(), objs.toArray()));
	}

	public <T> List<T> selectList(Class<T> t, Sql sql) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return buildQuery(conn, t, sql);
		} catch (SQLException e) {
			logger.error("Get result faild => " + sql, e);
			return null;
		} finally {
			close(conn);
		}
	}

	protected abstract <T> List<T> buildQuery(Connection conn, Class<T> t, Sql sql) throws SQLException;

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

	public <E> List<E> selectList(SqlTranction tran, Sql sql) throws SQLException {
		return buildQueryList(tran.Connection(), sql);
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

	public <E> List<E> selectList(Sql sql) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return buildQueryList(conn, sql);
		} catch (SQLException e) {
			logger.error("获取查询结果失败 =>" + sql, e);
			return null;
		} finally {
			close(conn);
		}
	}

	protected abstract <E> List<E> buildQueryList(Connection conn, Sql sql) throws SQLException;

	public <T> PageInfo<T> selectPage(Class<T> t, PageParamater paras, Object... objects) {
		int pageSize = paras.getPageSize();
		PageInfo<T> page = new PageInfo<T>(paras.getPageNum(), pageSize);
		if (ObjectUtils.isEmpty(objects)) {
			page.setResult(selectList(t, paras.getSelectSql() + " LIMIT ?,?", page.getStartRow(), pageSize));
			page.setTotal(Long.parseLong(getSingle(paras.getCountSql()).toString()));
		} else {
			Object[] newObj = new Object[objects.length + 2];
			System.arraycopy(objects, 0, newObj, 0, objects.length);
			// 赋值LIMIT参数
			newObj[objects.length] = page.getStartRow();
			newObj[objects.length + 1] = pageSize;
			page.setResult(selectList(t, paras.getSelectSql() + " LIMIT ?,?", newObj));
			page.setTotal(Long.parseLong(getSingle(paras.getCountSql(), objects).toString()));
		}
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

	public <K, V> Map<K, V> selectMap(Sql sql) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return buildQueryMap(conn, sql);
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

	public <K, V> Map<K, V> selectMap(SqlTranction tran, Sql sql) throws SQLException {
		return buildQueryMap(tran.Connection(), sql);
	}

	protected abstract <K, V> Map<K, V> buildQueryMap(Connection conn, Sql sql) throws SQLException;

	public Object getSingle(SqlTranction tran, String sql, Object... objects) throws SQLException {
		return getSingle(tran, Sql.parse(sql, objects));
	}

	public Object getSingle(SqlTranction tran, Sql sql) throws SQLException {
		return buildGetSingle(tran.Connection(), sql);
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
			return buildGetSingle(conn, sql);
		} catch (SQLException e) {
			logger.error("Get single faild =>" + sql, e);
			return null;
		} finally {
			close(conn);
		}
	}

	protected abstract Object buildGetSingle(Connection conn, Sql sql) throws SQLException;

	public boolean execute(SqlTranction tran, String sql, Object... objects) throws SQLException {
		return execute(tran, Sql.parse(sql, objects));
	}

	public boolean execute(SqlTranction tran, Sql sql) throws SQLException {
		return buildExecute(tran.Connection(), sql);
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
			return buildExecute(conn, sql);
		} catch (SQLException e) {
			logger.error("Execute SQL faild => " + sql, e);
			return false;
		} finally {
			close(conn);
		}
	}

	protected abstract boolean buildExecute(Connection conn, Sql sql) throws SQLException;

	public boolean save(SqlTranction tran, Object obj) throws SQLException {
		if (obj instanceof List)
			throw new SQLException("不支持List对象的事务处理");
		TableStruct struct = getEntity(obj.getClass());
		return buildSave(tran.Connection(), struct, obj);
	}

	protected abstract boolean buildSave(Connection conn, TableStruct struct, Object obj) throws SQLException;

	public boolean save(Object obj) {
		if (obj instanceof List)
			return saveList((List<?>) obj);

		List<Object> objs = new ArrayList<>();
		objs.add(obj);
		return saveList(objs);
	}

	protected boolean saveList(List<?> objs) {
		long start = getNow();
		Connection conn = null;
		try {
			conn = ds.getConnection();
			int num = 0;
			for (Object obj : objs) {
				buildSave(conn, getEntity(obj.getClass()), obj);
				num++;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Finish, total => " + objs.size() + ", saved => " + num + ", Times => "
						+ (getNow() - start) + "ns");
			}
			return true;
		} catch (SQLException e) {
			logger.error("Save object faild", e);
			return false;
		} finally {
			close(conn);
		}
	}

	public boolean update(Object obj) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return buildUpdate(conn, getEntity(obj.getClass()), obj, false);
		} catch (SQLException e) {
			logger.error("Update object faild", e);
			return false;
		} finally {
			close(conn);
		}
	}

	public boolean update(SqlTranction tran, Object obj) throws SQLException {
		return buildUpdate(tran.Connection(), getEntity(obj.getClass()), obj, false);
	}

	public boolean updateNotNull(Object obj) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return buildUpdate(conn, getEntity(obj.getClass()), obj, true);
		} catch (SQLException e) {
			logger.error("Update object faild", e);
			return false;
		} finally {
			close(conn);
		}
	}

	public boolean updateNotNull(SqlTranction tran, Object obj) throws SQLException {
		return buildUpdate(tran.Connection(), getEntity(obj.getClass()), obj, true);
	}

	protected abstract boolean buildUpdate(Connection conn, TableStruct struct, Object obj, boolean notNull)
			throws SQLException;

	public boolean delete(Object obj) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return buildDelete(conn, getEntity(obj.getClass()), obj);
		} catch (SQLException e) {
			logger.error("Delete object faild", e);
			return false;
		} finally {
			close(conn);
		}
	}

	public boolean delete(SqlTranction tran, Object obj) throws SQLException {
		return buildDelete(tran.Connection(), getEntity(obj.getClass()), obj);
	}

	protected abstract boolean buildDelete(Connection conn, TableStruct struct, Object obj) throws SQLException;

	public boolean saveOrUpdate(Object obj) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			return buildSaveOrUpdate(conn, getEntity(obj.getClass()), obj);
		} catch (SQLException e) {
			logger.error("Delete object faild", e);
			return false;
		} finally {
			close(conn);
		}
	}

	public boolean saveOrUpdate(SqlTranction tran, Object obj) throws SQLException {
		return buildSaveOrUpdate(tran.Connection(), getEntity(obj.getClass()), obj);
	}

	protected abstract boolean buildSaveOrUpdate(Connection conn, TableStruct struct, Object obj) throws SQLException;

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
}
