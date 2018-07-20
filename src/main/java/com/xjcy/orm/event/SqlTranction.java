package com.xjcy.orm.event;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlTranction
{
	Connection Connection();

	void commit();

	void rollback();

	void close() throws SQLException;
}
