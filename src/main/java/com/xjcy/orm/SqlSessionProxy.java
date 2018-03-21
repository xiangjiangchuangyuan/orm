package com.xjcy.orm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.xjcy.orm.event.SqlSession;

public class SqlSessionProxy implements InvocationHandler
{

	private SqlSession session;

	public SqlSessionProxy(DefaultSessionImpl session)
	{
		this.session = session;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		System.out.println("before");
		Object result = method.invoke(session, args);
		System.out.println("after");
		return result;
	}

	public SqlSession getProxy()
	{
		return (SqlSession) Proxy.newProxyInstance(session.getClass().getClassLoader(),
				session.getClass().getInterfaces(), this);
	}

}
