package my.katros.trials.benchmarks;

/**
 * Results:

  using index straight total time = 7843243925
 using index inLoopReg total time = 7895976806
using index outLoopReg total time = 7870708885
using index outLoopReg2total time = 13396917398
       not using index total time = 7850027999

  using index straight total time = 7927106817
 using index inLoopReg total time = 7927195384
using index outLoopReg total time = 7912343367
using index outLoopReg2total time = 13447082721
       not using index total time = 7941906146

 * Conclusion:
 * Except for outLoopReg2 which is a disaster results are so similar that it does not seem statistically meaningful.
 * 
 * @author doron
 */
public class LoopingOverArrayBenchmark
{
	public static void main(String... args)
	{
		final int size = 100000000;
		final int repeats = 100;
		final double[] array = new double[size];
		for (int i = 0; i < size; i++)
			array[i] = i + 1;

		long usingIndexStraightTime = 0;
		long usingIndexAndInLoopRegTime = 0;
		long usingIndexAndOutLoopRegTime = 0;
		long usingIndexAndOutLoopReg2Time = 0;
		long notUsingIndexTime = 0;
		for (int i = 1; i <= repeats; i++)
		{
			usingIndexStraightTime += getMaxUsingIndexStraight(array);
			usingIndexAndInLoopRegTime += getMaxUsingIndexAndInLoopReg(array);
			usingIndexAndOutLoopRegTime += getMaxUsingIndexAndOutLoopReg(array);
			usingIndexAndOutLoopReg2Time += getMaxUsingIndexAndOutLoopReg2(array);
			notUsingIndexTime += getMaxWithoutUsingIndex(array);
		}
		System.out.println("  using index straight total time = " + usingIndexStraightTime);
		System.out.println(" using index inLoopReg total time = " + usingIndexAndInLoopRegTime);
		System.out.println("using index outLoopReg total time = " + usingIndexAndOutLoopRegTime);
		System.out.println("using index outLoopReg2total time = " + usingIndexAndOutLoopReg2Time);
		System.out.println("       not using index total time = " + notUsingIndexTime);
	}

	private static long getMaxUsingIndexStraight(final double[] array)
	{
		final int size = array.length;
		double max = 0;
		long startTime = System.nanoTime();
		for (int i = 0; i < size; i++)
			if (array[i] > max)
				max = array[i];
		long delta = System.nanoTime() - startTime;
		System.out.println("  (straignt) array max using index= " + max + "\ttook " + delta + "ns");
		return delta;
	}

	private static long getMaxUsingIndexAndInLoopReg(final double[] array)
	{
		final int size = array.length;
		double max = 0;
		long startTime = System.nanoTime();
		for (int i = 0; i < size; i++)
		{
			final double val = array[i];
			if (val > max)
				max = val;
		}
		long delta = System.nanoTime() - startTime;
		System.out.println(" (inLoopReg) array max using index= " + max + "\ttook " + delta + "ns");
		return delta;
	}

	private static long getMaxUsingIndexAndOutLoopReg(final double[] array)
	{
		final int size = array.length;
		double max = 0;
		long startTime = System.nanoTime();
		double val;
		for (int i = 0; i < size; i++)
		{
			val = array[i];
			if (val > max)
				max = val;
		}
		long delta = System.nanoTime() - startTime;
		System.out.println("(outLoopReg) array max using index= " + max + "\ttook " + delta + "ns");
		return delta;
	}

	private static long getMaxUsingIndexAndOutLoopReg2(final double[] array)
	{
		final int size = array.length;
		double max = 0;
		long startTime = System.nanoTime();
		double val;
		for (int i = 0; i < size; )
		{
			val = array[i++];
			if (val > max)
				max = val;
		}
		long delta = System.nanoTime() - startTime;
		System.out.println("(outLoopReg2)array max using index= " + max + "\ttook " + delta + "ns");
		return delta;
	}

	private static long getMaxWithoutUsingIndex(final double[] array)
	{
		double max = 0;
		long startTime = System.nanoTime();
		for (double d : array)
			if (d > max)
				max = d;
		long delta = System.nanoTime() - startTime;
		System.out.println("         array max not using index= " + max + "\ttook " + delta + "ns");
		return delta;
	}
}