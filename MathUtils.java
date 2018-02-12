package net.katros.services.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Math related utilities.
 * 
 * @author doron
 */
public class MathUtils
{
	private static double[] powersOf10 =
		{
			1d,
			10d,
			100d,
			1000d,
			10000d,
			100000d,
			1000000d,
			10000000d,
			100000000d,
			1000000000d,
			10000000000d,
			100000000000d,
			1000000000000d,
			10000000000000d,
			100000000000000d,
			1000000000000000d,
			10000000000000000d,
			100000000000000000d,
			1000000000000000000d,
			10000000000000000000d
		};

	public static double round(double d, int placesAfterDecimalPoint)
	{
		// A solution that provides better handling of edge cases yet is a bit less efficient:
		// return new BigDecimal(d).setScale(placesAfterDecimalPoint, RoundingMode.HALF_UP).doubleValue();
		// (see http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places)
		if (Double.isNaN(d))
			return Double.NaN;
		if (placesAfterDecimalPoint == 8) // this is always (or almost always) the case
			return Math.round(d * 100000000d) / 100000000d;
		if (placesAfterDecimalPoint > -1 && placesAfterDecimalPoint < 20)
			return Math.round(d * powersOf10[placesAfterDecimalPoint]) / powersOf10[placesAfterDecimalPoint];
		double pow = Math.pow(10, placesAfterDecimalPoint);
		return Math.round(d * pow) / pow;
	}

	/**
	 * A {@link Comparator} that returns 0 if the two specified {@link Double}s are less than {@link #epsilon} apart.
	 * Note that the relationship defined is not transitive.
	 * 
	 * @author doron
	 */
	public static class LowResDoublesComparator implements Comparator<Double>
	{
		private final double epsilon;

		public LowResDoublesComparator(int precision)
		{
			epsilon = 0.5 * Math.pow(10, -precision); // if precision=8 then epsilon=5E-9;
		}

		/**
		 * 
		 * @param d1	assumed not null, NaN etc.
		 * @param d2	assumed not null, NaN etc.
		 * @return	0 if the two values are less than {@value #epsilon} apart, otherwise 1 if d1 is greater and -1 if d2
		 * 			is greater.
		 */
		@Override
		public int compare(Double d1, Double d2)
		{
			return Math.abs(d1 - d2) < epsilon ? 0 : d1 > d2 ? 1 : -1;
		}
	}

	/**
	 * A {@link Comparator} that first rounds the {@link Double}s per the specified precision and then compares them.
	 * 
	 * @author doron
	 */
	public static class RoundingDoublesComparator implements Comparator<Double>
	{
		private final int precision;

		public RoundingDoublesComparator(int precision)
		{
			this.precision = precision;
		}

		@Override
		public int compare(Double d1, Double d2)
		{
			return Double.compare(round(d1, precision), round(d2, precision));
		}
	}

	public static void testRound(String[] args)
	{
		System.out.println(round(3.22999, 2));
		System.out.println(round(3.1234, 2));
		System.out.println(round(3, 2));
		System.out.println(round(0, 2));
		System.out.println(round(3, 0));
		System.out.println(round(34567, -2));
		System.out.println(round(Double.NaN, 2));
	}

	public static void testRoundingDoublesComparator(Comparator<Double> comparator)
	{
		int[] results = new int[3];
		long start = System.currentTimeMillis();
		for (int i = 1; i <= 10000000; i++)
			results[comparator.compare(Math.random(), 0.5) + 1]++;
		long durationInMs = System.currentTimeMillis() - start;
		System.out.println(Arrays.toString(results) + " Took " + durationInMs + "ms");
	}

	public static void testRoundPerformance()
	{
		final double[] doubles = { 0.17605376768962566, 0.5850410213880107, 0.8578522696914949, 0.5325919756743327, 0.4987357955230166,
			0.4397362129116952, 0.11209167758826366, 0.3125639401767619, 0.3944389102707523, 0.3343652452685536 };
		double sum = 0d;
		long startTime = System.nanoTime();
		for (int i = 1; i < (int)1E8; i++) // without the cast to int the test is ~27% slower...
			sum += round(doubles[i % 10], 8);
		long elapsedTime = System.nanoTime() - startTime;
		long roundedTime = Math.round(elapsedTime / 1E6);
		System.out.println("'round2' Elapsed time = " + roundedTime + "ms, sum=" + Math.round(sum));
	}

	/**
	 * Note that the order of the specified elements is not preserved.
	 * 
	 * @param elements	assumed to contain at least 1 element.
	 * @return
	 */
	public static long getMedian(List<Long> elements)
	{
		Collections.sort(elements);
		int size = elements.size();
		int middle = size / 2;
		return size % 2 == 1 ? elements.get(middle) : (elements.get(middle) + elements.get(middle - 1)) / 2L;
	}

	public static void main(String[] args)
	{
		testRound(args);
		testRoundingDoublesComparator(new RoundingDoublesComparator(8));
		testRoundingDoublesComparator(new LowResDoublesComparator(8));
		testRoundPerformance();
	}
}