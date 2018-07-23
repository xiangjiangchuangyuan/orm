package com.xjcy.orm.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.ObjectUtils;
import com.xjcy.orm.core.SqlCache;
import com.xjcy.orm.event.Sql;

public class ResultHandler {
	private static final Logger logger = Logger.getLogger(ResultHandler.class);

	private List<Object> vos;
	private Object obj;
	private Map<Object, Object> map;
	private Class<?> o;

	@SuppressWarnings("unchecked")
	public ResultHandler(List<?> vos2, Class<?> o) {
		this.vos = (List<Object>) vos2;
		this.o = o;
	}

	@SuppressWarnings("unchecked")
	public ResultHandler(Map<?, ?> map2) {
		this.map = (Map<Object, Object>) map2;
	}

	public ResultHandler(Object obj2) {
		this.obj = obj2;
	}

	public void data(ResultSet rs, Sql sql) throws SQLException {
		if (this.vos != null) {
			if (o == null) {
				while (rs.next()) {
					this.vos.add(rs.getObject(1));
				}
			} else {
				ResultMap map = SqlCache.get(o, sql.getSql(), rs.getMetaData());
				if (map.size() > 0) {
					while (rs.next()) {
						this.vos.add(ObjectUtils.copyValue(map, rs, o));
					}
				}
			}
		} else if (this.map != null) {
			while (rs.next()) {
				this.map.put(rs.getObject(1), rs.getObject(2));
			}
		} else {
			if (rs.next()) {
				this.obj = rs.getObject(1);
				logger.debug("obj == " + obj);
			}
		}
	}
}
