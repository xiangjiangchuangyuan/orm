package com.xjcy.orm.mapper;

import com.xjcy.orm.event.Sql;

public class PageParamater {
	private String selectSql;
	private String countSql;
	private int pageNum;
	private int pageSize;

	public PageParamater(String selectSql, String countSql, int pageNum, int pageSize) {
		this(Sql.parse(selectSql), Sql.parse(countSql), pageNum, pageSize);
	}

	public PageParamater(Sql selectSql, Sql countSql, int pageNum, int pageSize) {
		this.selectSql = selectSql.toString();
		this.countSql = countSql.toString();
		this.pageNum = pageNum;
		this.pageSize = pageSize;
	}

	public String getSelectSql() {
		return selectSql;
	}

	public String getCountSql() {
		return countSql;
	}

	public int getPageNum() {
		return pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}
}
