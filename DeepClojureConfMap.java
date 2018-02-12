package net.katros.services.clojure;

import static net.katros.services.utils.DeepCollectionUtils.indent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import clojure.lang.Keyword;
import net.katros.services.utils.DeepCollectionUtils;

/**
 * A {@link TreeMap} that employs 'deep' operations on values that are {@link Map}s or {@link List}s.
 * 
 * Note that when putting elements that are maps or lists we should first transform them to {@link DeepClojureConfMap} and
 * {@link DeepClojureConfList}. This is to prevent values that are for example immutable maps which in turn may cause
 * failure of data modifying operations.
 * 
 * The class is not complete in the sense that not all possibly needed methods have been implemented.
 * 
 * @author doron
 *
 * @param <K>
 * @param <V>
 */
public class DeepClojureConfMap<K extends Keyword, V> extends TreeMap<K, V> // LinkedHashMap<K, V>
{
	private static final long serialVersionUID = 1L;

	public DeepClojureConfMap() {}

	public DeepClojureConfMap(Comparator<? super K> comparator)
	{
		super(comparator);
	}

	public DeepClojureConfMap(Map<? extends K, ? extends V> inMap)
	{
		this.putAll(inMap);
	}

	/**
	 * A c'tor that deeply creates {@link DeepClojureConfList}s and {@link DeepClojureConfMap}s from {@link List} and
	 * {@link Map} values.
	 * For example if inMap has values that are lists that have values that are maps then for each of those lists and
	 * maps a {@link DeepClojureConfList} or a {@link DeepClojureConfMap} will be created respectively.
	 * 
	 * @param inMap
	 * @param comparator	used also when creating inner {@link DeepClojureConfMap}s, may be null.
	 */
	public DeepClojureConfMap(Map<? extends K, ? extends V> inMap, Comparator<? super K> comparator)
	{
		super(comparator);
		this.putAll(inMap);
	}

	/**
	 * A convenience method.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public V put(String key, V value)
	{
		return put((K)Keyword.intern(key), value);
	}

	/**
	 * A deep put that creates {@link DeepClojureConfList}s and {@link DeepClojureConfMap}s where needed. See c'tor comment.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value)
	{
		if (value instanceof DeepClojureConfMap<?, ?> || value instanceof DeepClojureConfList<?>)
			return super.put(key, value);
		if (value instanceof Map<?, ?>)
			return super.put(key, (V)new DeepClojureConfMap<>((Map<? extends Keyword, ?>)value, (Comparator<? super Keyword>)comparator())); // note that the inner map doesn't have to be of the same types as this map (this map's types are K,V)
		if (value instanceof List<?>)
			return super.put(key, (V)new DeepClojureConfList<>((List<?>)value, (Comparator<? super Keyword>)comparator()));
		return super.put(key, value);
	}

	/**
	 * A deep put that creates {@link DeepClojureConfList}s and {@link DeepClojureConfMap}s where needed. See c'tor comment.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void putAll(Map<? extends K, ? extends V> map)
	{
		for (Map.Entry<? extends K, ? extends V> entry : map.entrySet())
		{
			Object o = entry.getKey();
			try
			{
				if (o instanceof Keyword)
					put(entry.getKey(), entry.getValue());
				else if (o instanceof String) // attempt to support the case where Strings are used as Keywords (the ':' is missing)
					put((K)Keyword.intern((String)o), entry.getValue());
				else
					put(entry.getKey(), entry.getValue());
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Exception while processing key=" + String.valueOf(o) + " value=" + entry.getValue(), e);
			}
		}
	}

	/**
	 * The difference between this method and {@link #putAll(Map)} is that here if a received key is present then if the
	 * value is itself a {@link Map} then instead of the value (a map) replacing the existing value (a map) the two maps
	 * are merged (deeply!). If a value at any level is not a map then the value from specified map replaces the one
	 * from this map.
	 * 
	 * @param map
	 */
	public void deepMerge(Map<? extends K, ? extends V> map)
	{
		DeepCollectionUtils.deepMergeMaps(this, new DeepClojureConfMap<>(map, comparator()));
	}

	/**
	 * Removes null values, also in values that are {@link Map}s.
	 * Note that for not doesn't remove nulls in {@link List}s.
	 */
	public void deepRemoveNulls()
	{
		DeepCollectionUtils.deepRemoveNulls(this);
	}

	/**
	 * A convenience method.
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <W> W get(String key)
	{
		return (W)get(Keyword.intern(key));
	}

	/**
	 * A convenience method.
	 * 
	 * @param key
	 * @return
	 */
	public V remove(String key)
	{
		return remove(Keyword.intern(key));
	}

	/**
	 * A convenience method for when the value is a {@link DeepClojureConfMap} and of the same types (K,V).
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DeepClojureConfMap<K, V> getMap(String key)
	{
		return (DeepClojureConfMap<K, V>)get(Keyword.intern(key));
	}

	public DeepClojureConfList<?> getList(String key)
	{
		return (DeepClojureConfList<?>)get(Keyword.intern(key));
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb, 0, true);
		return sb.toString();
	}

	/**
	 * 
	 * @param sb
	 * @param depth
	 * @param isSuccinctList	lists that don't contain any map or list will be presented in a single line.
	 */
	void toString(StringBuilder sb, int depth, boolean isSuccinctLists)
	{
		if (depth > 0) // just so that the first line in the resulting file is not empty
			sb.append('\n');
		indent(sb, depth, "{");
		Set<Map.Entry<K, V>> entries = entrySet();
		for (Map.Entry<K, V> entry : entries)
		{
			indent(sb, "\n", depth + 1, entry.getKey().toString());
			V value = entry.getValue();
			if (value instanceof DeepClojureConfMap<?, ?>)
				((DeepClojureConfMap<?, ?>)value).toString(sb, depth + 1, isSuccinctLists);
			else if (value instanceof DeepClojureConfList<?>)
				((DeepClojureConfList<?>)value).toString(sb, depth + 1, isSuccinctLists);
			else if (value instanceof String)
				sb.append(" \"" + value + "\"");
			else
				sb.append(" " + value);
		}
		indent(sb, "\n", depth, "}");
	}
}