package com.xjcy.orm.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.xjcy.orm.jpa.Table;
import com.xjcy.orm.util.Assert;

public class Selector {

	private StringBuilder sql;
	private List<Object> paras;
	private String tbl;
	private String selectHead;

	private Selector(String name) {
		sql = new StringBuilder();
		paras = new ArrayList<>();
		this.tbl = name;
		this.selectHead = "select * from " + name;
	}

	public static Selector from(Class<?> cla) throws SQLException {
		Table table = cla.getAnnotation(Table.class);
		Assert.notNull(table);
		return new Selector(table.value());
	}

	public static Selector from(String name) {
		Assert.notNull(name);
		return new Selector(name);
	}

	public Selector where(String column, String condition, Object obj) {
		sql.append(" where ").append(column).append(" ").append(condition).append(" ?");
		paras.add(obj);
		return this;
	}

	public Selector asc(String column) {
		if (sql.indexOf("order by") != -1)
			sql.append(" ,").append(column);
		else
			sql.append(" order by ").append(column);
		return this;
	}

	public Selector desc(String column) {
		if (sql.indexOf("order by") != -1)
			sql.append(" ,").append(column).append(" desc");
		else
			sql.append(" order by ").append(column).append(" desc");
		return this;
	}

	public Selector limit(int limit) {
		sql.append(" limit ?");
		paras.add(limit);
		return this;
	}

	public Selector limit(int start, int size) {
		sql.append(" limit ?, ?");
		paras.add(start);
		paras.add(size);
		return this;
	}

	public Selector select(String columns) {
		Assert.notNull(columns);
		this.selectHead = "select " + columns + " from " + this.tbl;
		return this;
	}

	public Selector and(String column, String condition, Object obj) {
		sql.append(" and ").append(column).append(" ").append(condition).append(" ?");
		paras.add(obj);
		return this;
	}

	public Selector groupBy(String columns) {
		sql.append(" group by ").append(columns);
		return this;
	}

	public Object[] array() {
		if (!paras.isEmpty()) {
			Object[] temp = new Object[paras.size()];
			paras.toArray(temp);
			return temp;
		}
		return null;
	}

	@Override
	public String toString() {
		String str = sql.toString();
		sql.setLength(0);
		sql.append(this.selectHead);
		sql.append(str);
		return sql.toString();
	}
}
