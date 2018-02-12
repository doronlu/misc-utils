package net.katros.services.log4j2;

import static net.katros.services.utils.io.LogAndStdout.write;
import static org.apache.logging.log4j.Level.ERROR;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Used to help in concatenating several log messages into a single one.
 * Note: Will be logged at the level specified in {@link #flush(Level)}, and only if that level is enabled.
 * Not MT-safe.
 * 
 * @author doron
 */
public class KatLog
{
	private final Logger LOG;
	private final StringBuilder sb;
	private boolean isFirst = true;

	public KatLog(Logger log)
	{
		this(log, 1024);
	}

	public KatLog(Logger log, int builderSize)
	{
		LOG = log;
		sb = new StringBuilder(builderSize);
	}

	public void info(Object msg)
	{
		if (LOG.isInfoEnabled())
			append(String.valueOf(msg));
	}

	public void debug(Object msg)
	{
		if (LOG.isDebugEnabled())
			append(String.valueOf(msg));
	}

	public void trace(Object msg)
	{
		if (LOG.isTraceEnabled())
			append(String.valueOf(msg));
	}

	public void flush(Level level, Object msg)
	{
		append(String.valueOf(msg));
		flush(level);
	}

	public void flush(Level level)
	{
		LOG.log(level, sb);
		sb.setLength(0);
	}

	public boolean isInfoEnabled()
	{
		return LOG.isInfoEnabled();
	}

	public boolean isDebugEnabled()
	{
		return LOG.isDebugEnabled();
	}

	public boolean isTraceEnabled()
	{
		return LOG.isTraceEnabled();
	}

	private void append(String msg)
	{
		if (isFirst)
			isFirst = false;
		else
			sb.append('\n');
		sb.append(msg);
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (sb.length() != 0)
			write(LOG, ERROR, "Did not flush the following to the log: " + sb);
	}
}