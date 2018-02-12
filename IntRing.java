package net.katros.services.utils;

/**
 * A Ring data structure (Circular buffer) containing a fixed size, ordered, list of <code>int</code>s.
 * When a new <code>int</code> is added the least-recently-used one is removed.
 * Initially filled with zeros.
 * 
 * @author doron
 */
public class IntRing
{
	private final int[] array;
	private int lruIndex; // index to the least recently used element
	private int sum;

	public IntRing(int size)
	{
		array = new int[size];
	}

	/**
	 * 
	 * @param newValue
	 * @return			the least recently used int.
	 */
	public int add(int newValue)
	{
		int removed = array[lruIndex];
		array[lruIndex] = newValue;
		if (lruIndex == array.length - 1)
			lruIndex = 0;
		else
			lruIndex++;
		sum += newValue - removed;
		return removed;
	}

	public int getSum()
	{
		return sum;
	}

	public void empty()
	{
		for (int i = 0; i < array.length; i++)
			array[i] = 0;
		lruIndex = sum = 0;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(array.length * 3);
		int limit = lruIndex + array.length;
		for (int i = lruIndex; i < limit; i++)
		{
			int position = i % (array.length);
			sb.append(array[position]);
			if (i < limit - 1)
				sb.append(",");
		}
		return sb.toString();
	}

	public static void main(String[] args)
	{
		IntRing intLRU = new IntRing(3);
		System.out.println(intLRU + " sum=" + intLRU.getSum());
		for (int i = 1; i <= 12; i++)
		{
			intLRU.add(i);
			System.out.println(intLRU + " sum=" + intLRU.getSum());
		}
		intLRU.empty();
		System.out.println(intLRU + " sum=" + intLRU.getSum());
	}
}