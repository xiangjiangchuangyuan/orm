package com.xjcy.orm.entity;

import java.sql.SQLException;

import com.xjcy.orm.event.SqlTranction;

public interface EntitySession {
	boolean update(DbEntity entity);

	boolean update(SqlTranction tran, DbEntity entity) throws SQLException;

	boolean updateNotNull(DbEntity entity);

	boolean updateNotNull(SqlTranction tran, DbEntity entity) throws SQLException;

	boolean save(DbEntity entity);

	boolean save(SqlTranction tran, DbEntity entity) throws SQLException;

	boolean saveOrUpdate(DbEntity entity);

	boolean saveOrUpdate(SqlTranction tran, DbEntity entity) throws SQLException;

	boolean delete(DbEntity entity);

	boolean delete(SqlTranction tran, DbEntity entity) throws SQLException;
}
