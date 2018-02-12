package net.katros.services.utils.mail;

import static net.katros.services.utils.io.LogAndStdout.write;
import static org.apache.logging.log4j.Level.INFO;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sends mail using the settings in the Log4j config file.
 * 
 * @author doron
 */
public abstract class DefaultMailSender
{
	private static final MailSender MAIL_SENDER = MailSender.createMailSenderBasedOnLog4jConf(); // may be null
	private static final Logger LOG = LogManager.getLogger();
	static {
		Runtime.getRuntime().addShutdownHook(
			new Thread(DefaultMailSender.class.getSimpleName() + " Shutdown-hook thread")
			{
				public void run()
				{
					write(LOG, INFO, DefaultMailSender.class.getSimpleName() + " Shutdown-hook called");
					close();
				}
			});
	}

	public static boolean send(MailFields mailFields)
	{
		return send(mailFields, true, null);
	}

	public static boolean send(MailFields mailFields, boolean isAsync)
	{
		return send(mailFields, isAsync, null);
	}

	public static boolean send(MailFields mailFields, boolean isAsync, Map<String, String> properties)
	{
		return MAIL_SENDER == null ? false : MAIL_SENDER.send(mailFields, isAsync, properties);
	}

	public static boolean isDefined()
	{
		return MAIL_SENDER != null;
	}

	public static void close()
	{
		if (MAIL_SENDER != null)
			MAIL_SENDER.close();
	}
}