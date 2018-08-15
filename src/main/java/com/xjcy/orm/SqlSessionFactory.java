package com.xjcy.orm;

import com.xjcy.orm.entity.DbEntity;
import com.xjcy.orm.event.SqlSession;

import javax.sql.DataSource;

public class SqlSessionFactory {
	private static SqlSession sessionImpl;

	public static SqlSession getSession(DataSource ds) {
		if (sessionImpl == null) {
			DefaultSessionImpl defaultSession = new DefaultSessionImpl(ds);
			SqlSessionProxy proxy = new SqlSessionProxy(defaultSession);
			sessionImpl = proxy.getProxy();
			DbEntity.init(sessionImpl);
		}
		return sessionImpl;
	}
}
