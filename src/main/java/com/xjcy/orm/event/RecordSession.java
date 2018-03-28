package com.xjcy.orm.event;

import java.sql.SQLException;

public interface RecordSession {
	boolean update(Object obj);

	boolean update(SqlTranction tran, Object obj) throws SQLException;

	boolean updateNotNull(Object obj);

	boolean updateNotNull(SqlTranction tran, Object obj) throws SQLException;

	boolean save(Object obj);

	boolean save(SqlTranction tran, Object obj) throws SQLException;

	boolean saveOrUpdate(Object obj);

	boolean saveOrUpdate(SqlTranction tran, Object obj) throws SQLException;

	boolean delete(Object obj);

	boolean delete(SqlTranction tran, Object obj) throws SQLException;
}
