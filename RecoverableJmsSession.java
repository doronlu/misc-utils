package net.katros.services.utils.jms;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * If there's a send failure will try reconnecting again on the first send after the specified period has passed.
 * 
 * @author doron
 */
public class RecoverableJmsSession implements Closeable
{
	private static final long RETRY_PERIOD_NS = 60_000_000_000L; // 1 minute
	private static final Map<String, Boolean> isConfiguredMap = new HashMap<>(); // session name -> is configured
	private final String name;
	private JmsSession jmsSession;
	private long nextAttemptTimeNs;
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * From the result of the first attempt to create a session with a specified name we project to decide whether a
	 * session for that name is configured.
	 * 
	 * @param name
	 * @return	true if configured, false if not, null if not yet attempted to create a {@link JmsSession} with this
	 * 			name.
	 */
	public static Boolean isConfigured(String name)
	{
		return isConfiguredMap.get(name);
	}

	public RecoverableJmsSession(String name)
	{
		this.name = name;
		jmsSession = JmsSession.createSession(name);
		if (!isConfiguredMap.containsKey(name)) // only the first time for each name matters
			isConfiguredMap.put(name, jmsSession != null);
	}

	/**
	 * 
	 * @param msg
	 * @return	true iff successful or not configured at all.
	 */
	public boolean send(String msg)
	{
		if (!isConfigured(name))
			return true;
		if (jmsSession == null)
		{
			if (System.nanoTime() > nextAttemptTimeNs)
			{
				jmsSession = JmsSession.createSession(name);
				if (jmsSession == null)
					return false;
			}
			else
				return false;
		}
		try
		{
			jmsSession.send(msg);
			return true;
		}
		catch (JMSException e)
		{
			jmsSession = null; // TODO do we need to first try to close the jmsSession?
			nextAttemptTimeNs = System.nanoTime() + RETRY_PERIOD_NS;
			LOG.warn("An exception occurred while trying to send msg=" + msg, e);
			return false;
		}
	}

	@Override
	public void close()
	{
		if (jmsSession != null)
			jmsSession.close();
	}
}