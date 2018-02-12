package net.katros.services.utils.mail;

import static net.katros.services.utils.io.LogAndStdout.write;
import static org.apache.logging.log4j.Level.INFO;
import static org.apache.logging.log4j.Level.WARN;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.SmtpAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.net.SmtpManager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.katros.services.utils.ErrorLoggingRejectedExecutionHandler;
import net.katros.services.utils.ErrorLoggingUncaughtExceptionHandler;
import net.katros.services.utils.InvalidConfigurationException;
import net.katros.services.utils.StringSubstitution;

/**
 * An implementation of {@link IMailSender}.
 * 
 * @author doron
 */
public class MailSender implements Closeable
{
	private static final String MAIL_SENDER_NAME = "MailSender";
	private final ThreadPoolExecutor executor;
	private final InternetAddress internetAddressFrom;
	private final InternetAddress[] internetAddressTos;
	private final String defaultSubjectPrefixBeforeSubstitutions;
	private final Session session;
	private static final Logger LOG = LogManager.getLogger();

	public MailSender(String from, String to, String smtpHost, int smtpPort) throws AddressException
	{
		this(from, to, smtpHost, smtpPort, null);
	}

	public MailSender(String from, String to, String smtpHost, int smtpPort, String defaultSubjectPrefixBeforeSubstitutions)
		throws AddressException
	{
		ThreadFactory threadFactory
			= new ThreadFactoryBuilder()
				.setDaemon(false)
				.setNameFormat(this.getClass().getSimpleName() + "-%d")
				.setUncaughtExceptionHandler(new ErrorLoggingUncaughtExceptionHandler(LOG))
				.setPriority(Thread.MIN_PRIORITY)
				.build();
		executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
			threadFactory, new ErrorLoggingRejectedExecutionHandler(LOG));
		internetAddressFrom = new InternetAddress(from);
		String[] tos = to.split(",");
		internetAddressTos = new InternetAddress[tos.length];
		for (int i = 0; i < tos.length; i++)
			internetAddressTos[i] = new InternetAddress(tos[i]);
		this.defaultSubjectPrefixBeforeSubstitutions = defaultSubjectPrefixBeforeSubstitutions == null ? "" : defaultSubjectPrefixBeforeSubstitutions;
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.setProperty("mail.smtp.port", String.valueOf(smtpPort));
		session = Session.getDefaultInstance(properties);
	}

	public boolean send(MailFields mailFields, boolean isAsync)
	{
		try
		{
			LOG.debug("Will now send mail: " + mailFields);
			send0(mailFields, isAsync, null);
			return true;
		}
		catch (MessagingException e)
		{
			LOG.warn("Could not send mail, continues running. " + mailFields, e);
			return false;
		}
	}

	public boolean send(MailFields mailFields, boolean isAsync, Map<String, String> properties)
	{
		try
		{
			LOG.debug("Will now send mail: " + mailFields);
			send0(mailFields, isAsync, properties);
			return true;
		}
		catch (MessagingException e)
		{
			LOG.warn("Could not send mail, continues running. " + mailFields, e);
			return false;
		}
	}

	private void send0(MailFields mailFields, boolean isAsync, Map<String, String> properties) throws MessagingException
	{
		MimeMessage mimeMessage = new MimeMessage(session);
		mimeMessage.setFrom(internetAddressFrom);
		for (InternetAddress to : internetAddressTos)
			mimeMessage.addRecipient(Message.RecipientType.TO, to);
		String subjectPrefix = mailFields.getSubjectPrefix();
		if (subjectPrefix == null)
			subjectPrefix = new StringSubstitution().substitute(defaultSubjectPrefixBeforeSubstitutions, properties);
		mimeMessage.setSubject(subjectPrefix + mailFields.getSubject());
		if (mailFields.isHtml())
			mimeMessage.setContent(mailFields.getBody(), "text/html");
		else
			mimeMessage.setText(mailFields.getBody());
		if (isAsync)
			executor.execute(new MailHandler(mimeMessage));
		else
			new MailHandler(mimeMessage).run();
	}

	@Override
	public void close()
	{
		executor.shutdown();
		try { Thread.sleep(2000); } catch (InterruptedException e1) { write(LOG, WARN, e1.toString()); }
		try
		{
			if (executor.awaitTermination(60, TimeUnit.SECONDS))
				write(LOG, INFO, "MailSender.close() terminated successfully");
			else
				write(LOG, WARN, "MailSender: Timeout elapsed before termination completed");
		}
		catch (InterruptedException e)
		{
			write(LOG, WARN, "MailSender: Wait for termination interrupted", e);
		}
	}

	public static MailSender createMailSenderBasedOnLog4jConf()
	{
		try
		{
			return createMailSenderBasedOnLog4jConf0();
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvalidConfigurationException e)
		{
			LOG.warn("Could not create MailSender", e);
			return null;
		}
	}

	/*
	 * Didn't find how to do this w/o reflection...
	 */
	private static MailSender createMailSenderBasedOnLog4jConf0()
		throws NoSuchFieldException, IllegalAccessException, InvalidConfigurationException
	{
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		SmtpAppender smtpAppender = (SmtpAppender)loggerConfig.getAppenders().get(MAIL_SENDER_NAME);
		if (smtpAppender == null)
		{
			LogManager.getLogger().warn("No SMTP appender named '" + MAIL_SENDER_NAME + "' was found, some mails may not be sent");
			return null;
		}
		Field f = smtpAppender.getClass().getDeclaredField("manager");
		f.setAccessible(true);
		SmtpManager smtpManager = (SmtpManager)f.get(smtpAppender);
		f = smtpManager.getClass().getDeclaredField("data");
		f.setAccessible(true);
		Class<?> clazz = f.getType();
		Object o = f.get(smtpManager);
		f = clazz.getDeclaredField("from");
		f.setAccessible(true);
		String from = (String)f.get(o);
		f = clazz.getDeclaredField("to");
		f.setAccessible(true);
		String to = (String)f.get(o);
		f = clazz.getDeclaredField("host");
		f.setAccessible(true);
		String host = (String)f.get(o);
		f = clazz.getDeclaredField("port");
		f.setAccessible(true);
		int port = (int)f.get(o);
		// The following is commented out b/c in log4j2 2.9.0 f.get(o) return an object of type
		// org.apache.logging.log4j.core.layout.PatternLayout$PatternSerializer and not the String representation of the
		// subject field.
//		f = clazz.getDeclaredField("subject");
//		f.setAccessible(true);
//		String subject = (String)f.get(o);
		String subject = null;
		try
		{
			return new MailSender(from, to, host, port, subject);
		}
		catch (AddressException e)
		{
			throw new InvalidConfigurationException(e);
		}
	}

	public static void main(String[] args) throws MessagingException
	{
		MailSender mailSender = new MailSender("donotreply@katros.net", "doron@katros.net", "fs2", 25, "Alert - ");
		String content = "This is nothing.<h1>This is a message</h1><font color=\"red\"><b>something</b></font>Nothing";
		mailSender.send(new MailFields("3", content).setIsHtml(true), true);
		mailSender.send(new MailFields("4", content).setIsHtml(false), true);
		mailSender.close();
	}

	class MailHandler implements Runnable
	{
		private final MimeMessage mimeMessage;

		private MailHandler(MimeMessage mimeMessage)
		{
			this.mimeMessage = mimeMessage;
		}

		@Override
		public void run()
		{
			try
			{
				send(mimeMessage.getSubject());
			}
			catch (MessagingException e)
			{
				write(LOG, WARN, "Could not infer mail's subject, no mail sent, continues running.", e);
			}
		}

		private void send(String subject)
		{
			try
			{
				Transport.send(mimeMessage);
				write(LOG, INFO, "Will now send with MimeMessage's subject: " + subject);
			}
			catch (MessagingException e)
			{
				write(LOG, WARN, "Could not send mail, continues running. MimeMessage's subject: " + subject, e);
			}
		}
	}
}