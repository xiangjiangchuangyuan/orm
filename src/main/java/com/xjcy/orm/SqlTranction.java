package com.xjcy.orm;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

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

	public void rollback()
	{
		try
		{
			connection.rollback();
			logger.debug("Tranction rollback");
		}
		catch (SQLException e)
		{
			logger.error("Tranction rollback faild", e);
		}
	}

	public Connection Connection()
	{
		return this.connection;
	}

	public void close() throws SQLException {
		if (!this.connection.getAutoCommit()) {
			connection.setAutoCommit(true);
			logger.debug("Set autocommit true on tranction close");
		}
		connection.close();
	}
}
