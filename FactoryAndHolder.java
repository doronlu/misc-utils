package net.katros.services.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Serves as an MT-safe storage for elements to be retrieved from it by using keys.
 * The {@link #getOrAdd(Object, Supplier)} will add/create an element if none exists for the specified key.
 * An alternative implementation might be to extend {@link ConcurrentHashMap}.
 * 
 * @author doron
 *
 * @param <K>	the key by which to retrieve from the cache.
 * @param <V>	the cached elements
 */
public class FactoryAndHolder<K, V>
{
	private final Map<K, V> map = new HashMap<>();

	/**
	 * If the key is not found or the value associated with it is null then the specified getter will be used to
	 * create a new element.
	 * 
	 * @param key
	 * @param getter	used to get/create an element instance if none is associated with the specified key.
	 * @return	the cached element or the newly generated one if none exists for the specified key.
	 */
	public V getOrAdd(K key, Supplier<V> getter)
	{
		V value = map.get(key);
		if (value != null)
			return value;
		synchronized (map)
		{
			value = map.get(key);
			if (value == null)
			{
				value = getter.get();
				map.put(key, value);
			}
			return value;
		}
	}

	public V remove(K key)
	{
		synchronized (map)
		{
			return map.remove(key);
		}
	}
}