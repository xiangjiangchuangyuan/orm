package com.xjcy.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.ObjectUtils;
import com.xjcy.orm.core.SqlCache;
import com.xjcy.orm.event.RecordSession;
import com.xjcy.orm.event.Sql;
import com.xjcy.orm.event.SqlSession;
import com.xjcy.orm.mapper.ResultMap;
import com.xjcy.orm.mapper.TableStruct;
import com.xjcy.orm.mapper.TableStruct.SQLType;

public class DefaultSessionImpl extends AbstractSession implements SqlSession , RecordSession
{
	private static final Logger logger = Logger.getLogger(DefaultSessionImpl.class);
	
	public DefaultSessionImpl(DataSource ds)
	{
		this.ds = ds;
	}

	@Override
	protected Object doQuery(Connection conn, Object o, Sql sql, Result result) throws SQLException {
		ResultSet rs = ObjectUtils.buildResultSet(conn, sql);
		Object rtn = null;
		if (result == Result.LIST) {
			List<Object> vos = new ArrayList<>();
			if (o == null) {
				while (rs.next()) {
					vos.add(rs.getObject(1));
				}
			} else {
				Class<?> oo = (Class<?>) o;
				ResultMap map = SqlCache.get(oo, sql.getSql(), rs.getMetaData());
				if (map.size() > 0) {
					while (rs.next()) {
						vos.add(ObjectUtils.copyValue(map, rs, oo));
					}
				}
			}
			rtn = vos;
		}
		if (result == Result.MAP) {
			Map<Object, Object> map = new HashMap<>();
			while (rs.next()) {
				map.put(rs.getObject(1), rs.getObject(2));
			}
			rtn = map;
		}
		if (result == Result.SINGLE && rs.next()) {
			rtn = rs.getObject(1);
		}
		rs.getStatement().close();
		rs.close();
		return rtn;
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
