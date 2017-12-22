package com.xjcy.orm.mapper;

public class ProcParamater
{
	private final int sort;
	private Object value = null;
	private final ParameterType parameterType;

	public ProcParamater(int sort, String value)
	{
		this.sort = sort;
		this.value = value;
		this.parameterType = ParameterType.IN;
	}

	public ProcParamater(int sort)
	{
		this.sort = sort;
		this.parameterType = ParameterType.OUT;
	}

	public int getInt()
	{
		return Integer.parseInt(this.value.toString());
	}

	public Object getValue()
	{
		return this.value;
	}

	public String getString()
	{
		return this.value.toString();
	}

	public int getSort()
	{
		return this.sort;
	}

	public ParameterType getParameterType()
	{
		return this.parameterType;
	}

	public void setValue(Object obj)
	{
		this.value = obj;
	}

	/**
	 * 参数类型
	 * 
	 * @author YYDF
	 *
	 */
	public enum ParameterType
	{
		IN, OUT, INOUT
	}
}
