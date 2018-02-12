package net.katros.services.utils.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * A factory for creating a simple (no buffering etc.) {@link OutputStream} to
 * the specified file.
 * This class is mostly for testing and debugging purposes.
 * 
 * @author doron
 */
public class SimpleFileOutputStreamFactory implements OutputStreamFactory
{
	private final static String DEFAULT_SUGGESTED_EXTENSION = ".csv";
	private final String suggestedExtension;

	public SimpleFileOutputStreamFactory()
	{
		this(DEFAULT_SUGGESTED_EXTENSION);
	}

	public SimpleFileOutputStreamFactory(String suggestedExtension)
	{
		this.suggestedExtension = suggestedExtension;
	}

	@Override
	public OutputStream getOutputStream(File file, boolean append) throws FileNotFoundException
	{
		return new FileOutputStream(file, append);
	}

	@Override
	public String getSuggestedExtension()
	{
		return suggestedExtension;
	}
}