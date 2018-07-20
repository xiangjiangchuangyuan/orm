package com.xjcy.orm.mapper;

import com.xjcy.orm.event.Sql;
import com.xjcy.util.ObjectUtils;

public class PageParamater {
	private Sql selectSql;
	private Sql countSql;
	private int pageNum;
	private int pageSize;

	public PageParamater(String selectSql, String countSql, int pageNum, int pageSize) {
		this(Sql.parse(selectSql), Sql.parse(countSql), pageNum, pageSize);
	}

	public PageParamater(Sql selectSql, Sql countSql, int pageNum, int pageSize) {
		this.selectSql = selectSql;
		this.countSql = countSql;
		this.pageNum = pageNum;
		this.pageSize = pageSize;
	}

	public Sql getSelectSql(Object[] objects, int startRow) {
		String sql = this.selectSql.getSql() + " LIMIT ?,?";
		Object[] temp = ObjectUtils.mergeArray(objects, new Object[] { startRow, pageSize });
		Object[] newObj = this.selectSql.getData(temp);
		return Sql.parse(sql, newObj);
	}

	public Sql getCountSql(Object[] objects) {
		String sql = this.countSql.getSql();
		Object[] newObj = this.selectSql.getData(objects);
		return Sql.parse(sql, newObj);
	}

	public int getPageNum() {
		return pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}
}
