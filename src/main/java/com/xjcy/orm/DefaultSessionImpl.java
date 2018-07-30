package com.xjcy.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.ObjectUtils;
import com.xjcy.orm.event.RecordSession;
import com.xjcy.orm.event.Sql;
import com.xjcy.orm.event.SqlSession;
import com.xjcy.orm.mapper.ResultHandler;
import com.xjcy.orm.mapper.TableStruct;
import com.xjcy.orm.mapper.TableStruct.SQLType;

public class DefaultSessionImpl extends AbstractSession implements SqlSession, RecordSession {
	private static final Logger logger = Logger.getLogger(DefaultSessionImpl.class);

	public DefaultSessionImpl(DataSource ds) {
		this.ds = ds;
	}

	@Override
	protected void doQuery(Connection conn, Sql sql, ResultHandler handler) throws SQLException {
		ResultSet rs = ObjectUtils.buildResultSet(conn, sql);
		handler.data(rs, sql.getSql());
		rs.close();
	}

	@Override
	protected boolean doExecute(Connection conn, TableStruct struct, Sql sql) throws SQLException {
		if (sql != null) {
			return ObjectUtils.executeUpdate(conn, sql) > 0;
		}
		PreparedStatement ps = ObjectUtils.buildStatement(conn, struct);
		if (ps.executeUpdate() > 0 && struct.hasGenerageKey() && struct.getSqlType() == SQLType.INSERT) {
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				logger.debug("id => " + rs.getObject(1));
				struct.setGenerateKey(rs.getObject(1));
			}
			rs.close();
		}
		ps.close();
		return true;
	}
}
