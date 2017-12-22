package com.xjcy.orm;

import com.xjcy.orm.event.SqlSession;

import javax.sql.DataSource;

public class SqlSessionFactory
{
	private static DefaultSessionImpl sessionImpl;

	public static SqlSession getSession(DataSource ds)
	{
		if (sessionImpl == null)
			sessionImpl = new DefaultSessionImpl(ds);
		return sessionImpl;
	}
}
