package com.xjcy.orm;

import com.xjcy.orm.core.EntityUtils;
import com.xjcy.orm.event.SqlSession;
import com.xjcy.orm.mapper.ActiveRecord;
import com.xjcy.util.StringUtils;

import javax.sql.DataSource;

public class SqlSessionFactory
{
	private static SqlSession sessionImpl;

	public static SqlSession getSession(DataSource ds, String entityPkg)
	{
		if (sessionImpl == null)
		{
			DefaultSessionImpl defaultSession = new DefaultSessionImpl(ds);
			SqlSessionProxy proxy = new SqlSessionProxy(defaultSession);
			sessionImpl = proxy.getProxy();
			ActiveRecord.init(sessionImpl);
			if (!StringUtils.isEmpty(entityPkg))
				EntityUtils.cacheEntities(entityPkg);
		}
		return sessionImpl;
	}
}
