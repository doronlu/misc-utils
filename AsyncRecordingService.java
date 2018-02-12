package net.katros.services.utils.io;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.katros.services.utils.ErrorLoggingUncaughtExceptionHandler;

/**
 * An {@link IRecordingService} that records {@link String}s to a file, using
 * the specified {@link OutputStreamFactory}, in a dedicated thread.
 * Creates the new file only on first write.
 * 
 * @author doron
 */
public class AsyncRecordingService implements IRecordingService
{
	private final ThreadPoolExecutor executor;
	private final IRecordingService recordingService;
	private static final Logger LOG = LogManager.getLogger();
	private int counter;
	private int comulativeQueueSize;
	private int maxQueueSize;

	/**
	 * 
	 * @param file					if null then will create a 'do nothing'
	 * 								{@link CsvRecordingService}.
	 * @param outputStreamFactory
	 */
	public AsyncRecordingService(File file, OutputStreamFactory outputStreamFactory)
	{
		ThreadFactory threadFactory
			= new ThreadFactoryBuilder()
				.setDaemon(true)
				.setNameFormat(this.getClass().getSimpleName() + "-" + file.getName() + "-%d")
				.setUncaughtExceptionHandler(new ErrorLoggingUncaughtExceptionHandler(LOG))
				.setPriority(Thread.MIN_PRIORITY)
				.build();
		executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
		recordingService = new RecordingService(file, outputStreamFactory);
	}

	@Override
	public void print(String str)
	{
		print(str.getBytes());
	}

	@Override
	public void print(byte[] bytes)
	{
		if (LOG.isTraceEnabled())
		{
			int queueSize = executor.getQueue().size();
			maxQueueSize = Math.max(maxQueueSize, queueSize);
			comulativeQueueSize += executor.getQueue().size();
			counter++;
			if (counter % 1000 == 0)
				LOG.trace("Avg. queue size=" + (comulativeQueueSize / counter) + "  current counter=" + counter
					+ "  current queue size=" + queueSize + "  max queue size=" + maxQueueSize);
		}
		executor.execute(new RecordingHandler(bytes));
	}

	@Override
	public void flush() throws IOException
	{
		recordingService.flush();
	}

	@Override
	public void close() throws IOException
	{
		shutdownExecutor();
		recordingService.close();
	}

	@Override
	public void closeEatException()
	{
		shutdownExecutor();
		recordingService.closeEatException();
	}

	private void shutdownExecutor()
	{
		executor.shutdown();
		try
		{
			if (!executor.awaitTermination(1, TimeUnit.SECONDS))
				LOG.warn("Timeout elapsed before termination completed");
		}
		catch (InterruptedException e)
		{
			LOG.warn("Wait for termination interrupted", e);
		}
	}

	class RecordingHandler implements Runnable
	{
		private final byte[] bytes;

		private RecordingHandler(byte[] bytes)
		{
			this.bytes = bytes;
		}

		@Override
		public void run()
		{
			recordingService.print(bytes);
		}
	}
}