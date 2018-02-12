package net.katros.services.utils.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An {@link IRecordingService} that records {@link String}s to a file, using the specified {@link OutputStreamFactory}.
 * Creates the new file only on first write.
 * 
 * @author doron
 */
public class RecordingService implements IRecordingService
{
	private final OutputStreamFactory outputStreamFactory;
	private final File file;
    private OutputStream outputStream;
    private boolean append;
	private static Logger log = LogManager.getLogger();

	/**
	 * Creates a file, eats - yet logs - any {@link IOException} that may happen.
	 * Logs at Info file creation and closing.
	 * 
	 * @param filename
	 * @param content
	 */
	public static void createFileEatException(String filename, String content)
	{
		try
		{
			createFile(filename, content);
		}
		catch (IOException e)
		{
			log.error("Error while writing file " + filename, e);
		}
	}

	/**
	 * Creates a file.
	 * Logs at Info file creation and closing.
	 * 
	 * @param filename
	 * @param content
	 * @throws IOException
	 */
	public static void createFile(String filename, String content) throws IOException
	{
		try (IRecordingService recordingService = new RecordingService(new File(filename), new SimpleFileOutputStreamFactory()))
		{
			if (content != null)
				recordingService.print(content);
		}
	}

	/**
	 * 
	 * @param file					if null then will create a 'do nothing'
	 * 								{@link CsvRecordingService}.
	 * @param outputStreamFactory
	 */
	public RecordingService(File file, OutputStreamFactory outputStreamFactory)
	{
		this(file, outputStreamFactory, false);
	}

	/**
	 * 
	 * @param file					if null then will create a 'do nothing'
	 * 								{@link CsvRecordingService}.
	 * @param outputStreamFactory
	 * @param append
	 */
	public RecordingService(File file, OutputStreamFactory outputStreamFactory, boolean append)
	{
		this.outputStreamFactory = outputStreamFactory;
		this.file = file;
		this.append = append;
	}

	@Override
	public void print(String str)
	{
		print(str.getBytes());
	}

	@Override
	public void print(byte[] bytes)
	{
		try
		{
			if (outputStream == null)
			{
				DirectoryStructureBuilder.mkdirs(file.getParent());
				outputStream = outputStreamFactory.getOutputStream(file, append);
				log.info("Created new file: " + file.getCanonicalPath());
			}
			outputStream.write(bytes);
		}
		catch (IOException e)
		{
			log.error("Error while writing to file " + file.getAbsolutePath(), e); // TODO consider adding isError à la 'trouble' mechanism in PrintStream
		}
	}

	@Override
	public void flush() throws IOException
	{
		outputStream.flush();
	}

	@Override
	public void close() throws IOException
	{
		if (outputStream != null)
		{
			outputStream.flush();
			outputStream.close();
		}
		try
		{
			log.info("Closed file: " + file.getCanonicalPath());
		}
		catch (IOException e)
		{
			log.error("Error while closing file " + file.getAbsolutePath(), e); // TODO consider adding isError à la 'trouble' mechanism in PrintStream
		}
	}

	@Override
	public void closeEatException()
	{
		try
		{
			close();
		}
		catch (IOException e)
		{
			log.error("Error while closing file " + file.getAbsolutePath(), e);
		}
	}
}