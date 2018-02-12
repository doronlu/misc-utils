package net.katros.services.utils.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Recording {@link String}s, usually to a file.
 * 
 * @author doron
 */
public interface IRecordingService extends Closeable
{
	public void print(String str);
	public void print(byte[] bytes);
	public void closeEatException();
	public void flush() throws IOException;

	@Override
	public void close() throws IOException;
}