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
import com.xjcy.orm.event.SqlTranction;
import com.xjcy.orm.mapper.PageInfo;
import com.xjcy.orm.mapper.PageParamater;
import com.xjcy.orm.mapper.ProcParamater;
import com.xjcy.orm.mapper.ProcParamater.ParameterType;
import com.xjcy.orm.mapper.TableStruct;

public abstract class AbstractSession
{
	private static final Logger logger = Logger.getLogger(AbstractSession.class);

	protected DataSource ds;
	static final String NULL = "null";

	// 获取带事务的连接
	public SqlTranction beginTranction() throws SQLException
	{
		Connection conn = ds.getConnection();
		conn.setAutoCommit(false);
		return new DefaultTranctionImpl(conn);
	}

	public <T> List<T> Query(SqlTranction tran, Class<T> t) throws SQLException
	{
		TableStruct struct = getEntity(t);
		String sql = "select * from " + struct.getTableName();
		return Query(tran, t, sql, NULL);
	}

	public <T> List<T> Query(SqlTranction tran, Class<T> t, String sql) throws SQLException
	{
		return Query(tran, t, sql, NULL);
	}

	public <T> List<T> Query(SqlTranction tran, Class<T> t, String sql, Object... objects) throws SQLException
	{
		return buildQuery(t, sql, tran.Connection(), objects);
	}

	public <T> List<T> Query(Class<T> t) throws SQLException
	{
		TableStruct struct = getEntity(t);
		return Query(t, "select * from " + struct.getTableName(), NULL);
	}

