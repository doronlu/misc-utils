package net.katros.services.utils;

/**
 * An {@link InvalidArgumentException} signifying a problem with configuration (that is passed as an argument to the
 * complaining method).
 * 
 * @author doron
 */
public class InvalidConfigurationException extends InvalidArgumentException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Exception#Exception(String)
	 */
	public InvalidConfigurationException(String message)
	{
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public InvalidConfigurationException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public InvalidConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}