package com.xjcy.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.ObjectUtils;
import com.xjcy.orm.entity.DbEntity;
import com.xjcy.orm.entity.EntitySession;
import com.xjcy.orm.event.Sql;
import com.xjcy.orm.event.SqlSession;
import com.xjcy.orm.mapper.ResultHandler;

public class DefaultSessionImpl extends AbstractSession implements SqlSession, EntitySession {
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
	protected boolean doExecute(Connection conn, DbEntity entity, Sql sql) throws SQLException {
		if (sql != null) {
			return ObjectUtils.executeUpdate(conn, sql) > 0;
		}
		PreparedStatement ps = ObjectUtils.buildStatement(conn, entity);
		if (ps.executeUpdate() > 0 && entity.handleAutoGenerageId()) {
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				logger.debug("id => " + rs.getInt(1));
				entity.setAutoGenerateIdValue(rs.getInt(1));
			}
			rs.close();
		}
		ps.close();
		return true;
	}
}
