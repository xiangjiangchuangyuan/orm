package com.xjcy.orm.core;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xjcy.orm.mapper.TableStruct;
import com.xjcy.util.StringUtils;

public class EntityUtils
{
	private static final Logger logger = Logger.getLogger(EntityUtils.class);

	private static List<Class<?>> scanEntities(String pkg)
	{
		List<Class<?>> classes = new ArrayList<>();
		URL url = EntityUtils.class.getClassLoader().getResource(pkg.replace(".", "/"));
		if (url == null)
		{
			logger.error("Entity package '" + pkg + "' not found");
			return classes;
		}
		if ("file".equals(url.getProtocol()))
		{
			try
			{
				String entityPath = URLDecoder.decode(url.getFile(), "UTF-8");
				File file = new File(entityPath);
				if (file.isDirectory())
				{
					File[] files = file.listFiles();
					for (File file2 : files)
					{
						classes.add(getClass(pkg, file2.getName()));
					}
				}
			}
			catch (UnsupportedEncodingException | ClassNotFoundException e)
			{
				logger.error("获取entity路径失败", e);
			}
		}
		return classes;
	}

	private static List<Class<?>> splitEntities(String _entities)
	{
		List<Class<?>> classes = new ArrayList<>();
		Class<?> cla;
		if (_entities.contains(","))
		{
			String[] strs = _entities.split(",");
			for (String str : strs)
			{
				cla = getClass(str);
				if (cla != null)
					classes.add(cla);
			}
			return classes;
		}
		cla = getClass(_entities);
		if (cla != null)
			classes.add(cla);
		return classes;
	}

	private static Class<?> getClass(String str)
	{
		try
		{
			return Class.forName(str);
		}
		catch (ClassNotFoundException e)
		{
			logger.error("Entity class '" + str + "' not found");
			return null;
		}
	}

	private static Class<?> getClass(String pkg, String fileName) throws ClassNotFoundException
	{
		return Class.forName(pkg + "." + fileName.replace(".class", ""));
	}

	public static void cacheEntities(String entities)
	{
		if (StringUtils.isEmpty(entities))	
		{
			if (logger.isDebugEnabled())
				logger.debug("The project.entities attribute was not found");
			return;
		}

		List<Class<?>> classes;
		if (entities.endsWith(".*"))
			classes = scanEntities(entities.substring(0, entities.length() - 2));
		else
			classes = splitEntities(entities);
		TableStruct struct;
		for (Class<?> cla : classes)
		{
			struct = ReflectJPA.loadTable(cla);
			if (struct != null)
			{
				SqlCache.addEntity(cla.getName(), struct);
				if (logger.isDebugEnabled())
					logger.debug("Load entity '" + cla.getName() + "'");
			}
		}
	}
}
