package net.katros.services.utils.jms;

import static net.katros.services.utils.StringUtils.print;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.katros.services.utils.CollectionUtils;
import net.katros.services.utils.GlobalConf;
import net.katros.services.utils.concurrent.ElementsProcessor;
import net.katros.services.utils.concurrent.ExecuteNowScheduler;
import net.katros.services.utils.concurrent.IScheduler;
import net.katros.services.utils.concurrent.PeriodicScheduler;
import net.katros.services.utils.io.IRecordingService;
import net.katros.services.utils.io.RecordingService;
import net.katros.services.utils.io.SimpleFileOutputStreamFactory;

/**
 * Aggregates and sends JMS messages periodically or immediately.
 * MT-safe.
 * Work submitted after {@link #close()} had been called or even shortly before it had been called may not get
 * processed.
 * 
 * @author doron
 */
public class JmsAggregatorSender implements Closeable
{
	private static final String MSG_SEPARATOR = "|";
	private final String name;
	private final IScheduler<String, Void> scheduler;
	private final IRecordingService recordingService = new RecordingService(new File("jms.log"), new SimpleFileOutputStreamFactory());
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * 
	 * @param name		used for naming the thread-pool and for logging.
	 * @param sender	the operation to take, would normally be JmsSevice::topic but can be something else for sake
	 * 					of testing.
	 */
	public JmsAggregatorSender(String name, Function<String, Boolean> sender)
	{
		this.name = name;
		final long periodMs = GlobalConf.get().getJmsSendingPeriodMs();
		print(this + " has periodMs=" + periodMs);
		if (periodMs > 0) // using PeriodicScheduler
		{
			ElementsProcessor<String, Void> elementsProcessor = new ElementsProcessor<String, Void>()
			{
				private Collection<String> list = new ArrayList<>();

				@Override
				public Void process(Collection<String> elements)
				{
					String msgs = CollectionUtils.toString(elements, MSG_SEPARATOR);
					boolean isSent = sender.apply(msgs);
					if (LOG.isDebugEnabled() || !isSent)
						logJmsMessages(elements);
					return null;
				}

				@Override
				public Collection<String> getCollection()
				{
					return list;
				}
			};
			scheduler = new PeriodicScheduler<>(elementsProcessor, periodMs, Thread.MIN_PRIORITY, true, name, false);
		}
		else // using ExecuteNowScheduler
		{
			ElementsProcessor<String, Void> elementsProcessor = new ElementsProcessor<String, Void>()
			{
				@Override
				public Void process(Collection<String> elements)
				{
					boolean isSent = sender.apply(elements.iterator().next());
					if (LOG.isDebugEnabled() || !isSent)
						logJmsMessages(elements);
					return null;
				}
			};
			scheduler = new ExecuteNowScheduler<>(elementsProcessor);
		}
	}

	private void logJmsMessages(Collection<String> elements)
	{
		StringBuilder sb = new StringBuilder(512);
		for (String element : elements)
			sb.append("\n").append(element);
		recordingService.print(sb.toString());
	}

	public void send(String msg)
	{
		try
		{
			scheduler.add(msg);
		}
		catch (InterruptedException e)
		{
			LOG.warn("An exception while trying to send msg=" + msg, e);
		}
	}

	@Override
	public String toString()
	{
		return JmsAggregatorSender.class.getSimpleName() + " with name=" + name;
	}

	@Override
	public void close() throws IOException
	{
		if (scheduler != null)
			scheduler.close();
	}
}