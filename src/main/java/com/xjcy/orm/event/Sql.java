package com.xjcy.orm.event;

import java.util.ArrayList;
import java.util.List;

import com.xjcy.util.ObjectUtils;
import com.xjcy.util.StringUtils;

public class Sql {
	static final String SQL_SELECT = "SELECT * FROM %s";
	static final String SQL_COUNT = "SELECT COUNT(1) FROM %s";
	static final String SQL_INNERJOIN = "SELECT t.*,t1.* FROM %s t,%s t1 WHERE t.%s=t1.%s";
	static final String SQL_LEFTJOIN = "SELECT t.*,t1.* FROM %s t LEFT JOIN %s t1 ON t.%s=t1.%s";
	private String sql;
	private Object[] objects;

	public Sql(String sql, Object... objects) {
		this.sql = sql;
		this.objects = objects;
	}

	public static Sql parse(String sql, Object... objects) {
		return new Sql(sql, objects);
	}

	public static Sql check(String sql, String sql2, Object... objects) {
		String key = null;
		StringBuilder builder = new StringBuilder(sql);
		List<Object> objs = new ArrayList<>();
		for (int i = 0; i < objects.length; i++) {
			if (i % 2 == 0) {
				key = (String) objects[i];
			} else {
				if (objects[i] != null && StringUtils.isNotBlank(objects[i].toString())) {
					// 判断sql中是否有where关键字
					if (builder.indexOf(" WHERE ") > -1)
						builder.append(" AND ").append(key);
					else
						builder.append(" WHERE ").append(key);
					objs.add(objects[i]);
				}
			}
		}
		if (StringUtils.isNotBlank(sql2))
			builder.append(" ").append(sql2);
		return new Sql(builder.toString(), objs.toArray());
	}

	public static Sql select(String table) {
		return new Sql(String.format(SQL_SELECT, table));
	}

	public static Sql count(String table) {
		return new Sql(String.format(SQL_COUNT, table));
	}

	public static Sql join(String table1, String table2, String joinField) {
		return new Sql(String.format(SQL_INNERJOIN, table1, table2, joinField, joinField));
	}

	public static Sql leftJoin(String table1, String table2, String joinField) {
		return new Sql(String.format(SQL_LEFTJOIN, table1, table2, joinField, joinField));
	}

	public Object[] getData() {
		return this.objects;
	}

	@Override
	public String toString() {
		return this.sql;
	}

	public Object[] getData(Object[] objArray) {
		return ObjectUtils.mergeArray(objects, objArray);
	}

	public boolean noData() {
		if (this.objects == null)
			return true;
		if (this.objects.length == 0)
			return true;
		if (this.objects[0] == null)
			return true;
		return false;
	}
}
