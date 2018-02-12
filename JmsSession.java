package net.katros.services.utils.jms;

import java.io.Closeable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.katros.services.utils.InvalidConfigurationException;

/**
 * A JMS session.
 * jndi.properties is looked for in the folders that are in the Classpath, that's why '../conf' was added to the Classpath.
 * 
 * @author doron
 */
public class JmsSession implements Closeable
{
	private final Connection connection;
	private final MessageProducer messageProducer;
	private final TextMessage textMessage;
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * 
	 * @param name
	 * @return	null if couldn't create.
	 */
	public static JmsSession createSession(String name)
	{
		try
		{
			return new JmsSession(name);
		}
		catch (Throwable t)
		{
			if (LOG.isTraceEnabled()) // Debug is usually enabled in non-live runs
				LOG.warn("Unable to create a JMS producer '" + name + "'. This is normal when this producer is not desired and the stack trace can be ignored.", t);
			else
				LOG.warn("Unable to create a JMS producer '" + name + "'. This is normal when this producer is not desired. Change log level to TRACE for stack trace");
			return null;
		}
	}

	private JmsSession(String name) throws JMSException, InvalidConfigurationException
	{
		Context jndiContext = getContext();
		ConnectionFactory connectionFactory = jndiLookup(jndiContext, "ConnectionFactory");
		connection = connectionFactory.createConnection();
		Destination destination = jndiLookup(jndiContext, name);
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		messageProducer = session.createProducer(destination);
		textMessage = session.createTextMessage();
		LOG.info(JmsSession.class.getSimpleName() + " with name=" + name + " successfully started");
	}

	private static Context getContext() throws InvalidConfigurationException
	{
		try
		{
			return new InitialContext();
		}
		catch (NamingException e)
		{
			throw new InvalidConfigurationException("Could not create JNDI API context", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T jndiLookup(Context jndiContext, String name) throws InvalidConfigurationException
	{
		try
		{
			return (T)jndiContext.lookup(name);
		}
		catch (NamingException e)
		{
			throw new InvalidConfigurationException("JNDI API lookup for '" + name + "' in file jndi.properties failed", e);
		}
	}

	/**
	 * 
	 * @param msg
	 * @return	true iff successfully submitted to the message broker.
	 */
	public boolean sendEatException(String msg)
	{
		try
		{
			send(msg);
			return true;
		}
		catch (JMSException e)
		{
			LOG.warn("An exception occurred while trying to send msg=" + msg, e);
			return false;
		}
	}

	public void send(String msg) throws JMSException
	{
		textMessage.setText(msg); // might step on an existing msg??
		messageProducer.send(textMessage);
	}

	@Override
	public void close()
	{
		try
		{
			if (connection != null)
				connection.close();
		}
		catch (JMSException e)
		{
			LOG.warn(e);
		}
	}
}