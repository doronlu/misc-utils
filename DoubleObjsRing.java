package net.katros.services.utils;

/**
 * A Ring data structure (Circular buffer) containing an ordered list of <code>double</code>s, limited by capacity.
 * When a new element is added then if already full to capacity the oldest one is removed.
 * 
 * @author doron
 */
public class DoubleObjsRing
{
	private final Double[] array;
	private int lruIndex; // index to the least recently used element

	public DoubleObjsRing(int capacity)
	{
		array = new Double[capacity];
		empty();
	}

	/**
	 * 
	 * @param newValue
	 * @return	the value removed, null if nothing removed.
	 */
	public Double add(double newValue)
	{
		Double removed = array[lruIndex];
		array[lruIndex] = newValue;
		if (lruIndex == array.length - 1)
			lruIndex = 0;
		else
			lruIndex++;
		return removed;
	}

	public void empty()
	{
		for (int i = 0; i < array.length; i++)
			array[i] = null;
		lruIndex = 0;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(array.length * 3);
		sb.append("[");
		int limit = lruIndex + array.length;
		for (int i = lruIndex; i < limit; i++)
		{
			int position = i % (array.length);
			Double value = array[position];
			if (value == null)
				continue;
			sb.append(value);
			if (i < limit - 1)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	public static void main(String[] args)
	{
		testCorrectness();
		testPerformance();
	}

	private static void testCorrectness()
	{
		DoubleObjsRing intLRU = new DoubleObjsRing(3);
		System.out.println("Correctness test:\n" + intLRU);
		for (int i = 1; i <= 12; i++)
		{
			intLRU.add(i);
			System.out.println(intLRU);
		}
		intLRU.empty();
		System.out.println(intLRU);
		for (int i = 16; i <= 19; i++)
		{
			intLRU.add(i);
			System.out.println(intLRU);
		}
	}

	private static void testPerformance()
	{
		DoubleObjsRing primitive = new DoubleObjsRing(10000);
		int counter = 0;
		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= 1000000000; i++)
		{
			Double removed = primitive.add(i);
			if (removed == null)
				counter++;
			else if (removed < 100)
				counter++;
		}
		long endTime = System.currentTimeMillis();
		System.out.println("\nPerformance test:\n" + counter + "  took: " + (endTime - startTime) + "ms");
	}
}