package com.xjcy.orm.entity;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.xjcy.orm.event.SqlSession;
import com.xjcy.orm.event.SqlTranction;

public abstract class DbEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8002741849005594775L;

	private static final Logger logger = Logger.getLogger(DbEntity.class);

	protected String tableName;
	protected boolean hasAutoId;
	protected String[] columns;
	protected String[] primaryKeys;
	private static EntitySession sessionImpl;
	private Map<Integer, Object> sqlMap = new HashMap<>();
	private SQLType sqlType;

	public boolean update() {
		return sessionImpl.update(this);
	}

	public boolean update(SqlTranction tran) throws SQLException {
		return sessionImpl.update(tran, this);
	}

	public boolean save() {
		return sessionImpl.save(this);
	}

	public boolean save(SqlTranction tran) throws SQLException {
		return sessionImpl.save(tran, this);
	}

	public boolean saveOrUpdate() {
		return sessionImpl.saveOrUpdate(this);
	}

	public boolean saveOrUpdate(SqlTranction tran) throws SQLException {
		return sessionImpl.saveOrUpdate(tran, this);
	}

	public static void init(SqlSession session) {
		sessionImpl = (EntitySession) session;
	}

	public abstract boolean hasPrimaryKey();

	public abstract Object getColumnValue(String name);

	public abstract void setAutoId(int val);

	public void buildSqlMap(SQLType sqlType) throws SQLException {
		sqlMap.clear();
		this.sqlType = sqlType;
		switch (sqlType) {
		case INSERT:
			buildInsertMap();
			break;
		case UPDATE:
			if (!hasPrimaryKey())
				throw new SQLException("获取对象的主键失败");
			buildUpdateMap(false);
			break;
		case UPDATENOTNULL:
			if (!hasPrimaryKey())
				throw new SQLException("获取对象的主键失败");
			buildUpdateMap(true);
			break;
		case DELETE:
			if (!hasPrimaryKey())
				throw new SQLException("获取对象的主键失败");
			buildDeleteMap();
			break;
		default:
			break;
		}
	}

	private void buildDeleteMap() {
		try {
			long start = System.nanoTime();
			StringBuffer sql1 = new StringBuffer();
			int j = 1;
			Object obj = null;
			for (String key : columns) {
				obj = getColumnValue(key);
				if (obj != null) {
					sql1.append("`" + key + "`=? AND ");
					sqlMap.put(j, obj);
					j++;
				}
			}
			if (j > 1) {
				sql1.delete(sql1.length() - 5, sql1.length());
			}
			String sql = "delete from " + this.tableName + " where " + sql1.toString();
			if (logger.isDebugEnabled())
				logger.debug("Delete(" + (System.nanoTime() - start) + "ns) => " + sql);
			sqlMap.put(0, sql);
		} catch (Exception e) {
			logger.error("构造DELETE语句失败", e);
		}
	}

	private void buildUpdateMap(boolean notNull) {
		try {
			long start = System.nanoTime();
			StringBuffer sql1 = new StringBuffer(" set ");
			int j = 1;
			if (notNull) {
				Object obj = null;
				for (String key : columns) {
					obj = getColumnValue(key);
					if (!contains(primaryKeys, key) && obj != null) {
						sql1.append("`" + key + "`=?,");
						sqlMap.put(j, obj);
						j++;
					}
				}
			} else {
				for (String key : columns) {
					if (!contains(primaryKeys, key)) {
						sql1.append("`" + key + "`=?,");
						sqlMap.put(j, getColumnValue(key));
						j++;
					}
				}
			}
			sql1.deleteCharAt(sql1.length() - 1);
			StringBuffer sql2 = new StringBuffer();
			if (!isEmpty(primaryKeys)) {
				for (String key : primaryKeys) {
					sql2.append("`" + key + "`=? AND ");
					sqlMap.put(j, getColumnValue(key));
					j++;
				}
			}
			if (sql2.length() > 0)
				sql2.delete(sql2.length() - 4, sql2.length());
			String sql = "update " + this.tableName + sql1.toString() + " where " + sql2.toString();
			if (logger.isDebugEnabled())
				logger.debug("Update(" + (System.nanoTime() - start) + "ns) => " + sql);
			sqlMap.put(0, sql);
		} catch (Exception e) {
			logger.error("构造UPDATE语句失败", e);
		}
	}

	private static boolean isEmpty(String[] array) {
		return array == null || array.length == 0;
	}

	private static boolean contains(String[] array, String key) {
		if (isEmpty(array))
			return false;
		for (String str : array) {
			if (str.equals(key))
				return true;
		}
		return false;
	}

	private void buildInsertMap() {
		try {
			long start = System.nanoTime();
			StringBuffer sql1 = new StringBuffer("(");
			StringBuffer sql2 = new StringBuffer("(");
			int j = 1;
			Object obj = null;
			for (String key : columns) {
				obj = getColumnValue(key);
				if (obj != null) {
					sql1.append("`" + key + "`,");
					sql2.append("?,");
					sqlMap.put(j, obj);
					j++;
				}
			}
			sql1.deleteCharAt(sql1.length() - 1);
			sql1.append(")");
			sql2.deleteCharAt(sql2.length() - 1);
			sql2.append(")");
			String sql = "insert into " + this.tableName + sql1.toString() + " values " + sql2.toString();
			if (logger.isDebugEnabled())
				logger.debug("Insert(" + (System.nanoTime() - start) + "ns) => " + sql);
			sqlMap.put(0, sql);
		} catch (Exception e) {
			logger.error("构造Insert语句失败", e);
		}
	}

	public Map<Integer, Object> getSqlMap() {
		return this.sqlMap;
	}

	public boolean handleAutoId() {
		return this.hasAutoId && this.sqlType == SQLType.INSERT;
	}

	public enum SQLType {
		INSERT, UPDATE, UPDATENOTNULL, DELETE, SELECT, SAVEORUPDATE
	}
}
