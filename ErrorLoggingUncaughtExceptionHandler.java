package net.katros.services.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An {@link UncaughtExceptionHandler} that logs an error.
 * 
 * @author doron
 */
public class ErrorLoggingUncaughtExceptionHandler implements UncaughtExceptionHandler
{
	private final Logger log;

	public ErrorLoggingUncaughtExceptionHandler()
	{
		this(LogManager.getLogger());
	}

	public ErrorLoggingUncaughtExceptionHandler(Logger log)
	{
		this.log = log;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
		String msg = "An exception occurred in thread " + t.getName();
		log.error(msg, e);
		System.err.println(msg);
		e.printStackTrace(System.err);
	}
}