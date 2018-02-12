package net.katros.services.utils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@link RejectedExecutionHandler} that logs an error.
 * 
 * @author doron
 */
public class ErrorLoggingRejectedExecutionHandler implements RejectedExecutionHandler
{
	private final Logger log;

	public ErrorLoggingRejectedExecutionHandler()
	{
		this(LogManager.getLogger());
	}

	public ErrorLoggingRejectedExecutionHandler(Logger log)
	{
		this.log = log;
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
	{
		log.error("Unable to perform job=" + r + " in executor=" + executor);
	}
}