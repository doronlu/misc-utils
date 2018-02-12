package net.katros.services.clojure;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clojure.lang.Keyword;
import net.katros.services.utils.conf.ElementConf;

/**
 * Enables reading a Clojure file from disk or using a String and creating a
 * {@link TradeEngineConf} from it.
 * 
 * @author doron
 */
public abstract class ClojureConfigReader
{
	public static Map<String, Object> getConfAsMap(String filename) throws FileNotFoundException
	{
		Map<?, Object> clojureMap = getClojureMapFromClojureFile(filename, false);
		return convertMapKeyTypeRecursive(clojureMap);
	}

	/**
	 * 
	 * @param filename
	 * @param isPerformEval
	 * @return
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <K> Map<K, Object> getClojureMapFromClojureFile(String filename, boolean isPerformEval)
		throws FileNotFoundException
	{
		Object clojureObject = deserializeClojureObjectFromFile(filename, isPerformEval);
		return (Map<K, Object>)clojureObject;
	}

	/**
	 * Reads the specified file from disk and returns a Clojure object representation.
	 * 
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 */
	static Object deserializeClojureObjectFromFile(String filename, boolean isPerformEval)
		throws FileNotFoundException
	{
		String str = isPerformEval ? "(load-file \"" + filename + "\")" : "(read-string (slurp \"" + filename + "\"))";
		return Clojure.eval(str); // even though not declared this may throw FileNotFoundException
	}

	/**
	 * Creates a Clojure object from the specified String.
	 * 
	 * @param str
	 * @return
	 */
	static Object deserializeClojureObjectFromString(String str)
	{
		String full = "(load-string \"" + str + "\")"; // will do evaluation
//		String full = "(read-string \"" + str + "\")"; // won't do evaluation
		return Clojure.eval(full);
	}

	/**
	 * Creates a Clojure map based on the specified map but any <code>Keyword</code>s are substituted by
	 * <code>String</code>s.
	 *
	 * @param inMap
	 * @return
	 */
	@SuppressWarnings("unchecked") // don't know of a way around this
	public static <K> Map<String, Object> convertMapKeyTypeRecursive(Map<K, Object> inMap)
	{
		Map<String, Object> outMap = new HashMap<>();
		for (Map.Entry<K, Object> entry : inMap.entrySet())
		{
			Object value = entry.getValue();
			Object newValue;
			if (value instanceof Map<?, ?>)
				newValue = convertMapKeyTypeRecursive((Map<K, Object>)value);
			else if (value instanceof List<?>)
				newValue = convertListKeyTypeRecursive((List<Object>)value);
			else
				newValue = value;
			outMap.put(keyToString(entry.getKey()), newValue);
		}
		return outMap;
	}

	/**
	 * Creates a Clojure list based on the specified list but any <code>Keyword</code>s are substituted by
	 * <code>String</code>s.
	 *
	 * @param inList
	 * @return
	 */
	@SuppressWarnings("unchecked") // don't know of a way around this
	public static <K> List<Object> convertListKeyTypeRecursive(List<Object> inList)
	{
		List<Object> outList = new ArrayList<>();
		for (Object o : inList)
		{
			Object newElement;
			if (o instanceof List<?>)
				newElement = convertListKeyTypeRecursive((List<Object>)o);
			else if (o instanceof Map<?, ?>)
				newElement = convertMapKeyTypeRecursive((Map<K, Object>)o);
			else if (o instanceof Keyword)
				newElement = ((Keyword)o).getName();
			else
				newElement = o;
			outList.add(newElement);
		}
		return outList;
	}

	private static <K> String keyToString(K key)
	{
		if (key instanceof Keyword)
			return ((Keyword)key).getName();
		return key.toString();
	}

	public static Map<String, Object> deserializeMapFromString(String str)
	{
		return deserializeMapFromEscapedString(str.replaceAll("\"", "\\\\\""));
	}

	public static Map<String, Object> deserializeMapFromEscapedString(String str)
	{
		Object o = deserializeClojureObjectFromString(str);
		@SuppressWarnings("unchecked")
		Map<Keyword, Object> map = (Map<Keyword, Object>)o;
		return convertMapKeyTypeRecursive(map);
	}

	/**
	 * A convenience method for creating ElementConf from a String holding a Clojure edn.
	 * 
	 * @param confStr
	 * @return
	 */
	public static ElementConf toElementConf(String confStr)
	{
		return new ElementConf(deserializeMapFromString(confStr));
	}

	public static void main(String... args)
	{
		Object o = deserializeClojureObjectFromString("{ :params { :instrumentId 33 } }");
		System.out.println(o);
		o = deserializeClojureObjectFromString("{ :params { :instrumentId \\\"gold\\\" } }");
		System.out.println(o);
		o = deserializeMapFromEscapedString("{ :params { :instrumentId \\\"gold\\\" } }");
		System.out.println(o);
		o = deserializeMapFromString("{ :params { :instrumentId \"gold\" } }");
		System.out.println(o);
	}
}