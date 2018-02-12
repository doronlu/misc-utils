package net.katros.services.utils.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Recording {@link String}s and doubles, usually to a file.
 * 
 * @author doron
 */
public interface ICsvRecordingService extends Closeable
{
	public abstract void println(double... doubles);
	public abstract void println(String str, double... doubles);
	public abstract void println(String... strings);
	public abstract void println(String str);
	public abstract void print(String str);
	public abstract void close() throws IOException;
}