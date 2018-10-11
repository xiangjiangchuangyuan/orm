package com.xjcy.orm;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.JdbcUtils;

public class SqlTranction
{

	private static final Logger logger = Logger.getLogger(SqlTranction.class);
	
	private Connection connection;

	public SqlTranction(Connection conn) throws SQLException
	{
		conn.setAutoCommit(false);
		this.connection = conn;
	}

	public void commit()
	{
		try
		{
			connection.commit();
			logger.debug("Tranction commit");
		}
		catch (SQLException e)
		{
			logger.error("Tranction commit faild", e);
		}
	}

	public void rollback(Exception e)
	{
		try
		{
			logger.error("Execute faild", e);
			connection.rollback();
			logger.debug("Tranction rollback");
		}
		catch (SQLException ee)
		{
			logger.error("Tranction rollback faild", ee);
		}
	}

	public Connection Connection()
	{
		return this.connection;
	}

	public void close() {
		JdbcUtils.closeTranction(connection);
		logger.debug("Tranction closed");
	}
}
