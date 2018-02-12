package net.katros.services.utils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utilities that deal with {@link Collection}s but treat collection members that are themselves {@link Collection}s in
 * a special way.
 * 
 * @author doron
 */
public abstract class DeepCollectionUtils
{
	public static final String[] INDENTS
		= {
			"",
			"  ",
			"    ",
			"      ",
			"        ",
			"          ",
			"            ",
			"              ",
			"                ",
			"                  ",
			"                    "
		  };

	public static StringBuilder indent(StringBuilder sb, String pre, int depth, String post)
	{
		sb.append(pre);
		return indent(sb, depth, post);
	}

	public static StringBuilder indent(StringBuilder sb, int depth, String str)
	{
		return indent(sb, depth).append(str);
	}

	public static StringBuilder indent(StringBuilder sb, int depth)
	{
		if (depth < INDENTS.length)
			sb.append(INDENTS[depth]);
		else
			for (int i = 1; i <= depth; i++)
				sb.append("  ");
		return sb;
	}

	/**
	 * Merges map2 into map1, deep merge for {@link Map}s.
	 * The difference between this method and {@link Map#putAll(Map)} is that here if a received key is present then if
	 * the value is itself a {@link Map} then instead of the value (a map) replacing the existing value (a map) in the
	 * destination map the two maps are merged (deeply!). If a value at any level is not a map then the value from map2
	 * replaces the one from map1.
	 * 
	 * @param map1	the destination map, must be mutable.
	 * @param map2
	 */
	public static <K, V> void deepMergeMaps(Map<K, V> map1, Map<K, V> map2)
	{
		for (K key : map2.keySet())
		{
			V map1ValObj = map1.get(key);
			V map2ValObj = map2.get(key);
			if (map1ValObj == null || !(map1ValObj instanceof Map && map2ValObj instanceof Map))
				map1.put(key, map2ValObj);
			else
			{
				@SuppressWarnings("unchecked")
				Map<K, V> map1Val = (Map<K, V>)map1ValObj;
				@SuppressWarnings("unchecked")
				Map<K, V> map2Val = (Map<K, V>)map2ValObj;
				deepMergeMaps(map1Val, map2Val);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <K, V> void deepRemoveNulls(Map<K, V> map)
	{
		Iterator<Entry<K, V>> it = map.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<K, V> entry = it.next();
			V value = entry.getValue();
			if (value == null)
				it.remove();
			else if (value instanceof Map<?, ?>)
				deepRemoveNulls((Map<K, V>)value);
		}
	}

	/**
	 * Based in part on {@link AbstractMap#toString()}.
	 */
	public static <K, V> String mapToString(Map<K, V> map)
	{
		Iterator<Entry<K,V>> i = map.entrySet().iterator();
		if (!i.hasNext())
			return "{}";
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (;;)
		{
			Entry<K,V> e = i.next();
			sb.append(e.getKey()).append(' ');
			V value = e.getValue();
			if (value instanceof String)
				sb.append('\"').append(value).append('\"');
			else if (value instanceof Map)
				sb.append(mapToString((Map<?, ?>)value));
			else if (value instanceof List)
				sb.append(listToString((List<?>)value));
			else
				sb.append(value);
			if (!i.hasNext())
				return sb.append('}').toString();
			sb.append(',').append(' ');
		}
	}

	public static <E> String listToString(List<E> list)
	{
		Iterator<E> it = list.iterator();
		if (!it.hasNext())
			return "[]";
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (;;)
		{
			E e = it.next();
			if (e instanceof String)
				sb.append('\"').append(e).append('\"');
			else if (e instanceof List)
				sb.append(listToString((List<?>)e));
			else if (e instanceof Map)
				sb.append(mapToString((Map<?, ?>)e));
			else
				sb.append(e);
			if (!it.hasNext())
				return sb.append(']').toString();
			sb.append(' ');
		}
	}

	public static void main(String... args)
	{
//		{:a {:b 22 :c 33}  :b 2  :e 7} {:a {:a 11 :c 333 :f 666}  :b 98  :c 0}
//		=> {:c 0, :a {:f 666, :a 11, :c 333, :b 22}, :b 98, :e 7}
		Map<String, Object> map1 = new HashMap<>();
		Map<String, Integer> map1inner = new HashMap<>();
		map1inner.put("b", 22);
		map1inner.put("c", 33);
		map1.put("a", map1inner);
		map1.put("b", 2);
		map1.put("e", 7);
		Map<String, Object> map2 = new HashMap<>();
		Map<String, Integer> map2inner = new HashMap<>();
		map2inner.put("a", 11);
		map2inner.put("c", 333);
		map2inner.put("f", 666);
		map2.put("a", map2inner);
		map2.put("b", 98);
		map2.put("c", 0);
		System.out.println(map1);
		System.out.println(map2);
		deepMergeMaps(map1, map2);
		System.out.println(map1);
	}
}