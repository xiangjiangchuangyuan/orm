package com.xjcy.orm.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ResultMapper<T> {
	T doStatement(PreparedStatement stmt) throws SQLException;

	String getSql();

	Object[] getArgs();

	void clean();
}
