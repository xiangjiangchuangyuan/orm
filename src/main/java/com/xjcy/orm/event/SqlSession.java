package com.xjcy.orm.event;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.xjcy.orm.mapper.PageInfo;
import com.xjcy.orm.mapper.PageParamater;
import com.xjcy.orm.mapper.ProcParamater;

public interface SqlSession
{
	SqlTranction beginTranction() throws SQLException;

	void close(SqlTranction tran);

	<T> PageInfo queryByPage(Class<T> t, int pageNum, int pageSize, String sql);

	<T> PageInfo queryByPage(Class<T> t, int pageNum, int pageSize, String sql, Object... objects);

	<T> PageInfo Query(Class<T> t, PageParamater paras, Object... objects);

	boolean RunProcdure(String sql);

	boolean RunProcdure(String sql, List<ProcParamater> paras);

	<T> List<T> Query(Class<T> t) throws SQLException;

	<T> List<T> Query(Class<T> t, String sql);

	<T> List<T> Query(Class<T> t, String sql, Object... objects);

	<T> List<T> Query(SqlTranction tran, Class<T> t) throws SQLException;

	<T> List<T> Query(SqlTranction tran, Class<T> t, String sql) throws SQLException;

	<T> List<T> Query(SqlTranction tran, Class<T> t, String sql, Object... objects) throws SQLException;

	<T> List<T> QueryList(String sql);

	<T> List<T> QueryList(String sql, Object... objects);

	<T> List<T> QueryList(SqlTranction tran, String sql) throws SQLException;

	<T> List<T> QueryList(SqlTranction tran, String sql, Object... objects) throws SQLException;

	<T> T Single(Class<T> t, String sql);

	<T> T Single(Class<T> t, String sql, Object... objects);

	<T> T Single(SqlTranction tran, Class<T> t, String sql) throws SQLException;

	<T> T Single(SqlTranction tran, Class<T> t, String sql, Object... objects) throws SQLException;

	Map<String, Object> QueryMap(String sql);

	Map<String, Object> QueryMap(String sql, Object... objects);

	Map<String, Object> QueryMap(SqlTranction tran, String sql) throws SQLException;

	Map<String, Object> QueryMap(SqlTranction tran, String sql, Object... objects) throws SQLException;

	Object getSingle(String sql);

	Object getSingle(String sql, Object... objects);

	Object getSingle(SqlTranction tran, String sql) throws SQLException;

	Object getSingle(SqlTranction tran, String sql, Object... objects) throws SQLException;

	boolean Execute(String sql);

	boolean Execute(String sql, Object... objects);

	boolean Execute(SqlTranction tran, String sql) throws SQLException;

	boolean Execute(SqlTranction tran, String sql, Object... objects) throws SQLException;

	boolean Execute(List<String> sqlList);

	boolean update(Object obj);

	boolean update(SqlTranction tran, Object obj) throws SQLException;

	boolean updateNotNull(Object obj);

	boolean updateNotNull(SqlTranction tran, Object obj) throws SQLException;

	boolean save(Object obj);

	boolean save(SqlTranction tran, Object obj) throws SQLException;

	boolean saveOrUpdate(Object obj);

	boolean saveOrUpdate(SqlTranction tran, Object obj) throws SQLException;

	boolean delete(Object obj);

	boolean delete(SqlTranction tran, Object obj) throws SQLException;
}
