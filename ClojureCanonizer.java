package net.katros.services.clojure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import clojure.lang.IPersistentMap;
import clojure.lang.PersistentTreeMap;
import clojure.lang.PersistentVector;

/**
 * Knows how to build a canonical Clojure {@link IPersistentMap}, from a Clojure
 * map, and a canonical Clojure {@link PersistentVector} from a Clojure List.
 * By canonical we mean that an iteration over its members (even a deep
 * iteration that includes the map members that are themselves Clojure maps and
 * lists, to any level of nesting) will return the elements lexicographically
 * ordered.
 * Lists are touched recursively only if only if isListCanonizedRecursively is set to true.
 * This works both when using Clojure Keywords and when not.
 * 
 * @author doron
 */
public abstract class ClojureCanonizer
{
	static final Comparator<Object> CLOJURE_OBJ_ORDER
		= new Comparator<Object>()
		{
			public int compare(Object obj1, Object obj2)
			{
				return obj1.toString().compareTo(obj2.toString());
			}
		};

	/**
	 * Builds a canonical {@link IPersistentMap} from the values in the
	 * specified map.
	 * Inner lists (appearing as values at the map) are touched only if isListCanonizedRecursively is set to true.
	 * 
	 * @param map
	 * @param isListCanonizedRecursively
	 * @return
	 */
	public static <K> IPersistentMap canonize(Map<K, ?> map, boolean isListCanonizedRecursively)
	{
		SortedMap<K, Object> sortedMap = new TreeMap<>(map);
		for (Entry<K, Object> entry : sortedMap.entrySet())
		{
			K key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Map<?, ?>)
			{
				@SuppressWarnings("unchecked") // no way around this
				Map<K, ?> mapValue = (Map<K, ?>)value;
				IPersistentMap canonicalMap = canonize(mapValue, isListCanonizedRecursively);
				sortedMap.put(key, canonicalMap);
			}
			else if (value instanceof List<?>)
			{
				if(isListCanonizedRecursively) 
				{
					List<?> listValue = (List<?>)value;
					PersistentVector persistentVector = canonize(listValue, isListCanonizedRecursively);
					sortedMap.put(key, persistentVector);
				}
			}
		}
		return PersistentTreeMap.create(sortedMap);
	}

	/**
	 * Builds a canonical {@link PersistentVector} from the values in the
	 * specified list.
	 * Inner lists are touched only if isListCanonizedRecursively is set to true.
	 * 
	 * @param list
	 * @param isListCanonizedRecursively
	 * @return
	 */
	public static <K> PersistentVector canonize(List<?> list, boolean isListCanonizedRecursively)
	{
		List<Object> newList = new ArrayList<>();
		for (Object value : list)
		{
			if (value instanceof Map<?, ?>)
			{
				@SuppressWarnings("unchecked") // no way around this
				Map<K, ?> mapValue = (Map<K, ?>)value;
				IPersistentMap canonicalMap = canonize(mapValue, isListCanonizedRecursively);
				newList.add(canonicalMap);
			}
			else if (value instanceof List<?>)
			{
				if(isListCanonizedRecursively) 
				{
					List<?> listValue = (List<?>)value;
					PersistentVector pv = canonize(listValue, isListCanonizedRecursively);
					newList.add(pv);
				}
			}
			else
				newList.add(value);
		}
		Collections.sort(newList, CLOJURE_OBJ_ORDER);
		PersistentVector persistentVector = PersistentVector.create(newList);
		return persistentVector;
	}
}