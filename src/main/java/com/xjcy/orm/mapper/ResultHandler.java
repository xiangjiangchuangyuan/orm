package com.xjcy.orm.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.SqlCache;

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

	public void data(ResultSet rs, String sql) throws SQLException {
		if (this.vos != null) {
			if (o == null) {
				while (rs.next()) {
					this.vos.add(rs.getObject(1));
				}
			} else {
				ResultMap resultMap = SqlCache.get(o, sql, rs.getMetaData());
				if (resultMap.size() > 0) {
					while (rs.next()) {
						this.vos.add(copyValue(resultMap, rs, o));
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

	private static Object copyValue(ResultMap map, ResultSet rs, Class<?> t) {
		try {
			Object tt = t.newInstance();
			Set<String> keys = map.Keys();
			for (String label : keys) {
				map.setObject(tt, t, label, rs.getObject(label));
			}
			return tt;
		} catch (Exception e) {
			logger.error("数据库对象转换失败,传入对象 => " + t.getName(), e);
			return null;
		}
	}
}
