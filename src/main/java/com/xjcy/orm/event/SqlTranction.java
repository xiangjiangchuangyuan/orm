package com.xjcy.orm.event;

import java.sql.Connection;

public interface SqlTranction
{
	Connection Connection();

	void commit();

	void rollback();
}