	/***
	 * 
	 * @param t
	 *            要查询的对象
	 * @param sql
	 *            要执行的sql
	 * @return Model Collection
	 */
	public <T> List<T> Query(Class<T> t, String sql)
	{
		return Query(t, sql, NULL);
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
	public <T> List<T> Query(Class<T> t, String sql, Object... objects)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildQuery(t, sql, conn, objects);
		}
		catch (SQLException e)
		{
			logger.error("Get result faild => " + sql, e);
			return null;
		}
		finally
		{
			close(conn);
		}
	}

	protected abstract <T> List<T> buildQuery(Class<T> t, String sql, Connection conn, Object... objects)
			throws SQLException;

	public <T> T Single(Class<T> t, String sql)
	{
		return Single(t, sql, NULL);
	}

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
	public <T> T Single(Class<T> t, String sql, Object... objects)
	{
		List<T> dataList = Query(t, sql, objects);
		if (dataList != null && dataList.size() == 1)
		{
			return dataList.get(0);
		}
		return null;
	}

	public <T> T Single(SqlTranction tran, Class<T> t, String sql) throws SQLException
	{
		return Single(tran, t, sql, NULL);
	}

	public <T> T Single(SqlTranction tran, Class<T> t, String sql, Object... objects) throws SQLException
	{
		List<T> dataList = Query(tran, t, sql, objects);
		if (dataList != null)
		{
			if (dataList.size() == 1)
				return dataList.get(0);
			if (dataList.size() > 1)
				throw new SQLException("Too many results were found :" + dataList.size());
		}
		return null;
	}

	public <T> List<T> QueryList(SqlTranction tran, String sql) throws SQLException
	{
		return QueryList(tran, sql, NULL);
	}

	public <T> List<T> QueryList(SqlTranction tran, String sql, Object... objects) throws SQLException
	{
		return buildQueryList(sql, tran.Connection(), objects);
	}

	/***
	 * 
	 * @param sql
	 *            查询的SQL
	 * @return 查询的对象集合
	 */
	public <T> List<T> QueryList(String sql)
	{
		return QueryList(sql, NULL);
	}

	/**
	 * 
	 * @param sql
	 * @param objects
	 *            parameters
	 * @return 查询的单列集合
	 */
	public <T> List<T> QueryList(String sql, Object... objects)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildQueryList(sql, conn, objects);
		}
		catch (SQLException e)
		{
			logger.error("获取查询结果失败 =>" + sql, e);
			return null;
		}
		finally
		{
			close(conn);
		}
	}

	protected abstract <T> List<T> buildQueryList(String sql, Connection conn, Object... objects) throws SQLException;

	public <T> PageInfo<T> Query(Class<T> t, PageParamater paras)
	{
		return Query(t, paras, NULL);
	}
	
	public <T> PageInfo<T> Query(Class<T> t, PageParamater paras, Object... objects)
	{
		int pageSize = paras.getPageSize();
		PageInfo<T> page = new PageInfo<T>(paras.getPageNum(), pageSize);
		if (ObjectUtils.isEmpty(objects))
		{
			page.setResult(Query(t, paras.getSelectSql() + " LIMIT ?,?", page.getStartRow(), pageSize));
			page.setTotal(Long.parseLong(getSingle(paras.getCountSql()).toString()));
		}
		else
		{
			Object[] newObj = new Object[objects.length + 2];
			System.arraycopy(objects, 0, newObj, 0, objects.length);
			// 赋值LIMIT参数
			newObj[objects.length] = page.getStartRow();
			newObj[objects.length + 1] = pageSize;
			page.setResult(Query(t, paras.getSelectSql() + " LIMIT ?,?", newObj));
			page.setTotal(Long.parseLong(getSingle(paras.getCountSql(), objects).toString()));
		}
		return page;
	}

	public Map<String, Object> QueryMap(String sql)
	{
		return QueryMap(sql, NULL);
	}

	/***
	 * 
	 * @param sql
	 *            要执行的sql
	 * @return 查询的键值对
	 */
	public Map<String, Object> QueryMap(String sql, Object... objects)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildQueryMap(sql, conn, objects);
		}
		catch (SQLException e)
		{
			logger.error("Get map<k,v> faild =>" + sql, e);
			return null;
		}
		finally
		{
			close(conn);
		}
	}

	public Map<String, Object> QueryMap(SqlTranction tran, String sql) throws SQLException
	{
		return QueryMap(tran, sql, NULL);
	}

	public Map<String, Object> QueryMap(SqlTranction tran, String sql, Object... objects) throws SQLException
	{
		return buildQueryMap(sql, tran.Connection(), objects);
	}

	protected abstract Map<String, Object> buildQueryMap(String sql, Connection conn, Object... objects)
			throws SQLException;

	public Object getSingle(SqlTranction tran, String sql) throws SQLException
	{
		return getSingle(tran, sql, NULL);
	}

	public Object getSingle(SqlTranction tran, String sql, Object... objects) throws SQLException
	{
		return buildGetSingle(sql, tran.Connection(), objects);
	}

	public Object getSingle(String sql)
	{
		return getSingle(sql, NULL);
	}

	/***
	 * 
	 * @param sql
	 *            要执行的sql
	 * @return 返回单个值
	 */
	public Object getSingle(String sql, Object... objects)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildGetSingle(sql, conn, objects);
		}
		catch (SQLException e)
		{
			logger.error("Get single faild =>" + sql, e);
			return null;
		}
		finally
		{
			close(conn);
		}
	}

	protected abstract Object buildGetSingle(String sql, Connection conn, Object... objects) throws SQLException;

	public boolean Execute(SqlTranction tran, String sql) throws SQLException
	{
		return Execute(tran, sql, NULL);
	}

	public boolean Execute(SqlTranction tran, String sql, Object... objects) throws SQLException
	{
		return buildExecute(sql, tran.Connection(), objects);
	}

	/***
	 * 
	 * @param sql
	 *            要执行的sql
	 * @return boolean
	 */
	public boolean Execute(String sql)
	{
		return Execute(sql, NULL);
	}

	/***
	 * 
	 * @param sql
	 *            要执行的sql
	 * @param objects
	 *            执行sql语句对应的参数
	 * @return boolean
	 */
	public boolean Execute(String sql, Object... objects)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildExecute(sql, conn, objects);
		}
		catch (SQLException e)
		{
			logger.error("Execute SQL faild => " + sql, e);
			return false;
		}
		finally
		{
			close(conn);
		}
	}

	protected abstract boolean buildExecute(String sql, Connection conn, Object... objects) throws SQLException;

	public boolean Execute(List<String> sqlList)
	{
		long start = getNow();
		SqlTranction tran = null;
		try
		{
			tran = beginTranction();
			for (String sql : sqlList)
			{
				Execute(tran, sql);
			}
			tran.commit();
			logger.debug("Executed, Nums => " + sqlList.size() + ", Times => " + (getNow() - start) + "ns");
			return true;
		}
		catch (SQLException e)
		{
			tran.rollback();
			logger.error("Execute sqlList faild", e);
			return false;
		}
		finally
		{
			close(tran);
		}
	}

	public boolean save(SqlTranction tran, Object obj) throws SQLException
	{
		if (obj instanceof List)
			throw new SQLException("不支持List对象的事务处理");
		TableStruct struct = getEntity(obj.getClass());
		return buildSave(tran.Connection(), struct, obj);
	}

	protected abstract boolean buildSave(Connection conn, TableStruct struct, Object obj) throws SQLException;

	public boolean save(Object obj)
	{
		if (obj instanceof List)
			return saveList((List<?>) obj);

		List<Object> objs = new ArrayList<>();
		objs.add(obj);
		return saveList(objs);
	}

	protected boolean saveList(List<?> objs)
	{
		long start = getNow();
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			int num = 0;
			for (Object obj : objs)
			{
				buildSave(conn, getEntity(obj.getClass()), obj);
				num++;
			}
			if (logger.isDebugEnabled())
			{
				logger.debug("Finish, total => " + objs.size() + ", saved => " + num + ", Times => "
						+ (getNow() - start) + "ns");
			}
			return true;
		}
		catch (SQLException e)
		{
			logger.error("Save object faild", e);
			return false;
		}
		finally
		{
			close(conn);
		}
	}

	public boolean update(Object obj)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildUpdate(conn, getEntity(obj.getClass()), obj, false);
		}
		catch (SQLException e)
		{
			logger.error("Update object faild", e);
			return false;
		}
		finally
		{
			close(conn);
		}
	}

	public boolean update(SqlTranction tran, Object obj) throws SQLException
	{
		return buildUpdate(tran.Connection(), getEntity(obj.getClass()), obj, false);
	}

	public boolean updateNotNull(Object obj)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildUpdate(conn, getEntity(obj.getClass()), obj, true);
		}
		catch (SQLException e)
		{
			logger.error("Update object faild", e);
			return false;
		}
		finally
		{
			close(conn);
		}
	}

	public boolean updateNotNull(SqlTranction tran, Object obj) throws SQLException
	{
		return buildUpdate(tran.Connection(), getEntity(obj.getClass()), obj, true);
	}

	protected abstract boolean buildUpdate(Connection conn, TableStruct struct, Object obj, boolean notNull)
			throws SQLException;

	public boolean delete(Object obj)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildDelete(conn, getEntity(obj.getClass()), obj);
		}
		catch (SQLException e)
		{
			logger.error("Delete object faild", e);
			return false;
		}
		finally
		{
			close(conn);
		}
	}

	public boolean delete(SqlTranction tran, Object obj) throws SQLException
	{
		return buildDelete(tran.Connection(), getEntity(obj.getClass()), obj);
	}

	protected abstract boolean buildDelete(Connection conn, TableStruct struct, Object obj) throws SQLException;

	public boolean saveOrUpdate(Object obj)
	{
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			return buildSaveOrUpdate(conn, getEntity(obj.getClass()), obj);
		}
		catch (SQLException e)
		{
			logger.error("Delete object faild", e);
			return false;
		}
		finally
		{
			close(conn);
		}
	}

	public boolean saveOrUpdate(SqlTranction tran, Object obj) throws SQLException
	{
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
	public boolean RunProcdure(String sql)
	{
		return RunProcdure(sql, null);
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
	public boolean RunProcdure(String sql, List<ProcParamater> paras)
	{
		long start = getNow();
		Connection conn = null;
		try
		{
			conn = ds.getConnection();
			CallableStatement cs = ObjectUtils.buildStatement(conn, sql);
			if (paras != null)
			{
				for (ProcParamater para : paras)
				{
					switch (para.getParameterType())
					{
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
				for (ProcParamater para : paras)
				{
					if (para.getParameterType() == ParameterType.OUT || para.getParameterType() == ParameterType.INOUT)
					{
						para.setValue(cs.getObject(para.getSort()));
					}
				}
			}
			else
			{
				cs.execute();
			}
			cs.close();
			if (logger.isDebugEnabled())
			{
				logger.debug("Procdure => " + sql);
				logger.debug("Times => " + (getNow() - start) + "ns");
			}
			return true;
		}
		catch (SQLException e)
		{
			logger.error("Run procdure faild =>" + sql, e);
			return false;
		}
		finally
		{
			close(conn);
		}
	}

	private static void close(Connection conn)
	{
		try
		{
			if (conn == null || conn.isClosed())
				return;
			conn.close();
		}
		catch (SQLException e)
		{
			logger.error("关闭连接失败", e);
		}
	}

	public void close(SqlTranction tran)
	{
		close(tran.Connection());
	}

	private static TableStruct getEntity(Class<?> cla) throws SQLException
	{
		TableStruct struct = SqlCache.getEntity(cla.getName());
		if(struct == null)
			throw new SQLException("No annotations for '" + cla.getName() + "' were found");
		return struct;
	}

	protected static long getNow()
	{
		return System.nanoTime();
	}
}
