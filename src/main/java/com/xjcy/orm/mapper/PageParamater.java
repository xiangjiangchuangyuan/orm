package com.xjcy.orm.mapper;

public interface PageParamater
{
	String countSql();
	String selectSql();
	int pageNum();
	int pageSize();
}
