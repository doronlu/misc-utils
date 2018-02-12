package net.katros.services.utils.jms;

import static net.katros.services.utils.io.LogAndStdout.write;
import static org.apache.logging.log4j.Level.INFO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A service that supports both a Queue and a Topic {@link RecoverableJmsSession}s. The sessions themselves are created
 * lazily (upon first send or check to see if enabled).
 * Uses JNDI to lookup the JMS connection factory and destination.
 * 
 * @author doron
 */
public abstract class JmsService
{
	private static final String QUEUE_NAME = "appQueue";
	private static final String TOPIC_NAME = "appTopic";
	private static RecoverableJmsSession recoverableQueueSession;
	private static RecoverableJmsSession recoverableTopicSession;
	private static final Logger LOG = LogManager.getLogger();
	static {
		Runtime.getRuntime().addShutdownHook(
			new Thread(JmsService.class.getSimpleName() + " Shutdown-hook thread")
			{
				public void run()
				{
					write(LOG, INFO, JmsService.class.getSimpleName() + " Shutdown-hook called");
					close();
				}
			});
	}

	/**
	 * 
	 * @param msg
	 * @return	true iff successfully submitted to the message broker.
	 */
	public static boolean queue(String msg)
	{
		if (recoverableQueueSession == null)
		{
			synchronized (QUEUE_NAME)
			{
				if (recoverableQueueSession == null)
					recoverableQueueSession = new RecoverableJmsSession(QUEUE_NAME);
			}
		}
		return recoverableQueueSession.send(msg);
	}

	/**
	 * 
	 * @param msg
	 * @return	true iff successfully submitted to the message broker.
	 */
	public static boolean topic(String msg)
	{
		if (recoverableTopicSession == null)
		{
			synchronized (TOPIC_NAME)
			{
				if (recoverableTopicSession == null)
					recoverableTopicSession = new RecoverableJmsSession(TOPIC_NAME);
			}
		}
		return recoverableTopicSession.send(msg);
	}

	/**
	 * Is the Queue enabled in configuration.
	 * If there was yet an attempt to create the Queue will try creating it.
	 * @return
	 */
	public static boolean isQueueEnabled()
	{
		Boolean result = RecoverableJmsSession.isConfigured(QUEUE_NAME);
		if (result != null)
			return result;
		synchronized (QUEUE_NAME)
		{
			result = RecoverableJmsSession.isConfigured(QUEUE_NAME);
			if (result != null)
				return result;
			if (recoverableQueueSession == null)
				recoverableQueueSession = new RecoverableJmsSession(QUEUE_NAME);
		}
		return RecoverableJmsSession.isConfigured(QUEUE_NAME);
	}

	/**
	 * Is the Topic enabled in configuration.
	 * If there was yet an attempt to create the Topic will try creating it.
	 * @return
	 */
	public static boolean isTopicEnabled()
	{
		Boolean result = RecoverableJmsSession.isConfigured(TOPIC_NAME);
		if (result != null)
			return result;
		synchronized (TOPIC_NAME)
		{
			result = RecoverableJmsSession.isConfigured(TOPIC_NAME);
			if (result != null)
				return result;
			if (recoverableTopicSession == null)
				recoverableTopicSession = new RecoverableJmsSession(TOPIC_NAME);
		}
		return RecoverableJmsSession.isConfigured(TOPIC_NAME);
	}

	public static void close()
	{
		if (recoverableQueueSession != null)
			recoverableQueueSession.close();
		if (recoverableTopicSession != null)
			recoverableTopicSession.close();
	}
}