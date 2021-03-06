package com.xjcy.orm;

import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import javax.sql.DataSource;

import com.xjcy.orm.core.ScriptUtils;
import com.xjcy.orm.core.Selector;
import com.xjcy.orm.mapper.PageInfo;
import com.xjcy.util.ObjectUtils;

public class SqlSession extends SqlSessionBase {
	public SqlSession(DataSource ds) {
		super(ds);
	}

	public <T> List<T> selectList(Class<T> target, Selector selector) {
		return execute(new queryResultMapper<>(target, selector));
	}

	public <T> List<T> selectList(Class<T> target, String sql, Object... array) {
		return execute(new queryResultMapper<>(target, sql, array));
	}

	public <T> List<T> selectList(SqlTranction tran, Class<T> target, String sql, Object... array) throws SQLException {
		return execute(tran, new queryResultMapper<>(target, sql, array));
	}

	public <T> List<T> selectPage(Class<T> target, PageInfo pageInfo, String fetchSql, String countSql,
			Object... array) {
		pageInfo.setTotal(selectOne(Long.class, countSql, array));
		fetchSql = fetchSql + " LIMIT ?,?";
		Object[] temp = ObjectUtils.mergeArray(array, pageInfo.getStartRow(), pageInfo.getPageSize());
		return selectList(target, fetchSql, temp);
	}

	public <T> T selectOne(Class<T> target, Selector selector) {
		return execute(new singleResultMapper<>(target, selector));
	}

	public <T> T selectOne(Class<T> target, String sql, Object... array) {
		return execute(new singleResultMapper<>(target, sql, array));
	}

	public <T> T selectOne(SqlTranction tran, Class<T> target, String sql, Object... array) throws SQLException {
		return execute(tran, new singleResultMapper<>(target, sql, array));
	}

	public int insert(Object data) {
		Entry<String, Object[]> entry = ScriptUtils.buildInsert(data);
		if (entry != null)
			return execute(entry.getKey(), entry.getValue());
		return -1;
	}

	public int insert(SqlTranction tran, Object data) throws SQLException {
		Entry<String, Object[]> entry = ScriptUtils.buildInsert(data);
		if (entry != null)
			return execute(tran, entry.getKey(), entry.getValue());
		return -1;
	}

	public int update(Object data) {
		Entry<String, Object[]> entry = ScriptUtils.buildUpdate(data);
		if (entry != null)
			return execute(entry.getKey(), entry.getValue());
		return -1;
	}

	public int update(SqlTranction tran, Object data) throws SQLException {
		Entry<String, Object[]> entry = ScriptUtils.buildUpdate(data);
		if (entry != null)
			return execute(tran, entry.getKey(), entry.getValue());
		return -1;
	}

	public int execute(Selector selector) {
		return execute(new defaultResultMapper(selector));
	}


	public int execute(final String sql, final Object... array) {
		return execute(new defaultResultMapper(sql, array));
	}

	public int execute(SqlTranction tran, final String sql, final Object... array) throws SQLException {
		return execute(tran, new defaultResultMapper(sql, array));
	}
}
