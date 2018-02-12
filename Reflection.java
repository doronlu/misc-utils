package net.katros.strategies.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.katros.services.utils.InvalidConfigurationException;
import net.katros.strategies.config.EntityConf;
import net.katros.strategies.events.OptionalConf;
import net.katros.strategies.events.RequiredConf;

/**
 * Shallow reflection of an object's fields.
 *  
 * @author doron
 */
public abstract class Reflection
{
	public static NavigableMap<String, String> getDeclaredFieldsValues(Object o)
	{
		NavigableMap<String, String> map = new TreeMap<>();
		for (Field field : o.getClass().getDeclaredFields())
			map.put(field.getName(), getFieldValue(o, field));
		return map;
	}

	private static String getFieldValue(Object o, Field field)
	{
		try
		{
			boolean isAccessible = field.isAccessible();
			field.setAccessible(true);
			Object fieldObject = field.get(o);
			field.setAccessible(isAccessible);
			if (fieldObject == null)
				return String.valueOf((Object)null);
			Class<?> clazz = fieldObject.getClass();
			if (!clazz.isArray())
				return fieldObject.toString();
			if (clazz == double[].class)
				return Arrays.toString((double[])fieldObject);
			if (clazz == int[].class)
				return Arrays.toString((int[])fieldObject);
			if (clazz == long[].class)
				return Arrays.toString((long[])fieldObject);
			if (clazz == boolean[].class)
				return Arrays.toString((boolean[])fieldObject);
			if (clazz == float[].class)
				return Arrays.toString((float[])fieldObject);
			if (clazz == char[].class)
				return Arrays.toString((char[])fieldObject);
			if (clazz == byte[].class)
				return Arrays.toString((byte[])fieldObject);
			if (clazz == short[].class)
				return Arrays.toString((short[])fieldObject);
			return Arrays.deepToString((Object[])fieldObject);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			return "Exception while reflection: " + e.getMessage();
		}		
	}

// This one works too:
//	private static String getFieldValue2(Object o, Field field)
//	{
//		try
//		{
//			boolean isAccessible = field.isAccessible();
//			field.setAccessible(true);
//			Object fieldObject = field.get(o);
//			field.setAccessible(isAccessible);
//			if (fieldObject == null)
//				return String.valueOf((Object)null);
//			Class<?> arrayComponentType = fieldObject.getClass().getComponentType();
//			if (arrayComponentType == null)
//				return String.valueOf(fieldObject);
//			if (arrayComponentType == Double.TYPE)
//				return Arrays.toString((double[])fieldObject);
//			if (arrayComponentType == Integer.TYPE)
//				return Arrays.toString((int[])fieldObject);
//			if (arrayComponentType == Long.TYPE)
//				return Arrays.toString((long[])fieldObject);
//			if (arrayComponentType == Boolean.TYPE)
//				return Arrays.toString((boolean[])fieldObject);
//			if (arrayComponentType == Float.TYPE)
//				return Arrays.toString((float[])fieldObject);
//			if (arrayComponentType == Character.TYPE)
//				return Arrays.toString((char[])fieldObject);
//			if (arrayComponentType == Byte.TYPE)
//				return Arrays.toString((byte[])fieldObject);
//			if (arrayComponentType == Short.TYPE)
//				return Arrays.toString((short[])fieldObject);
//			return Arrays.deepToString((Object[])fieldObject);
//		}
//		catch (IllegalArgumentException | IllegalAccessException e)
//		{
//			return "Exception while reflection: " + e.getMessage();
//		}		
//	}

	/**
	 * Examples:
	@OptionalConf(defaultDouble=3) private double something;
	@OptionalConf private double somethingElse;
	@RequiredConf private String espClassName;
	@OptionalConf(defaultString="null") private String someString;
	 */
	public static void loadFieldsFromEntityConf(Object o, EntityConf conf) throws InvalidConfigurationException
	{
		for (Field field : o.getClass().getDeclaredFields())
		{
			if (field.isAnnotationPresent(OptionalConf.class))
			{
				OptionalConf optionalConf = field.getAnnotation(OptionalConf.class);
				boolean isAccessible = field.isAccessible();
				field.setAccessible(true);
				Class<?> type = field.getType();
				String name = optionalConf.name();
				if (name.isEmpty())
					name = field.getName();
				try
				{
					if (type == Double.TYPE || type == Double.class)
						field.set(o, conf.getDouble(name, optionalConf.defaultDouble()));
					else if (type == Integer.TYPE || type == Integer.class)
						field.set(o, conf.getInt(name, optionalConf.defaultInt()));
					else if (type == Boolean.TYPE || type == Boolean.class)
						field.set(o, conf.getBoolean(name, optionalConf.defaultBoolean()));
					else if (type == String.class)
						field.set(o, conf.getString(name, optionalConf.defaultString()));
					field.setAccessible(isAccessible);
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					throw new InvalidConfigurationException(e);
				}		
			}
			else if (field.isAnnotationPresent(RequiredConf.class))
			{
				RequiredConf requiredConf = field.getAnnotation(RequiredConf.class);
				boolean isAccessible = field.isAccessible();
				field.setAccessible(true);
				Class<?> type = field.getType();
				String name = requiredConf.name();
				if (name.isEmpty())
					name = field.getName();
				try
				{
					if (type == Double.TYPE || type == Double.class)
						field.set(o, conf.getDouble(name));
					else if (type == Long.TYPE || type == Long.class)
						field.set(o, conf.getLong(name));
					else if (type == Integer.TYPE || type == Integer.class)
						field.set(o, conf.getInt(name));
					else if (type == Boolean.TYPE || type == Boolean.class)
						field.set(o, conf.getBoolean(name));
					else if (type == String.class)
						field.set(o, conf.getString(name));
					else if (type == String[].class)
						field.set(o, conf.getStringsArray(name));
					else if (type == List.class)
						field.set(o, conf.getListGenerics(name));
					else if (type == double[].class)
						field.set(o, conf.getArrayOfDoubles(name));
					field.setAccessible(isAccessible);
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					throw new InvalidConfigurationException(e);
				}		
			}
		}
	}
}