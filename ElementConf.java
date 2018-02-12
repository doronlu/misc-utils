package net.katros.services.utils.conf;

import static net.katros.services.utils.CollectionUtils.throwingMerger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.katros.services.utils.InvalidArgumentException;
import net.katros.services.utils.InvalidConfigurationException;

/**
 * Holds the configuration of a single element.
 * Note that in the future may also support conf of type Object[].
 * 
 * @author doron
 */
public class ElementConf
{
	private final SortedMap<String, Object> conf;
	private final ElementConf parent;
	private final String keyAtParent; // key at parent

	public ElementConf(Map<String, Object> conf)
	{
		this(conf, null, null);
	}

	/**
	 * 
	 * @param conf
	 * @param parent		may be null.
	 * @param keyAtParent	may be null.
	 */
	public ElementConf(Map<String, Object> conf, ElementConf parent, String keyAtParent)
	{
		if (conf == null)
			throw new IllegalArgumentException("conf is null");
		this.conf = new TreeMap<>(conf);
		this.parent = parent;
		this.keyAtParent = keyAtParent;
	}

// not in use
//	public ElementConf[] getElementConfArray(String key) throws InvalidConfigurationException
//	{
//		return validateElementExists(getElementConfArray(key, null), key);
//	}

	@SuppressWarnings("unchecked")
	public ElementConf[] getElementConfArray(String key, ElementConf[] defaultElementConf) throws InvalidConfigurationException
	{
		Object obj = get(key, null);
		if (obj == null)
			return parent != null ? parent.getElementConfArray(key, defaultElementConf) : defaultElementConf;
		if (obj instanceof List)
		{
			List<Object> list = (List<Object>)obj;
			ElementConf[] confs = new ElementConf[list.size()];
			for (int i = 0; i < confs.length; i++)
				confs[i] = new ElementConf((Map<String, Object>)list.get(i), parent, String.valueOf(i));
			return confs;
		}
		throw new InvalidConfigurationException("Unexpected type: " + obj.getClass());
	}

	/**
	 * From a double or a String.
	 * 
	 * @param key
	 * @return
	 * @throws InvalidConfigurationException
	 */
	public double getDouble(String key) throws InvalidConfigurationException
	{
		try
		{
			Object obj = get(key);
			if (obj instanceof Double)
				return (Double)obj;
			if (obj instanceof String)
				return Double.parseDouble((String)obj);
			throw new InvalidConfigurationException("Invalid type for value associated with key=" + key);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidConfigurationException("An error when processing key=" + key, e);
		}
	}

	public int getInt(String key) throws InvalidConfigurationException
	{
		try
		{
			Object obj = get(key);
			if (obj instanceof Long)
				return ((Long)obj).intValue();
			if (obj instanceof String)
				return Integer.parseInt((String)obj);
			throw new InvalidConfigurationException("Invalid type for value associated with key=" + key);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidConfigurationException("An error when processing key=" + key, e);
		}
	}

	public long getLongFromArray(String key) throws InvalidArgumentException
	{
		List<Long> longs = get(key);
		if (longs.size() != 1)
			throw new InvalidArgumentException("Exactly 1 long is expected but there are " + longs.size());
		return longs.get(0);
	}

	/**
	 * 
	 * @param key
	 * @return	the value associated with the specified key.
	 * @throws InvalidConfigurationException	if the specified key doesn't exist or its value is null.
	 */
	public <V> V get(String key) throws InvalidConfigurationException
	{
		return validateElementExists(get(key, null), key);
	}

	// this method is named deepGet instead of get so that it doesn't conflict with: get(String key, V defaultValue) when V is a String
	public <V> V deepGet(String... keys) throws InvalidConfigurationException
	{
		if (keys.length == 1)
			return validateElementExists(get(keys[0], null), keys[0]);
		String[] restOfKeys = IntStream.range(0, keys.length - 1).mapToObj(i -> keys[i]).toArray(size -> new String[size]);
		return getElementConf(restOfKeys).get(keys[keys.length - 1]);
	}

