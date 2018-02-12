package net.katros.services.utils.mail;

/**
 * Holds the different fields and directives for sending mails.
 * Using the Builder pattern.
 * 
 * @author doron
 */
public class MailFields
{
	private final String subject;
	private String body = "";
	private boolean isHtml = true;
	private String subjectPrefix = null;

	public MailFields(String subject)
	{
		this.subject = subject;
	}

	public MailFields(String subject, String body)
	{
		this.subject = subject;
		this.body = body;
	}

	public MailFields setBody(String body)
	{
		this.body = body;
		return this;
	}

	public MailFields setIsHtml(boolean isHtml)
	{
		this.isHtml = isHtml;
		return this;
	}

	public MailFields setSubjectPrefix(String subjectPrefix)
	{
		this.subjectPrefix = subjectPrefix;
		return this;
	}

	public String getSubject()
	{
		return subject;
	}

	public String getBody()
	{
		return body;
	}

	public boolean isHtml()
	{
		return isHtml;
	}

	public String getSubjectPrefix()
	{
		return subjectPrefix;
	}

	@Override
	public String toString()
	{
		return "SubjectPrefix=" + subjectPrefix + " Subject=" + subject + " body=" + body + " isHtml=" + isHtml;
	}
}