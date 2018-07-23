package com.xjcy.orm.mapper;

import java.sql.SQLException;

import com.xjcy.orm.event.RecordSession;
import com.xjcy.orm.event.SqlSession;
import com.xjcy.orm.event.SqlTranction;

public class ActiveRecord {
	private static RecordSession sessionImpl;

	public boolean save() {
		return sessionImpl.save(this);
	}

	public boolean save(SqlTranction tran) throws SQLException {
		return sessionImpl.save(tran, this);
	}

	public boolean saveOrUpdate() {
		return sessionImpl.saveOrUpdate(this);
	}

	public boolean saveOrUpdate(SqlTranction tran) throws SQLException {
		return sessionImpl.saveOrUpdate(tran, this);
	}

	public static void init(SqlSession session) {
		ActiveRecord.sessionImpl = (RecordSession)session;
	}
}
