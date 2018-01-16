package com.xjcy.orm.mapper;

public class PageParamater
{
	private String selectSql;
	private String countSql;
	private int pageNum;
	private int pageSize;

	public PageParamater(String selectSql, String countSql, int pageNum, int pageSize)
	{
		this.selectSql = selectSql;
		this.countSql = countSql;
		this.pageNum = pageNum;
		this.pageSize = pageSize;
	}

	public String getSelectSql()
	{
		return selectSql;
	}

	public String getCountSql()
	{
		return countSql;
	}

	public int getPageNum()
	{
		return pageNum;
	}

	public int getPageSize()
	{
		return pageSize;
	}
}
