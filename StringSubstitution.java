package net.katros.services.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Substitutes ${key} with value.
 * 
 * @author doron
 */
public class StringSubstitution
{
	private Map<String, String> map;
	private StringBuilder sb;

	public String substitute(String s, Map<String, String> map)
	{
		if (map == null)
			return s;
		this.map = map;
		sb = new StringBuilder(s.length() * 2);
		sub(s);
		return sb.toString();
	}

	private void sub(String s)
	{
		String[] parts = s.split("\\$\\{", 2);
		if (parts.length == 1) // no open delimiter
		{
			sb.append(s);
			return;
		}
		String[] parts2 = parts[1].split("}", 2);
		if (parts2.length == 1) // no close delimiter
		{
			sb.append(s);
			return;
		}
		String val = map.get(parts2[0]);
		if (val != null)
			sb.append(parts[0]).append(val);
		else
			sb.append(parts[0]).append("${").append(parts2[0]).append("}");
		sub(parts2[1]);
	}

	/**
abc
abc${
abc${key
abcvalue
abcvalue_and_main_${no}_value_${no
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		Map<String, String> map = new HashMap<>();
		map.put("thread-name", Thread.currentThread().getName());
		map.put("key", "value");
		System.out.println(new StringSubstitution().substitute("abc", map));
		System.out.println(new StringSubstitution().substitute("abc${", map));
		System.out.println(new StringSubstitution().substitute("abc${key", map));
		System.out.println(new StringSubstitution().substitute("abc${key}", map));
		System.out.println(new StringSubstitution().substitute("abc${key}_and_${thread-name}_${no}_${key}_${no", map));
	}
}