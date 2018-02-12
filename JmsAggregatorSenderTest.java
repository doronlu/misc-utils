package net.katros.services.utils.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import net.katros.services.utils.GlobalConf;

/**
 * Unit-tests for {@link JmsAggregatorSender}.
 * 
 * @author doron
 */
public class JmsAggregatorSenderTest
{
	private static final long[] TIMES = { 1, 10, 20, 45, 105, 145, 155, 255, 295 };

	@Test
	public void testExecuteNow() throws IOException, InterruptedException
	{
		String expected = "[1, 10, 20, 45, 105, 145, 155, 255, 295]";
		GlobalConf.get().setJmsSendingPeriodMs(0);
		test(expected);
	}

	@Test
	public void testExecutePeriodically() throws IOException, InterruptedException
	{
		String expected = "[1|10|20, 45, 105, 145|155, 255, 295]";
		GlobalConf.get().setJmsSendingPeriodMs(40);
		test(expected);
	}

	@Test
	public void testMultiThreadedExecuteNow() throws IOException, InterruptedException
	{
		String expected = "[1, 1, 1, 10, 10, 10, 20, 20, 20, 45, 45, 45, 105, 105, 105, 145, 145, 145, 155, 155, 155, 255, 255, 255, 295, 295, 295]";
		Set<String> expecteds = new HashSet<>();
		expecteds.add(expected);
		GlobalConf.get().setJmsSendingPeriodMs(0);
		testMultiThreaded(3, expecteds);
	}

	/**
	 * Unit-testing something that depends on execution speed is difficult b/c the result is not deterministic; hence in
	 * this case there are several valid expected results and yet more may need to be added.
	 * Note that the output is not divided every round 40ms b/c the PeriodicScheduler is not started at the exact same
	 * time the first sleep commences, not to mention other inaccuracies due to thread scheduling etc.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testMultiThreadedExecutePeriodically() throws IOException, InterruptedException
	{
		String expected1 = "[1|1|1|10|10|10|20|20|20, 45|45|45, 105|105|105, 145|145|145|155|155|155, 255|255|255, 295|295|295]";
		String expected2 = "[1|1|1|10|10|10|20|20|20, 45|45|45, 105|105|105, 145|145|145, 155|155|155, 255|255|255, 295|295|295]";
		Set<String> expecteds = new HashSet<>();
		expecteds.add(expected1);
		expecteds.add(expected2);
		GlobalConf.get().setJmsSendingPeriodMs(40);
		testMultiThreaded(3, expecteds);
	}

	private void test(String expected) throws IOException, InterruptedException
	{
		List<String> list = new ArrayList<>();
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		try (JmsAggregatorSender jmsAggregatorSender = new JmsAggregatorSender(methodName, list::add))
//		try (JmsAggregatorSender jmsAggregatorSender = new JmsAggregatorSender(methodName, JmsAggregatorSenderTest::print))
		{
			long currentTime = 0L;
			for (int i = 0; i < TIMES.length; i++)
			{
				Thread.sleep(TIMES[i] - currentTime);
				currentTime = TIMES[i];
				jmsAggregatorSender.send(String.valueOf(TIMES[i]));
			}
		}
//		System.out.println(list);
		assertEquals(expected, list.toString());
	}

	private void testMultiThreaded(int numOfThreads, Set<String> expecteds) throws IOException, InterruptedException
	{
		List<String> list = new ArrayList<>();
		Thread[] threads = new Thread[numOfThreads];
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		try (JmsAggregatorSender jmsAggregatorSender = new JmsAggregatorSender(methodName, list::add))
		{
			for (int j = 0; j < numOfThreads; j++)
				threads[j] = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						long currentTime = 0L;
						for (int i = 0; i < TIMES.length; i++)
						{
							try { Thread.sleep(TIMES[i] - currentTime); } catch (InterruptedException e) { e.printStackTrace(); }
							currentTime = TIMES[i];
							jmsAggregatorSender.send(String.valueOf(TIMES[i]));
						}
					}
				}, "thread-" + String.valueOf(j));
			for (int i = 0; i < numOfThreads; i++)
				threads[i].start();
			for (int i = 0; i < numOfThreads; i++)
				threads[i].join();
		}
//		System.out.println(list);
		try
		{
			assertTrue(expecteds.contains(list.toString()));
		}
		catch (AssertionError e)
		{
			System.out.println("Actual=" + list);
			throw(e);
		}
	}

//	private static boolean print(String str)
//	{
//		System.out.println(str);
//		return true;
//	}
}