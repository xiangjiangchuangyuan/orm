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

	<T> PageInfo<T> selectPage(Class<T> t, PageParamater paras, Object... objects);

	boolean callProcdure(String sql);

	boolean callProcdure(String sql, List<ProcParamater> paras);

	<T> List<T> selectList(Class<T> t, String sql, Object... objects);

	<T> List<T> selectList(SqlTranction tran, Class<T> t, String sql, Object... objects) throws SQLException;

	<E> List<E> selectList(String sql, Object... objects);

	<E> List<E> selectList(SqlTranction tran, String sql, Object... objects) throws SQLException;

	<T> T selectOne(Class<T> t, String sql, Object... objects);

	<T> T selectOne(SqlTranction tran, Class<T> t, String sql, Object... objects) throws SQLException;

	<K, V> Map<K, V> selectMap(String sql, Object... objects);

	<K, V> Map<K, V> selectMap(SqlTranction tran, String sql, Object... objects) throws SQLException;

	Object getSingle(String sql, Object... objects);

	Object getSingle(SqlTranction tran, String sql, Object... objects) throws SQLException;

	boolean execute(String sql, Object... objects);

	boolean execute(SqlTranction tran, String sql, Object... objects) throws SQLException;
}
