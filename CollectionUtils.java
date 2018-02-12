package net.katros.services.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * Utilities concerned with collections.
 * 
 * @author doron
 */
public abstract class CollectionUtils
{
	public static final Long[] EMPTY_LONG_OBJ_ARRAY = new Long[0];
	public static final Double[] EMPTY_DOUBLE_OBJ_ARRAY = new Double[0];

	public static <E> E[] listToArray(List<E> list, Class<E> clazz)
	{
		@SuppressWarnings("unchecked")
		final E[] a = (E[])Array.newInstance(clazz, list.size());
		for (int i = 0; i < a.length; i++)
			a[i] = list.get(i);
		return a;
	}

	/**
	 * Returns an array of in.length+1 elements containing all the elements in the specified 'in' array (preserving
	 * their order) with the addition of the specified 'element' in the last place.
	 * 
	 * @param in		assumed not null.
	 * @param element	the element to add to the array.
	 * @return
	 */
	public static <E> E[] addToArray(E[] in, E element)
	{
		E[] out = Arrays.copyOf(in, in.length + 1);
		out[out.length - 1] = element;
		return out;
	}

	public static List<Long> toList(long[] longs)
	{
		if (longs == null)
			return null; // otherwise NullPointerException at Arrays.asList()
		return Arrays.asList(toObjArray(longs));
	}

	public static Long[] toObjArray(long[] longs)
	{
		if (longs == null)
			return null;
		if (longs.length == 0)
			return EMPTY_LONG_OBJ_ARRAY;
		Long[] longObjects = new Long[longs.length];
		for (int i = 0; i < longs.length; i++)
			longObjects[i] = longs[i];
		return longObjects;
	}

	/**
	 * 
	 * @param list	if any is more than 15 digits then conversion to double uses rounding.
	 * @return
	 */
	public static double[] toDoublesArray(List<Long> list)
	{
		double[] vals = new double[list.size()];
		for (int i = 0; i < vals.length; i++)
			vals[i] = (double)list.get(i);
		return vals;
	}

	public static List<Double> toList(double[] doubles)
	{
		if (doubles == null)
			return null; // otherwise NullPointerException at Arrays.asList()
		return Arrays.asList(toObjArray(doubles));
	}

	public static double[] doubleToPrimitives(List<Double> doubles)
	{
		double[] result = new double[doubles.size()];
		int i = 0;
		for (Double val : doubles) {
			result[i] = val;
			i++;
		}
		return result;
	}

	public static int[] integerToPrimitives(List<Integer> integers)
	{
		int[] result = new int[integers.size()];
		int i = 0;
		for (Integer val : integers) {
			result[i] = val;
			i++;
		}
		return result;
	}

	public static Double[] toObjArray(double[] doubles)
	{
		if (doubles == null)
			return null;
		if (doubles.length == 0)
			return EMPTY_DOUBLE_OBJ_ARRAY;
		Double[] doubleObjects = new Double[doubles.length];
		for (int i = 0; i < doubles.length; i++)
			doubleObjects[i] = doubles[i];
		return doubleObjects;
	}

	/**
	 * Returns a List of the times in milliseconds for the specified list of
	 * {@link Calendar}.
	 * 
	 * @param list
	 * @return
	 */
	public static List<Long> toLongsList(List<? extends Calendar> list)
	{
		if (list == null)
			return null;
		List<Long> longs = new ArrayList<Long>();
		for (Calendar calendar : list)
			longs.add(calendar.getTimeInMillis());
		return longs;
	}

	/**
	 * Returns a CSV representation of the elements in the specified list.
	 * 
	 * @param list
	 * @return
	 */
	public static <E> String toCsv(List<E> list)
	{
		Iterator<E> it = list.iterator();
		if (!it.hasNext())
			return "";
		StringBuilder sb = new StringBuilder();
		for (;;)
		{
			E e = it.next();
			sb.append(e);
			if (!it.hasNext())
				return sb.toString();
			sb.append(",");
		}
	}

