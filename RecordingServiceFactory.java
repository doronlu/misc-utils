package net.katros.services.utils.io;

import java.io.File;
import java.nio.file.Paths;

/**
 * A factory for creating instances of {@link IRecordingService}.
 * 
 * @author doron
 */
public abstract class RecordingServiceFactory
{
	public static enum OutputType { buffered, simple, zip };
	public static enum SyncType { sync, async };

	public static CsvRecordingService createCsvRecordingService(String outputTypeStr, String syncTypeStr, String filename)
	{
		OutputType outputType = OutputType.valueOf(outputTypeStr);
		SyncType syncType = SyncType.valueOf(syncTypeStr);
		return createCsvRecordingService(outputType, syncType, filename);
	}

	public static CsvRecordingService createCsvRecordingService(OutputType outputType, SyncType syncType, String filenameWithoutExtension)
	{
		OutputStreamFactory outputStreamFactory;
		switch (outputType)
		{
			case buffered:
				outputStreamFactory = new BufferedFileOutputStreamFactory();
				break;
			case simple:
				outputStreamFactory = new SimpleFileOutputStreamFactory();
				break;
			case zip:
				String entryName = Paths.get(filenameWithoutExtension + ".csv").getFileName().toString();
				outputStreamFactory = new ZipOutputStreamFactory(entryName);
				break;
			default:
				throw new AssertionError();
		}
		File file = new File(filenameWithoutExtension + outputStreamFactory.getSuggestedExtension());
		IRecordingService recordingService;
		switch (syncType)
		{
			case async:
				recordingService = new AsyncRecordingService(file, outputStreamFactory);
				break;
			case sync:
				recordingService = new RecordingService(file, outputStreamFactory);
				break;
			default:
				throw new AssertionError();
		}
		return new CsvRecordingService(recordingService);
	}
}