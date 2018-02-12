package net.katros.services.utils;

/**
 * A checked exception thrown to indicate a problem with the data the method deals with.
 * E.g. when a method reads data from a file but it's not in the expected format.
 * 
 * @author doron
 */
public class InvalidDataException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Exception#Exception(String)
	 */
	public InvalidDataException(String message)
	{
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public InvalidDataException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public InvalidDataException(String message, Throwable cause)
	{
		super(message, cause);
	}
}