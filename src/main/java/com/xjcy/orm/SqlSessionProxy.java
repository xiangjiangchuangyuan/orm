package com.xjcy.orm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;

import com.xjcy.orm.event.SqlSession;

public class SqlSessionProxy implements InvocationHandler
{
	private static final Logger logger = Logger.getLogger(SqlSessionProxy.class);

	private SqlSession session;

	public SqlSessionProxy(DefaultSessionImpl session)
	{
		this.session = session;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		long start = System.currentTimeMillis();
		logger.debug("Begin " + method.getName());
		Object result = method.invoke(session, args);
		logger.debug("Finished " + method.getName() + " => " + (System.currentTimeMillis() - start) + "ms");
		return result;
	}

	public SqlSession getProxy()
	{
		return (SqlSession) Proxy.newProxyInstance(session.getClass().getClassLoader(),
				session.getClass().getInterfaces(), this);
	}

}
