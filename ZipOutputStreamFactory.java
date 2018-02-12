package net.katros.services.utils.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A factory for creating a buffered {@link ZipOutputStream} to
 * the specified file.
 * Using UTF-8 {@link Charset}.
 * 
 * @author doron
 */
public class ZipOutputStreamFactory implements OutputStreamFactory
{
	public final static String CHARSET_NAME = "UTF-8";
	private final static String DEFAULT_SUGGESTED_EXTENSION = ".zip";
	private final String entryName;
	private final String optionalHeaderContent;
	private final String optionalHeaderEntryName;
	private final String suggestedExtension;

	/**
	 * A factory for creating a buffered {@link ZipOutputStream} to
	 * a {@link ZipEntry} with the specified entryName.
	 * 
	 * @param entryName
	 */
	public ZipOutputStreamFactory(String entryName)
	{
		this(entryName, null, null, DEFAULT_SUGGESTED_EXTENSION);
	}

	/**
	 * A factory for creating a buffered {@link ZipOutputStream} to
	 * a {@link ZipEntry} with the specified entryName.
	 * 
	 * @param entryName
	 */
	public ZipOutputStreamFactory(String entryName, String suggestedExtension)
	{
		this(entryName, null, null, suggestedExtension);
	}

	/**
	 * 
	 * @param entryName
	 * @param optionalHeaderContent
	 * @param optionalHeaderEntryName
	 */
	public ZipOutputStreamFactory(String entryName, String optionalHeaderContent, String optionalHeaderEntryName)
	{
		this(entryName, optionalHeaderContent, optionalHeaderEntryName, DEFAULT_SUGGESTED_EXTENSION);
	}

	/**
	 * A factory for creating a buffered {@link ZipOutputStream} to
	 * a {@link ZipEntry} with the specified entryName.
	 * If the optional parameters are set will create a {@link ZipEntry} with
	 * the specified optionalHeaderEntryName name and with the
	 * optionalHeaderContent content.
	 * 
	 * @param entryName
	 * @param optionalHeaderContent
	 * @param optionalHeaderEntryName
	 */
	public ZipOutputStreamFactory(String entryName, String optionalHeaderContent, String optionalHeaderEntryName,
			String suggestedExtension)
	{
		this.entryName = entryName;
		this.optionalHeaderContent = optionalHeaderContent;
		this.optionalHeaderEntryName = optionalHeaderEntryName;
		this.suggestedExtension = suggestedExtension;
	}

	@Override
	public OutputStream getOutputStream(File file, boolean append) throws IOException
	{
		OutputStream outputStream = new BufferedFileOutputStreamFactory().getOutputStream(file, append);
		ZipOutputStream zos = new ZipOutputStream(outputStream);
		if (optionalHeaderContent != null)
		{
			ZipEntry ze = new ZipEntry(optionalHeaderEntryName);
			zos.putNextEntry(ze);
			zos.write(optionalHeaderContent.getBytes(CHARSET_NAME));
		}
		ZipEntry ze = new ZipEntry(entryName);
		zos.putNextEntry(ze);
		return zos;
	}

	@Override
	public String getSuggestedExtension()
	{
		return suggestedExtension;
	}
}
//	public static OutputStream getZipOutputStream(File file, String optionalHeader)
//		throws IOException
//	{
//		ZipOutputStream zos
//			= new ZipOutputStream(
//				new BufferedOutputStream(
//					new FileOutputStream(file)));
//		ZipEntry ze = new ZipEntry("Config");
//		zos.putNextEntry(ze);
//		zos.write(optionalHeader.getBytes());
//		ze = new ZipEntry("Table");
//		zos.putNextEntry(ze);
//		return zos;
//	}

//	public static void writeToFile(File file, String content)
//		throws IOException
//	{
//		ZipOutputStream zos
//			= new ZipOutputStream(
//				new BufferedOutputStream(
//					new FileOutputStream(file)));
//		ZipEntry ze = new ZipEntry("Table");
//		zos.putNextEntry(ze);
//		zos.write(content.getBytes("UTF-8"));
//		zos.close();
//	}

//	public static ZipOutputStream getZipOutputStream(File file)
//		throws IOException
//	{
//		ZipOutputStream zos
//			= new ZipOutputStream(
//				new BufferedOutputStream(
//					new FileOutputStream(file)));
//		ZipEntry ze = new ZipEntry("Table");
//		zos.putNextEntry(ze);
//		return zos;
//	}