	/**
	 * The value associated with the specified key, defaultValue if the key is not found.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 * @throws InvalidConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public <V> V get(String key, V defaultValue) throws InvalidConfigurationException
	{
		Object obj = conf.get(key);
		if (obj == null)
			return parent != null ? parent.get(key, defaultValue) : defaultValue;
		try
		{
			return (V)obj;
		}
		catch (ClassCastException e) // not sure this exception can actually happen b/c I suspect V is determined to be the type of obj
		{
			throw new InvalidConfigurationException("An error when processing key=" + key, e);
		}
	}

	/**
	 * Allows for: conf.getElementConf("params", "dependencies")
	 * 
	 * @param keys
	 * @return
	 * @throws InvalidConfigurationException
	 */
	public ElementConf getElementConf(String... keys) throws InvalidConfigurationException
	{
		if (keys.length == 1)
			return validateElementExists(getElementConf(keys[0], null), keys[0]);
		String[] restOfKeys = IntStream.range(1, keys.length).mapToObj(i -> keys[i]).toArray(size -> new String[size]);
		return getElementConf(keys[0]).getElementConf(restOfKeys);
	}

	@SuppressWarnings("unchecked")
	public ElementConf getElementConf(String key, ElementConf defaultElementConf) throws InvalidConfigurationException
	{
		Object obj = get(key, null);
		if (obj == null)
			return parent != null ? parent.getElementConf(key, defaultElementConf) : defaultElementConf;
		if (obj instanceof Map)
			return new ElementConf((Map<String, Object>)obj, this, key);
		throw new InvalidConfigurationException(getInvalidValueMsg(key, obj, Map.class));
	}

	public Map<String, String> getOrderedMapOfStringsFromListOrMap(String key, Map<String, String> defaultMap)
		throws InvalidConfigurationException
	{
		try
		{
			Map<String, Object> map = getOrderedMapFromListOrMap(key, null);
			return map != null
				? map.entrySet().stream().collect(Collectors.toMap(
					Map.Entry::getKey, entry -> (String)entry.getValue(), throwingMerger(), LinkedHashMap::new)) // using LinkedHashMap to keep the order
				: defaultMap;
		}
		catch (ClassCastException e)
		{
			throw new InvalidConfigurationException("value for key '" + key + "' should contain a map to Strings", e);
		}
	}

	public Map<String, List<Long>> getOrderedMapOfListsOfLongs(String key) throws InvalidConfigurationException
	{
		return validateElementExists(getOrderedMapOfListsOfLongs(key, null), key);
	}

	public Map<String, List<Long>> getOrderedMapOfListsOfLongs(String key, Map<String, List<Long>> defaultMap)
		throws InvalidConfigurationException
	{
		Map<String, Object> map = getOrderedMapFromListOrMap(key, null);
		if (map == null)
			return defaultMap;
		Map<String, List<Long>> outMap = new LinkedHashMap<>(); // using LinkedHashMap to keep the order
		for (Map.Entry<String, Object> entry : map.entrySet())
			outMap.put(entry.getKey(), getValueAsListOfLongs(entry.getValue(), key));
		return outMap;
	}

	@SuppressWarnings("unchecked")
	private List<Long> getValueAsListOfLongs(Object obj, String key) throws InvalidConfigurationException
	{
		try
		{
			if (obj instanceof Long)
			{
				List<Long> list = new ArrayList<>();
				list.add((Long)obj);
				return list;
			}
			return (List<Long>)obj;
		}
		catch (ClassCastException e)
		{
			throw new InvalidConfigurationException("value for key '" + key + "' should contain a map to either Long or List<Long>s", e);
		}
	}

