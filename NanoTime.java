package my.katros.trials.benchmarks;

public class NanoTime
{
	public static void main(String... args)
	{
		long startTime = System.nanoTime();
		long time = 0;
		for(int i = 0; i < 100000000; i++)
		{
			time = System.nanoTime();
			if (time == 9L)
				System.out.println("bingo");
		}
		long endTime = System.nanoTime();
		System.out.println("Total time          nanoTime: " + (endTime-startTime));
		startTime = System.nanoTime();
		time = 0;
		for(int i = 0; i < 100000000; i++)
		{
			time = System.currentTimeMillis();
			if (time == 9L)
				System.out.println("bingo");
		}
		endTime = System.nanoTime();
		System.out.println("Total time currentTimeMillis: " + (endTime-startTime));
	}
}
