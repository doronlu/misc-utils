package net.katros.services.utils.io;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.katros.services.utils.ProcessUtils;

/**
 * A factory for creating a {@link BufferedOutputStream} to the specified file.
 * 
 * @author doron
 */
public class BufferedFileOutputStreamFactory implements OutputStreamFactory
{
	private final static String DEFAULT_SUGGESTED_EXTENSION = ".csv";
	private final String suggestedExtension;

	public BufferedFileOutputStreamFactory()
	{
		this(DEFAULT_SUGGESTED_EXTENSION);
	}

	public BufferedFileOutputStreamFactory(String suggestedExtension)
	{
		this.suggestedExtension = suggestedExtension;
	}

	@Override
	public OutputStream getOutputStream(File file, boolean append) throws FileNotFoundException
	{
		return new BufferedOutputStream(new FileOutputStream(file, append));
	}

	/**
	 * A first step in migrating to NIO.2.
	 * 
	 * @param path
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public OutputStream getOutputStream(Path path, OpenOption... options) throws IOException
	{
		return new BufferedOutputStream(Files.newOutputStream(path, options));
	}

	@Override
	public String getSuggestedExtension()
	{
		return suggestedExtension;
	}

	public static void main(String[] args) throws IOException
	{
		BufferedFileOutputStreamFactory factory = new BufferedFileOutputStreamFactory();
		Path path = Paths.get("moses/risotto/logfile.txt");
		Files.createDirectories(path.getParent());
		OutputStream outputStream = factory.getOutputStream(path, CREATE, TRUNCATE_EXISTING);
		String env = ProcessUtils.getEnvironmentParams();
		outputStream.write(env.getBytes());
		outputStream.flush();
		outputStream.close();
		System.out.println(env);
	}
}