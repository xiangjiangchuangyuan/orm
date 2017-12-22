package com.xjcy.orm.mapper;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.xjcy.orm.core.FieldUtils;

public class TableStruct
{
	private static final Logger logger = Logger.getLogger(TableStruct.class);

	private String name;
	private String generateKey;
	private Map<String, Method> columnMethods;
	private Map<String, Object> columnObjects;
	private Map<Integer, Object> sqlMap = new HashMap<>();

	private Object primaryKey;

	private Object currentObject;

	public TableStruct(String name)
	{
		this.name = name;
	}

	public String getTableName()
	{
		return this.name;
	}

	public Object getPrimaryKey()
	{
		return this.primaryKey;
	}

	public Object getPrimaryKey(Object obj)
	{
		return FieldUtils.getValue(obj, this.generateKey);
	}

	public String getGenerageKey()
	{
		return this.generateKey;
	}

	public boolean hasGenerageKey()
	{
		return this.generateKey != null;
	}

	public void setGenerateKey(String column)
	{
		this.generateKey = column;
	}

	public void setPrimaryKey(Object result)
	{
		if (result != null && generateKey != null)
		{
			FieldUtils.setValue(this.currentObject, generateKey, result);
		}
	}

	public void setColumnMethods(Map<String, Method> methods)
	{
		this.columnMethods = methods;
	}

	public void setColumns(Object obj, SQLType sqlType) throws SQLException
	{
		if (this.columnMethods == null || this.columnMethods.isEmpty())
			return;
		this.currentObject = obj;
		columnObjects = new HashMap<>();
		Set<String> columnNames = this.columnMethods.keySet();
		for (String colName : columnNames)
		{
			columnObjects.put(colName, FieldUtils.getValue(obj, this.columnMethods.get(colName)));
			if (colName.equals(generateKey))
				this.primaryKey = columnObjects.get(colName);
		}
		switch (sqlType)
		{
		case INSERT:
			buildInsertMap();
			break;
		case UPDATE:
			if (this.primaryKey == null)
				throw new SQLException("获取对象" + obj.getClass().getName() + "的主键失败");
			buildUpdateMap(false);
			break;
		case UPDATENOTNULL:
			if (this.primaryKey == null)
				throw new SQLException("获取对象" + obj.getClass().getName() + "的主键失败");
			buildUpdateMap(true);
			break;
		case DELETE:
			if (this.primaryKey == null)
				throw new SQLException("获取对象" + obj.getClass().getName() + "的主键失败");
			buildDeleteMap();
			break;
		default:
			break;
		}
	}

	public Map<Integer, Object> getSqlMap()
	{
		return this.sqlMap;
	}

	private void buildDeleteMap()
	{
		try
		{
			sqlMap.clear();
			StringBuffer sql1 = new StringBuffer();
			Set<String> keys = columnObjects.keySet();
			int j = 1;
			Object obj = null;
			for (String key : keys)
			{
				obj = columnObjects.get(key);
				if (obj != null)
				{
					sql1.append(key + "=? AND ");
					sqlMap.put(j, obj);
					j++;
				}
			}
			if (j > 1)
			{
				sql1.delete(sql1.length() - 5, sql1.length());
			}
			String sql = "delete from " + this.name + " where " + sql1.toString();
			if (logger.isDebugEnabled())
				logger.debug("构造完成的SQL => " + sql);
			sqlMap.put(0, sql);
		}
		catch (Exception e)
		{
			logger.error("构造DELETE语句失败", e);
		}
	}

	private void buildUpdateMap(boolean notNull)
	{
		try
		{
			sqlMap.clear();
			StringBuffer sql1 = new StringBuffer(" set ");
			Set<String> keys = columnObjects.keySet();
			int j = 1;
			if (notNull)
			{
				Object obj = null;
				for (String key : keys)
				{
					obj = columnObjects.get(key);
					if (!key.equals(generateKey) && obj != null)
					{
						sql1.append(key + "=?,");
						sqlMap.put(j, obj);
						j++;
					}
				}
			}
			else
			{
				for (String key : keys)
				{
					if (!key.equals(generateKey))
					{
						sql1.append(key + "=?,");
						sqlMap.put(j, columnObjects.get(key));
						j++;
					}
				}
			}
			sql1.deleteCharAt(sql1.length() - 1);
			StringBuffer sql2 = new StringBuffer(generateKey + " = ?");
			sqlMap.put(j, columnObjects.get(generateKey));
			String sql = "update " + this.name + sql1.toString() + " where " + sql2.toString();
			if (logger.isDebugEnabled())
				logger.debug("构造完成的SQL => " + sql);
			sqlMap.put(0, sql);
		}
		catch (Exception e)
		{
			logger.error("构造UPDATE语句失败", e);
		}
	}

	private void buildInsertMap()
	{
		try
		{
			sqlMap.clear();
			StringBuffer sql1 = new StringBuffer("(");
			StringBuffer sql2 = new StringBuffer("(");
			Set<String> keys = columnObjects.keySet();
			int j = 1;
			Object obj = null;
			for (String key : keys)
			{
				obj = columnObjects.get(key);
				if (!key.equals(this.generateKey) && obj != null)
				{
					sql1.append(key + ",");
					sql2.append("?,");
					sqlMap.put(j, obj);
					j++;
				}
			}
			sql1.deleteCharAt(sql1.length() - 1);
			sql1.append(")");
			sql2.deleteCharAt(sql2.length() - 1);
			sql2.append(")");
			String sql = "insert into " + this.name + sql1.toString() + " values " + sql2.toString();
			if (logger.isDebugEnabled())
				logger.debug("构造完成的SQL => " + sql);
			sqlMap.put(0, sql);
		}
		catch (Exception e)
		{
			logger.error("构造Insert语句失败", e);
		}
	}

	public enum SQLType
	{
		INSERT, UPDATE, UPDATENOTNULL, DELETE, SELECT, SAVEORUPDATE
	}
}
