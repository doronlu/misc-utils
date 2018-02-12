package net.katros.services.utils.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A factory for creating {@Link OutputStream}s.
 * 
 * @author doron
 */
public interface OutputStreamFactory
{
	/**
	 * Creates a new OutputStream. It's the caller's responsibility to close the
	 * stream.
	 * 
	 * @param	file
	 * @param	append
	 * @return
	 * @throws IOException
	 * @see {@link FileOutputStream}.
	 */
	public OutputStream getOutputStream(File file, boolean append) throws IOException;

	/**
	 * Allows to specify a suggested filename extension based on the factory
	 * type.
	 * 
	 * @return
	 */
	public String getSuggestedExtension();
}