	public <T> Map<String, T> getMapToType(String key, Map<String, T> defaultMap, Class<T> clazz)
		throws InvalidConfigurationException
	{
		try
		{
			Map<String, Object> map = getOrderedMapFromListOrMap(key, null);
			return map != null
				? map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> clazz.cast(entry.getValue())))
				: defaultMap;
		}
		catch (ClassCastException e)
		{
			throw new InvalidConfigurationException("value for key '" + key + "' should contain a map to " + clazz.getSimpleName(), e);
		}
	}

	public Map<String, Object> getOrderedMapFromListOrMap(String key, Map<String, Object> defaultMap)
		throws InvalidConfigurationException
	{
		Object obj = get(key, null);
		if (obj == null)
			return parent != null ? parent.getOrderedMapFromListOrMap(key, defaultMap) : defaultMap;
		if ((obj instanceof Map<?, ?>))
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> tmpMap = (Map<String, Object>)obj;
			Map<String, Object> sortedMap = new TreeMap<>(tmpMap);
			return sortedMap;
		}
		if (obj instanceof List<?>)
		{
			List<?> keyValuesList = (List<?>)obj;
			if (keyValuesList.size() % 2 == 1)
				throw new InvalidConfigurationException("'" + key + "' should contain an even number of elements");
			Map<String, Object> orderedMap = new LinkedHashMap<>();
			Iterator<?> it = keyValuesList.iterator();
			while (it.hasNext())
			{
				Object o = it.next();
				if (!(o instanceof String))
					throw new InvalidConfigurationException(
						"Object " + o + " is expected to be of type String but is of type " + o.getClass());
				orderedMap.put((String)o, it.next());
			}
			return orderedMap;
		}
		throw new InvalidConfigurationException(getInvalidValueMsg(key, obj, List.class, Map.class));
	}

	private static <T> T validateElementExists(T t, String key) throws InvalidConfigurationException
	{
		if (t != null)
			return t;
		throw new InvalidConfigurationException("Either key '" + key + "' is missing or its value is null");
	}

	private static String getInvalidValueMsg(String key, Object obj, Class<?>... expectedClasses)
	{
		String classNames = Stream.of(expectedClasses).map(Class::getTypeName).collect(Collectors.joining(", ")); // StringJoiner also an option
		return "Value associated with key '" + key + "' is '" + obj + "' which is of type " + obj.getClass()
			+ " while one of [" + classNames + "] is expected";
	}

	public String getKeyAtParent()
	{
		return keyAtParent;
	}

	@Override
	public String toString()
	{
		return conf.toString();
	}
}

/*
/**
 * Not in use at the moment, consider removing.
 * Referring key is of the format: <something>#ids
 * 
 * @return
 * @throws InvalidConfigurationException
 *
@Deprecated
public String getReferringKey() throws InvalidConfigurationException
{
	List<String> referringKeys = conf.keySet().stream().filter(key -> key.endsWith("#ids")).collect(Collectors.toList());
	if (referringKeys.size() != 1)
		throw new InvalidConfigurationException("Exactly 1 key that ends with '#ids' is expected but there are " + referringKeys.size());
	return referringKeys.get(0);
}

public Map<String, String> getMapOfStrings(String key) throws InvalidConfigurationException
{
	return validateElementExists(getMap(key, (Map<String, String>)null), key);
}

public Map<String, Object> getMap(String key) throws InvalidConfigurationException
{
	return validateElementExists(getMap(key, null), key);
}

@SuppressWarnings("unchecked")
public <K, V> Map<K, V> getMap(String key, Map<K, V> defaultMap) throws InvalidConfigurationException
{
	Object obj = conf.get(key);
	if (obj == null)
		return parent != null ? parent.getMap(key, defaultMap) : defaultMap;
	if ((obj instanceof Map<?, ?>))
		return (Map<K, V>)obj;
	throw new InvalidConfigurationException(getInvalidValueMsg(key, obj, Map.class));
}
*/