	/**
	 * Returns a CSV representation of the elements in the specified array.
	 * 
	 * @param array
	 * @param nullSubstitute
	 * @return
	 */
	public static <E> String toCsv(E[] array, String nullSubstitute)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(array[i] == null ? nullSubstitute : array[i].toString());
		}
		return sb.toString();
	}

	/**
	 * Returns a CSV representation of the elements in the specified array.
	 * 
	 * @param array
	 * @return
	 */
	public static <E> String toCsv(int[] array)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(String.valueOf(array[i]));
		}
		return sb.toString();
	}

	/**
	 * Returns a CSV representation of the elements in the specified array.
	 * 
	 * @param array
	 * @return
	 */
	public static <E> String toCsv(long[] array)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(String.valueOf(array[i]));
		}
		return sb.toString();
	}

	/**
	 * Returns a CSV representation of the elements in the specified array.
	 * 
	 * @param array
	 * @return
	 */
	public static <E> String toCsv(double[] array)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(String.valueOf(array[i]));
		}
		return sb.toString();
	}

	/**
	 * Non recursive, i.e. doesn't convert inner maps.
	 *
	 * @param inMap
	 * @return
	 */
	public static Map<String, Object> convertMap(Map<String, String> inMap)
	{
		Map<String, Object> outMap = new HashMap<>();
		for (Map.Entry<String, String> entry : inMap.entrySet())
			outMap.put(entry.getKey(), entry.getValue());
		return outMap;
	}	

	public static double[] intArrayToDoubleArray(int[] ints)
	{
		double[] doubles = new double[ints.length];
		for (int i = 0; i < ints.length; i++)
			doubles[i] = ints[i];
		return doubles;
	}

	/**
	 * recursively print collection elements
	 * 
	 * @param	obj	the object to be printed 
	 * @return 	concatenated values of the elements of the given collection, by calling their 'toString'. if an element is a collection its elements will also be printed
	 */
	public static String toStringRecursive(Object obj)
	{
		if (obj == null)
			return "null";
		if ( obj instanceof Collection<?> )
			obj = ((Collection<?>)obj).toArray();
		else if ( !obj.getClass().isArray() )
			return obj.toString();
		int arrayLength = Array.getLength(obj);
		if (arrayLength == 0)
			return "[]";
		if (arrayLength == 1)
			return toStringRecursive(Array.get(obj, 0));
		String str = "[" + toStringRecursive(Array.get(obj, 0));
		for (int i = 1; i < arrayLength; i++)
			str += ", " + toStringRecursive(Array.get(obj, i));
		str += "]";
		return str;
	}

	/**
	 * Returns a {@link String} containing the collection elements separated with the specified separator.
	 * 
	 * @param coll
	 * @param separator
	 * @return
	 */
	public static <E> String toString(Collection<E> coll, String separator)
	{
		Iterator<E> i = coll.iterator();
		if (!i.hasNext())
			return "";
		StringBuilder sb = new StringBuilder();
		for (;;)
		{
			sb.append(String.valueOf(i.next()));
			if (!i.hasNext())
				return sb.toString();
			sb.append(separator);
		}
	}

	public static boolean isStrictlyMonotonic(double[] vals, boolean isAscending)
	{
		if (isAscending)
		{
			for (int i = 1; i < vals.length; i++)
				if (vals[i] <= vals[i - 1])
					return false;
		}
		else
		{
			for (int i = 1; i < vals.length; i++)
				if (vals[i] >= vals[i - 1])
					return false;
		}
		return true;
	}

	public static boolean isStrictlyMonotonic(int[] vals, boolean isAscending)
	{
		if (isAscending)
		{
			for (int i = 1; i < vals.length; i++)
				if (vals[i] <= vals[i - 1])
					return false;
		}
		else
		{
			for (int i = 1; i < vals.length; i++)
				if (vals[i] >= vals[i - 1])
					return false;
		}
		return true;
	}

	public static double[] readCsvDoubleArray(String str)
	{
		String[] strs = str.split(",");
		double[] nums = new double[strs.length];
		for (int i = 0; i < strs.length; i++)
			nums[i] = Double.valueOf(strs[i]);
		return nums;
	}
	public static int[] readCsvIntArray(String str)
	{
		String[] strs = str.split(",");
		int[] nums = new int[strs.length];
		for (int i = 0; i < strs.length; i++)
			nums[i] = Integer.valueOf(strs[i]);
		return nums;
	}

	public static double[] unaryMinus(double[] nums)
	{
		double[] result = nums.clone();
		for (int i = 0; i < result.length; i++)
			result[i] = -result[i];
		return result;
	}
	public static int[] unaryMinus(int[] nums)
	{
		int[] result = nums.clone();
		for (int i = 0; i < result.length; i++)
			result[i] = -result[i];
		return result;
	}

	// from: http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/util/stream/Collectors.java#Collectors.throwingMerger%28%29
	// see: http://stackoverflow.com/questions/31004899/java-8-collectors-tomap-sortedmap
	public static <T> BinaryOperator<T> throwingMerger()
	{
		return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
	}
}