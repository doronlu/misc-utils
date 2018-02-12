package net.katros.services.utils.io;

import java.io.IOException;

/**
 * Recording {@link String}s and doubles to a file in CSV format.
 * Creates the new file only on first write.
 * 
 * @author doron
 */
public class CsvRecordingService implements ICsvRecordingService
{
	private final IRecordingService recordingService;

	public CsvRecordingService(IRecordingService recordingService)
	{
		this.recordingService = recordingService;
	}

	/* (non-Javadoc)
	 * @see net.katros.strategies.util.io.ICsvRecordingService#println(java.lang.String, double)
	 */
	@Override
	public void println(double... doubles)
	{
		println(null, doubles);
	}

	/* (non-Javadoc)
	 * @see net.katros.strategies.util.io.ICsvRecordingService#println(double)
	 */
	@Override
	public void println(String str, double... doubles)
	{
		StringBuilder sb = new StringBuilder(doubles.length * 15);
		if (str != null)
		{
			sb.append(str);
			if (doubles.length > 0)
				sb.append(",");
		}
		for (int i = 0; i < doubles.length; i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(doubles[i]);
		}
		println(sb.toString());
	}

	/* (non-Javadoc)
	 * @see net.katros.strategies.util.io.ICsvRecordingService#println(java.lang.String)
	 */
	@Override
	public void println(String... strings)
	{
		StringBuilder sb = new StringBuilder(strings.length * 15);
		for (int i = 0; i < strings.length; i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(strings[i]);
		}
		println(sb.toString());
	}

	/* (non-Javadoc)
	 * @see net.katros.strategies.util.io.ICsvRecordingService#println(java.lang.String)
	 */
	@Override
	public void println(String str)
	{
		print(str + "\n"); // we don't want the platform specific "line.separator"
	}

	/* (non-Javadoc)
	 * @see net.katros.strategies.util.io.ICsvRecordingService#print(java.lang.String)
	 */
	@Override
	public void print(String str)
	{
		recordingService.print(str);
	}

	/* (non-Javadoc)
	 * @see net.katros.strategies.util.io.ICsvRecordingService#close()
	 */
	@Override
	public void close() throws IOException
	{
		recordingService.close();
	}
}