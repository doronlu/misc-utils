package my.katros.trials.benchmarks;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Run with something like the following to disable GC: -Xms30G -Xmx30G
 * 
 * Conclusion:
 * While clearly int[] is the fastest followed by Integer[] on both population and traversal, it seems but not
 * convincingly that ArrayDeque is faster than ArrayList on population, whereas ArrayList is clearly faster than
 * ArrayDeque on traversal.
 * 
 * @author doron
 */
public class ArrayVsDequeVsListBenchmark
{
	private static final List<GarbageCollectorMXBean> gcBeansAfterStartup = ManagementFactory.getGarbageCollectorMXBeans();

	public static void main(String... args)
	{
		final int size = 1000000;
		final int repeats = 6;
		final int[] intArray = new int[size];
		final Integer[] array = new Integer[size];
		final Deque<Integer> deque = new ArrayDeque<>();
		final List<Integer> list = new ArrayList<>();
		System.out.println("After startup: " + GcStats.getGcStats(gcBeansAfterStartup));
		for (int i = 1; i <= repeats; i++)
		{
			populateIntArray(intArray);
			System.out.print("  " + GcStats.getGcStats(gcBeansAfterStartup) + '\n');
			populateArray(array);
			System.out.print("  " + GcStats.getGcStats(gcBeansAfterStartup) + '\n');
			populateCollection(deque, size);
			System.out.print("  " + GcStats.getGcStats(gcBeansAfterStartup) + '\n');
			populateCollection(list, size);
			System.out.print("  " + GcStats.getGcStats(gcBeansAfterStartup) + '\n');
		}

		getIntArrayMax(intArray);
		getArrayMax(array);
		getDequeMax(deque, size);
		getListMax(list, size);
		System.out.print("  " + GcStats.getGcStats(gcBeansAfterStartup) + '\n');
	}

	private static void populateIntArray(final int[] array)
	{
		final int size = array.length;
		long startTime = System.nanoTime();
		for (int i = 0; i < size; i++)
			array[i] = i + 1;
		long delta = System.nanoTime() - startTime;
		System.out.printf("populating %-20.20s took %15d %s", "int array", delta, "ns");
	}

	private static void populateArray(final Integer[] array)
	{
		final int size = array.length;
		long startTime = System.nanoTime();
		for (int i = 0; i < size; i++)
			array[i] = i + 1;
		long delta = System.nanoTime() - startTime;
		System.out.printf("populating %-20.20s took %15d %s", "Integer array", delta, "ns");
	}

	private static void populateCollection(final Collection<Integer> coll, final int size)
	{
		long startTime = System.nanoTime();
		for (int i = 1; i <= size; i++)
			coll.add(i);
		long delta = System.nanoTime() - startTime;
		System.out.printf("populating %-20.20s took %15d %s", coll.getClass().getSimpleName(), delta, "ns");
	}

	private static long getIntArrayMax(final int[] array)
	{
		int max = 0;
		long startTime = System.nanoTime();
		for (int i : array)
			if (i > max)
				max = i;
		long delta = System.nanoTime() - startTime;
		System.out.printf("%-20.20s max = %10d  took %12d %s", "int array", max, delta, "ns\n");
		return delta;
	}

	private static long getArrayMax(final Integer[] array)
	{
		int max = 0;
		long startTime = System.nanoTime();
		for (int i : array)
			if (i > max)
				max = i;
		long delta = System.nanoTime() - startTime;
		System.out.printf("%-20.20s max = %10d  took %12d %s", "Integer array", max, delta, "ns\n");
		return delta;
	}

	private static long getDequeMax(final Deque<Integer> deque, final int size)
	{
		int max = 0;
		long startTime = System.nanoTime();
		final Iterator<Integer> it = deque.iterator();
		while(it.hasNext())
		{
			final int next = it.next();
			if (next > max)
				max = next;
		}
		long delta = System.nanoTime() - startTime;
		System.out.printf("%-20.20s max = %10d  took %12d %s", "deque", max, delta, "ns\n");
		return delta;
	}

	private static long getListMax(final List<Integer> list, final int size)
	{
		int max = 0;
		long startTime = System.nanoTime();
		for (int i = 0; i < size; i++)
		{
			final int next = list.get(i);
			if (next > max)
				max = next;
		}
		long delta = System.nanoTime() - startTime;
		System.out.printf("%-20.20s max = %10d  took %12d %s", "list", max, delta, "ns\n");
		return delta;
	}
}