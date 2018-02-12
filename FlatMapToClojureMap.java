package net.katros.services.clojure;

import java.util.HashMap;
import java.util.Map;

import clojure.lang.Keyword;

/**
 * Transforms a flat map<String,String> to a deep (a value may itself be a map) Clojure map, for example:
{esp.execution.closeDelta=2, esp.execution.openDelta=1, esp.windingDownPosMgr.dailyStopLossLimit=3200, esp.preExecution.maxOrderSize=4}
 * to:
{:esp={:execution={:closeDelta=2, :openDelta=1}, :preExecution={:maxOrderSize=4}, :windingDownPosMgr={:dailyStopLossLimit=3200}}}
 * 
 * @author doron
 */
public class FlatMapToClojureMap
{
	private final String separator;
	private final Map<Keyword, Object> outMap = new HashMap<>();

	public static Map<Keyword, Object> transform(Map<String, String> inMap, String separator)
	{
		FlatMapToClojureMap flatMapToClojureMap = new FlatMapToClojureMap(separator);
		return flatMapToClojureMap.transform(inMap);
	}

	private FlatMapToClojureMap(String separator)
	{
		if (separator.equals("."))
			separator = "\\.";
		this.separator = separator;
	}

	private Map<Keyword, Object> transform(Map<String, String> inMap)
	{
		inMap.entrySet().stream().forEach(entry -> add(entry));
		return outMap;
	}

	private void add(Map.Entry<String, String> entry)
	{
		String[] parts = entry.getKey().split(separator);
		addToNestedMap(outMap, parts, 0, entry.getValue());
	}

	private static void addToNestedMap(Map<Keyword, Object> map, String[] parts, int fromIndex, String value)
	{
		Keyword keyword = Keyword.intern(parts[fromIndex]);
		if (fromIndex == parts.length - 1)
			map.put(keyword, value);
		else
		{
			@SuppressWarnings("unchecked")
			Map<Keyword, Object> nestedMap = (Map<Keyword, Object>)map.get(keyword);
			if (nestedMap == null)
			{
				nestedMap = new HashMap<>();
				map.put(keyword, nestedMap);
			}
			addToNestedMap(nestedMap, parts, fromIndex + 1, value);
		}
	}

	/**
	 * Result:
{:esp={:execution={:closeDelta=2, :openDelta=1}, :preExecution={:maxOrderSize=4}, :windingDownPosMgr={:dailyStopLossLimit=3200}}}
	 * @param args
	 */
	public static void main(String... args)
	{
		Map<String, String> inMap = new HashMap<>();
		inMap.put("esp.execution.openDelta", "1");
		inMap.put("esp.execution.closeDelta", "2");
		inMap.put("esp.windingDownPosMgr.dailyStopLossLimit", "3200");
		inMap.put("esp.preExecution.maxOrderSize", "4");
		System.out.println(inMap);
		System.out.println(FlatMapToClojureMap.transform(inMap, "."));
	}
}