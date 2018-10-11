package com.xjcy.orm;

import javax.sql.DataSource;

public class SqlSessionFactory {
	private static SqlSession sessionImpl;

	public static SqlSession getSession() {
		return sessionImpl;
	}

	public static void createSession(DataSource ds) {
		if (sessionImpl == null) {
			sessionImpl = new SqlSession(ds);
		}
	}

}
