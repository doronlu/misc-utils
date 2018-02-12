package net.katros.services.utils;

/**
 * A Ring data structure (Circular buffer) containing an ordered list of <code>double</code>s, limited by capacity.
 * When a new element is added then if already full to capacity the oldest one is removed.
 * TODO if ever in use again change capacity to be a factor of 2 and change modulus operation to be a bitwise operation.
 * 
 * @author doron
 */
public class DoublesRing
{
	private final double[] array;
	private int lruIndex; // index to the least recently used element
	private int size; // number of elements in the ring

	public DoublesRing(int capacity)
	{
		array = new double[capacity];
		empty();
	}

	/**
	 * 
	 * @param newValue
	 * @return	the value removed, {@value Double#NaN} if the value is NAN or if nothing removed.
	 * 			<code>isAnyElementRemoved()</code> may be used to distinguish between the latter two cases.
	 */
	public double add(double newValue)
	{
		if (size < array.length)
			size++;
		double removed = array[lruIndex];
		array[lruIndex] = newValue;
		if (lruIndex == array.length - 1)
			lruIndex = 0;
		else
			lruIndex++;
		return removed;
	}

	public double get(int position)
	{
		if (position >= size)
			throw new IndexOutOfBoundsException("Current ring size is " + size);
		return array[(lruIndex + position) % size];
	}

	public int size()
	{
		return size;
	}

	public void empty()
	{
		for (int i = 0; i < array.length; i++)
			array[i] = Double.NaN;
		lruIndex = 0;
		size = 0;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(array.length * 3);
		sb.append("[");
		for (int i = lruIndex - size + array.length; i < lruIndex + array.length; i++)
		{
			int position = i % array.length;
			double value = array[position];
			sb.append(value);
			if (i < lruIndex + array.length - 1)
				sb.append(",");
		}
		sb.append("] size=" + size);
		return sb.toString();
	}

	public static void main(String[] args)
	{
		testCorrectness();
		testPerformance();
	}

	private static void testCorrectness()
	{
		DoublesRing intLRU = new DoublesRing(3);
		System.out.println("Correctness test:\n" + intLRU);
		for (int i = 1; i <= 2; i++)
		{
			intLRU.add(i);
			System.out.println(intLRU);
		}
		System.out.println("get(1)=" + intLRU.get(1));
		for (int i = 3; i <= 12; i++)
		{
			intLRU.add(i);
			System.out.println(intLRU);
		}
		for (int i = 0; i < 3; i++)
			System.out.println("get(" + i + ")=" + intLRU.get(i));
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
		DoublesRing primitive = new DoublesRing(10000);
		int counter = 0;
		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= 1000000000; i++)
		{
			double removed = primitive.add(i);
			if (Double.isNaN(removed) || removed < 100)
				counter++;
		}
		long endTime = System.currentTimeMillis();
		System.out.println("\nPerformance test:\n" + counter + "  took: " + (endTime - startTime) + "ms");
	}
}