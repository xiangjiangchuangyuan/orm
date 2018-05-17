package com.xjcy.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.FieldUtils;
import com.xjcy.orm.core.ObjectUtils;
import com.xjcy.orm.core.SqlCache;
import com.xjcy.orm.event.RecordSession;
import com.xjcy.orm.event.Sql;
import com.xjcy.orm.event.SqlSession;
import com.xjcy.orm.mapper.ResultMap;
import com.xjcy.orm.mapper.TableStruct;
import com.xjcy.orm.mapper.TableStruct.SQLType;

public class DefaultSessionImpl extends AbstractSession implements SqlSession , RecordSession
{
	private static final Logger logger = Logger.getLogger(DefaultSessionImpl.class);

	public DefaultSessionImpl(DataSource ds)
	{
		this.ds = ds;
	}

	@Override
	protected <T> List<T> buildQuery(Connection conn, Class<T> t, Sql sql) throws SQLException
	{
		long start = getNow();
		List<T> vos = new ArrayList<>();
		ResultSet rs = ObjectUtils.buildResultSet(conn, sql);
		long start2 = getNow();
		String cacheKey = t.getName() + "_" + sql;
		ResultMap map = SqlCache.get(cacheKey);
		if (map != null)
		{
			if (logger.isDebugEnabled())
				logger.debug("Load map from cache => " + (getNow() - start2) + "ns");
		}
		else
		{
			map = ObjectUtils.buildColumnMap(t, rs.getMetaData());
			SqlCache.put(cacheKey, map);
			if (logger.isDebugEnabled())
				logger.debug("Load map from metadata => " + (getNow() - start2) + "ns");
		}
		if (map.size() > 0)
		{
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next())
			{
				vos.add(ObjectUtils.copyValue(map, rs, t, metaData));
			}
		}
		rs.getStatement().close();
		rs.close();
		if (logger.isDebugEnabled())
		{
			logger.debug("SQL => " + sql);
			logger.debug("Result rows => " + vos.size() + "; columns => " + map.size() + "; time => "
					+ (getNow() - start) + "ns");
		}
		return vos;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <E> List<E> buildQueryList(Connection conn, Sql sql) throws SQLException
	{
		long start = getNow();
		ResultSet rs = ObjectUtils.buildResultSet(conn, sql);
		List<E> dataList = new ArrayList<>();
		while (rs.next())
		{
			dataList.add((E) rs.getObject(1));
		}
		rs.getStatement().close();
		rs.close();
		if (logger.isDebugEnabled())
		{
			logger.debug("SQL => " + sql);
			logger.debug("Times => " + (getNow() - start) + "ns");
		}
		return dataList;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <K, V> Map<K, V> buildQueryMap(Connection conn, Sql sql) throws SQLException
	{
		long start = getNow();
		ResultSet rs = ObjectUtils.buildResultSet(conn, sql);
		Map<K, V> map = new HashMap<>();
		while (rs.next())
		{
			map.put((K)rs.getObject(1), (V)rs.getObject(2));
		}
		rs.getStatement().close();
		rs.close();
		if (logger.isDebugEnabled())
		{
			logger.debug("SQL => " + sql);
			logger.debug("Map<K,V> => " + map.size() + "; Times => " + (getNow() - start) + "ns");
		}
		return map;
	}

	@Override
	protected Object buildGetSingle(Connection conn, Sql sql) throws SQLException
	{
		long start = getNow();
		Object obj = null;
		ResultSet rs = ObjectUtils.buildResultSet(conn, sql);
		if (rs.next())
			obj = rs.getObject(1);
		rs.getStatement().close();
		rs.close();
		if (logger.isDebugEnabled())
		{
			logger.debug("SQL => " + sql);
			logger.debug("Result => " + obj + "; Times => " + (getNow() - start) + "ns");
		}
		return obj;
	}

	@Override
	protected boolean buildExecute(Connection conn, Sql sql) throws SQLException
	{
		long start = getNow();
		Integer num = ObjectUtils.executeUpdate(conn, sql);
		if (logger.isDebugEnabled())
		{
			logger.debug("SQL => " + sql);
			logger.debug("Rows => " + num + ", Times => " + (getNow() - start) + "ns");
		}
		return true;
	}

	@Override
	protected boolean buildSave(Connection conn, TableStruct struct, Object obj) throws SQLException
	{
		long start = getNow();
		// 重新赋值Columns
		struct.setColumns(obj, SQLType.INSERT);
		Object result = null;
		PreparedStatement ps = ObjectUtils.buildStatement(conn, struct);
		if (ps.executeUpdate() > 0 && struct.hasGenerageKey())
		{
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next())
			{
				result = rs.getObject(1);
				FieldUtils.setValue(obj, struct.getGenerageKey(), result);
			}
			rs.close();
		}
		ps.close();
		if (logger.isDebugEnabled())
			logger.debug("Saved, ID => " + result + ", Times => " + (getNow() - start) + "ns");
		return true;
	}

	@Override
	protected boolean buildUpdate(Connection conn, TableStruct struct, Object obj, boolean notNull) throws SQLException
	{
		long start = getNow();
		struct.setColumns(obj, (notNull ? SQLType.UPDATENOTNULL : SQLType.UPDATE));
		PreparedStatement ps = ObjectUtils.buildStatement(conn, struct);
		Integer num = ps.executeUpdate();
		ps.close();
		if (logger.isDebugEnabled())
			logger.debug("Updated, Rows => " + num + ", Times => " + (getNow() - start) + "ns");
		return true;
	}

	@Override
	protected boolean buildDelete(Connection conn, TableStruct struct, Object obj) throws SQLException
	{
		long start = getNow();
		struct.setColumns(obj, SQLType.DELETE);
		PreparedStatement ps = ObjectUtils.buildStatement(conn, struct);
		Integer num = ps.executeUpdate();
		ps.close();
		if (logger.isDebugEnabled())
			logger.debug("Deleted, Rows => " + num + ", Times => " + (getNow() - start) + "ns");
		return true;
	}

	@Override
	protected boolean buildSaveOrUpdate(Connection conn, TableStruct struct, Object obj) throws SQLException
	{
		if (struct.hasPrimaryKey(obj))
		{
			logger.debug("发现主键有值，定义为update");
			return buildUpdate(conn, struct, obj, true);
		}
		else
		{
			logger.debug("没有发现主键的value，定义为insert");
			return buildSave(conn, struct, obj);
		}
	}
}
