package com.xjcy.orm;

import com.xjcy.orm.core.EntityUtils;
import com.xjcy.orm.event.SqlSession;
import com.xjcy.util.StringUtils;

import javax.sql.DataSource;

public class SqlSessionFactory
{
	private static DefaultSessionImpl sessionImpl;

	public static SqlSession getSession(DataSource ds, String entityPkg)
	{
		if (sessionImpl == null)
		{
			sessionImpl = new DefaultSessionImpl(ds);
			if (!StringUtils.isEmpty(entityPkg))
				EntityUtils.cacheEntities(entityPkg);
		}
		return sessionImpl;
	}
}
