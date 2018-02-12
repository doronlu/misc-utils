package net.katros.services.utils;

/**
 * A checked exception thrown to indicate that a method has been passed an illegal or inappropriate argument.
 * Similar to {@link IllegalArgumentException} but differs in being a checked exception.
 * 
 * @author doron
 */
public class InvalidArgumentException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Exception#Exception(String)
	 */
	public InvalidArgumentException(String message)
	{
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public InvalidArgumentException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public InvalidArgumentException(String message, Throwable cause)
	{
		super(message, cause);
	}
}