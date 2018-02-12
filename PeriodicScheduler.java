package net.katros.services.utils.concurrent;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.katros.services.utils.StringUtils.print;
import static org.apache.logging.log4j.Level.WARN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.katros.services.utils.ErrorLoggingUncaughtExceptionHandler;

/**
 * Receives elements over time and processes them periodically using the specified {@link ElementsProcessor}.
 * MT-safe.
 * Work submitted after {@link #close()} had been called or even shortly before it had been called may not get
 * processed.
 * 
 * @author doron
 *
 * @param <E>	the element type.
 * @param <V>	the return type from the {@link ElementsProcessor}.
 */
public class PeriodicScheduler<E, V> implements IScheduler<E, V>
{
	private final ScheduledExecutorService scheduler;
	private final BlockingQueue<E> queue = new LinkedBlockingQueue<>();
	private final QueueProcessor queueProcessor;
	private static final Logger LOG = LogManager.getLogger();

	public PeriodicScheduler(ElementsProcessor<E, V> elementsProcessor, long periodMs, int threadPriority,
		boolean isDaemon, String threadPoolName, boolean isFixedRate)
	{
		ThreadFactory threadFactory
			= new ThreadFactoryBuilder()
				.setDaemon(isDaemon)
				.setNameFormat(threadPoolName + "-%d")
				.setUncaughtExceptionHandler(new ErrorLoggingUncaughtExceptionHandler(LOG))
				.setPriority(threadPriority)
				.build();
		scheduler = Executors.newScheduledThreadPool(1, threadFactory);
		queueProcessor = new QueueProcessor(elementsProcessor);
		if (isFixedRate)
			scheduler.scheduleAtFixedRate(queueProcessor, periodMs, periodMs, MILLISECONDS);
		else
			scheduler.scheduleWithFixedDelay(queueProcessor, periodMs, periodMs, MILLISECONDS);
	}

	@Override
	public void add(E element) throws InterruptedException
	{
		queue.put(element);
//		print("added:" + element + "\t\tqueue=" + queue);
	}

	/**
	 * Note that tasks that were added recently may not be executed.
	 */
	@Override
	public void close() throws IOException
	{
		try
		{
			scheduler.shutdown();
			if (scheduler.awaitTermination(200, MILLISECONDS))
				print(PeriodicScheduler.class.getSimpleName() + ".close() terminated successfully");
			else
				print(WARN, "Timeout elapsed before termination during " + PeriodicScheduler.class.getSimpleName() + ".close()");
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
		finally
		{
			queueProcessor.run();
		}
	}

	private class QueueProcessor implements Runnable
	{
		private final ElementsProcessor<E, V> elementsProcessor;

		private QueueProcessor(ElementsProcessor<E, V> elementsProcessor)
		{
			this.elementsProcessor = elementsProcessor;
		}

		@Override
		public void run()
		{
//			print("run() called");
			if (queue.isEmpty())
				return;
			Collection<E> coll = elementsProcessor.getCollection(); // we're reusing the same collection
			queue.drainTo(coll);
			elementsProcessor.process(coll);
			coll.clear(); // clear the collection as we're going to reuse it
		}
	}

	/**
	 * Note that for proper testing {@link PeriodicScheduler#add(Object)} should be called by multiple threads
	 * concurrently.
	 * Note that the output is not divided every round 500ms b/c the PeriodicScheduler is not started at the exact same
	 * time the first sleep commences, not to mention other inaccuracies due to thread scheduling etc.
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	// If there's no output check the body of the print method is not commented out
	public static void main(String... args) throws InterruptedException, IOException
	{
		long[] times = { 10, 100, 200, 350, 1350, 1450, 1550, 3550, 3950 };
		ElementsProcessor<String, Boolean> elementsProcessor = new ElementsProcessor<String, Boolean>()
		{
			private Collection<String> list = new ArrayList<>();

			@Override
			public Boolean process(Collection<String> elements)
			{
				print("Starting processing " + elements);
				try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
				print("Finished processing " + elements);
				return null;
			}

			@Override
			public Collection<String> getCollection()
			{
				return list;
			}
		};
		try (PeriodicScheduler<String, Boolean> periodicScheduler
			= new PeriodicScheduler<String, Boolean>(elementsProcessor, 500, Thread.NORM_PRIORITY, false, "test", false))
		{
			long currentTime = 0L;
			for (int i = 0; i < times.length; i++)
			{
				print(currentTime + "\twill sleep for " + (times[i] - currentTime) + "ms");
				try { Thread.sleep(times[i] - currentTime); } catch (InterruptedException e) { e.printStackTrace(); }
				currentTime = times[i];
				print(currentTime + "\tadding " + times[i]);
				periodicScheduler.add(String.valueOf(times[i]));
			}
		}
	}
}