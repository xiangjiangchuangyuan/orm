package com.xjcy.orm;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.xjcy.orm.event.SqlTranction;

public class DefaultTranctionImpl implements SqlTranction
{

	private static final Logger logger = Logger.getLogger(DefaultTranctionImpl.class);
	
	private Connection connection;

	public DefaultTranctionImpl(Connection conn) throws SQLException
	{
		conn.setAutoCommit(false);
		this.connection = conn;
	}

	@Override
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

	@Override
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

	@Override
	public Connection Connection()
	{
		return this.connection;
	}
}
