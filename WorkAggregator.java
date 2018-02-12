package net.katros.services.utils.concurrent;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.katros.services.utils.ErrorLoggingUncaughtExceptionHandler;
import net.katros.services.utils.GlobalConf;

/**
 * A service, initialized with a pre-set time span (for example 3 seconds), that receives work (objects) over time and
 * each time 3 seconds have passed with no new work, it releases all the accumulated work as a collection.
 * Each time a collection of items is released, that collection should be processed by a user specified
 * {@link ElementsProcessor}.
 * 
 * Example:
 * Assume the pre-set time span is 3000ms and the received work is:
 * 1 -- 1000ms -- 2 -- 1000ms -- 3 -- 1000ms -- 4 -- 3500ms -- 5
 * The result should be items 1,2,3,4 released together 3 seconds after item 4 arrival, and item 5 will be released
 * later, once 3 seconds with no new work have passed.
 * 
 * @author doron
 *
 * @param <E>	see {@link ElementsProcessor}.
 * @param <V>	see {@link ElementsProcessor}.
 */
public class WorkAggregator<E, V> implements Closeable
{
	private final ElementsProcessor<E, V> elementsProcessor;
	private final long waitPeriodMs;
	private Deque<E> elements = new ArrayDeque<>();
	private final ScheduledExecutorService scheduler;
	private ScheduledFuture<V> scheduledFuture;
	private static final Logger LOG = LogManager.getLogger();

	public WorkAggregator(ElementsProcessor<E, V> elementsProcessor, long waitPeriodMs, int threadPriority)
	{
		this(elementsProcessor, waitPeriodMs, null, threadPriority);
	}

	public WorkAggregator(ElementsProcessor<E, V> elementsProcessor, long waitPeriodMs, ScheduledExecutorService scheduler)
	{
		this(elementsProcessor, waitPeriodMs, scheduler, null);
	}

	private WorkAggregator(ElementsProcessor<E, V> elementsProcessor, long waitPeriodMs, ScheduledExecutorService scheduler,
			Integer threadPriority)
	{
		this.elementsProcessor = elementsProcessor;
		this.waitPeriodMs = waitPeriodMs;
		if (scheduler != null)
		{
			this.scheduler = scheduler;
			return;
		}
		ThreadFactory threadFactory
			= new ThreadFactoryBuilder()
				.setDaemon(false)
				.setNameFormat(this.getClass().getSimpleName() + "-%d")
				.setUncaughtExceptionHandler(new ErrorLoggingUncaughtExceptionHandler(LOG))
				.setPriority(threadPriority)
				.build();
		this.scheduler = Executors.newScheduledThreadPool(1, threadFactory);
		print(WorkAggregator.class.getSimpleName() + " created: " + this);
	}

	public synchronized void add(E element) throws RejectedExecutionException
	{
		attemptToCancelScheduledWork();
		elements.add(element);
//		print("In add(), added element " + element + ", elements=" + elements);
		print("In add(), added element " + element + ", total of " + elements.size() + " elements");
		Handler<E, V> handler = new Handler<E, V>(elementsProcessor, elements);
		try
		{
			scheduledFuture = scheduler.schedule(handler, waitPeriodMs, MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			throw new RejectedExecutionException("Unable to schedule task: " + handler, e);
		}
	}

	private void attemptToCancelScheduledWork()
	{
		if (scheduledFuture == null)
			return;
		long delay = scheduledFuture.getDelay(MILLISECONDS);
		print("In attemptToCancelScheduledWork(), scheduledFuture delay=" + delay);
		if (delay < 50 || !scheduledFuture.cancel(false)) // delay < 50 - ugly hack!
			elements = new ArrayDeque<>(); // if we weren't able to cancel work we need to remove it from future processing
	}

	public synchronized void flush()
	{
		attemptToCancelScheduledWork();
		if (!elements.isEmpty())
			elementsProcessor.process(elements);
	}

	@Override
	public String toString()
	{
		return "waitPerionMs=" + waitPeriodMs + " elementsProcessor=" + elementsProcessor + ", scheduler=" + scheduler
			+ ", elements=" + elements;
	}

	@Override
	public synchronized void close() throws IOException
	{
		print("In WorkAggregator.close() elements=" + elements);
		flush();
		print("In WorkAggregator.close() scheduler=" + scheduler);
		scheduler.shutdown();
		try
		{
			if (scheduler.awaitTermination(GlobalConf.get().getWorkAggregatorAwaitTerminationMs(), MILLISECONDS))
				LOG.info("WorkAggregator.close() terminated successfully");
			else
				LOG.warn("Timeout elapsed before termination during WorkAggregator.close()");
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
	}

	private static void print(String s)
	{
//		System.out.format("%tT  %-16s %s\n", Calendar.getInstance(), Thread.currentThread().getName(), s);
		LOG.info(String.format("%tT  %-16s %s", Calendar.getInstance(), Thread.currentThread().getName(), s));
	}

	public static void main(String... args) throws IOException
	{
		long[] sleepTimes = { 1000, 1000, 1000, 3500, 3500, 1000, 3500, 1000, 1000 }; // each time is the sleep AFTER the element is added
		ElementsProcessor<String, Boolean> elementsProcessor = new ElementsProcessor<String, Boolean>()
		{
			@Override
			public Boolean process(Collection<String> elements)
			{
				print("Starting processing " + elements);
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
				print("Finished processing " + elements);
				return null;
			}

			@Override
			public String toString()
			{
				return "Printing-" + ElementsProcessor.class.getSimpleName();
			}
		};
		try (WorkAggregator<String, Boolean> workAggregator = new WorkAggregator<>(elementsProcessor, 3000, Thread.NORM_PRIORITY))
		{
			print("Start, sleep-times=" + Arrays.toString(sleepTimes));
			for (int i = 1; i <= sleepTimes.length; i++)
			{
				workAggregator.add(String.valueOf(i));
				try { Thread.sleep(sleepTimes[i - 1]); } catch (InterruptedException e) { e.printStackTrace(); }
			}
			print("The end");
		}
	}
}

class Handler<E, V> implements Callable<V>
{
	private final ElementsProcessor<E, V> elementsProcessor;
	private final Deque<E> elements;

	Handler(ElementsProcessor<E, V> elementsProcessor, Deque<E> elements)
	{
		this.elementsProcessor = elementsProcessor;
		this.elements = elements;
	}

	@Override
	public V call() throws Exception
	{
		return elementsProcessor.process(elements);
	}

	@Override
	public String toString()
	{
		return "elementsProcessor=" + elementsProcessor + " elements=" + elements;
	}
}