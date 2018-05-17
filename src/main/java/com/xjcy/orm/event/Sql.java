package com.xjcy.orm.event;

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
}
