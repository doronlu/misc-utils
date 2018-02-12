package net.katros.services.utils.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import net.katros.services.utils.InvalidArgumentException;

/**
 * Miscellaneous file utilities.
 * 
 * @author doron
 */
public abstract class FileUtils
{
	public static String readFileOrResourceToString(String name) throws InvalidArgumentException
	{
		return name.startsWith("/") ? readFileToString(name) : readResourceToString(name);
	}

	public static String readFileToString(String filename) throws InvalidArgumentException
	{
		try
		{
			return org.apache.commons.io.FileUtils.readFileToString(new File(filename));
		}
		catch (IOException e)
		{
			throw new InvalidArgumentException("A problem while trying to read file " + filename, e);
		}
	}

	public static String readResourceToString(String resourceName) throws InvalidArgumentException
	{
		URL url = FileUtils.class.getClassLoader().getResource(resourceName);
		if (url == null)
			throw new InvalidArgumentException("Resource " + resourceName + " was not found or privileges are inadequate to get it");
		try
		{
			File file = new File(url.toURI());
			return org.apache.commons.io.FileUtils.readFileToString(file);
		}
		catch (IOException | URISyntaxException e)
		{
			throw new InvalidArgumentException("A problem while trying to read resource " + resourceName, e);
		}
	}

	/**
	 * Should be identical to the first implementation, the former uses Apache while this one uses Google.
	 * 
	 * @param resourceName
	 * @return
	 * @throws InvalidArgumentException
	 */
	public static String readResourceToStringImplementation2(String resourceName) throws InvalidArgumentException
	{
		URL url = Resources.getResource(resourceName);
		if (url == null)
			throw new InvalidArgumentException("Resource " + resourceName + " was not found or privileges are inadequate to get it");
		try
		{
			return Resources.toString(url, Charsets.UTF_8);
		}
		catch (IOException e)
		{
			throw new InvalidArgumentException("A problem while trying to read resource " + resourceName, e);
		}
	}

	/**
	 * 
	 * @param resourceName
	 * @return	an empty {@link Optional} if the resource specified by resourceName doesn't exist.
	 * @throws InvalidArgumentException
	 */
	public static Optional<String> getPathOfResource(String resourceName) throws InvalidArgumentException
	{
		URL url = FileUtils.class.getClassLoader().getResource(resourceName);
		return Optional.ofNullable(url == null ? null : getPathOfResource(url));
	}

	/**
	 * This solves the problem that a space in the path would appear as %20 in the returned String which in turn caused
	 * the file to not be found.
	 * Wasn't tested when the resource is in a jar file for example, might err there.
	 * 
	 * @param url
	 * @return
	 * @throws InvalidArgumentException
	 * @see http://stackoverflow.com/questions/3263560/sysloader-getresource-problem-in-java
	 */
	private static String getPathOfResource(URL url) throws InvalidArgumentException
	{
		try
		{
			return new URI(url.toString()).getPath();
		}
		catch (URISyntaxException e)
		{
			throw new InvalidArgumentException("A problem while manipulating URL=" + url, e);
		}
	}

	public static boolean contentEquals(Path path1, Path path2) throws IOException
	{
		return org.apache.commons.io.FileUtils.contentEquals(new File(path1.toString()), new File(path2.toString()));
	}

	/**
	 * Returns true iff the specified strings appear in the specified file in order.
	 * See {@link Scanner} for details.
	 * 
	 * @param fileName
	 * @param regex
	 * @return
	 * @throws FileNotFoundException
	 */
	public static boolean isRegexInFileInOrder(String fileName, String... regex)
		throws FileNotFoundException
	{
		return isRegexInFileInOrder(new File(fileName), regex);
	}

	/**
	 * Returns true iff the specified strings appear in the specified file in order.
	 * See {@link Scanner} for details.
	 * 
	 * @param file
	 * @param regex
	 * @return
	 * @throws FileNotFoundException
	 */
	public static boolean isRegexInFileInOrder(File file, String... regex)
		throws FileNotFoundException
	{
		try (Scanner scanner = new Scanner(file))
		{
			for (String pattern : regex)
			{
				String result = scanner.findWithinHorizon(pattern, 0);
				if (result == null)
					return false;
			}
			return true;
		}
	}

	public static void toFile(File file, String content) throws FileNotFoundException
	{
		try (PrintWriter out = new PrintWriter(file))
		{
			out.print(content);
		}
	}

	public static void toFile(String filename, String content) throws FileNotFoundException
	{
		try (PrintWriter out = new PrintWriter(filename))
		{
			out.print(content);
		}
	}

	// for testing isStringsInFileInOrder()
	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println(isRegexInFileInOrder(
			"/home/doron/tmp/test.txt", "one", "two ", " three four ", "five", "si[_0-9]*x"));
		System.out.println(isRegexInFileInOrder(
			"/home/doron/safe/testarea/acceptanceTesting/tmp/stdout_aggregator.txt", "_100000003| IDLE Mode: rejected as potential position (41) > holding limit (40)"));
		System.out.println(isRegexInFileInOrder(
			"/home/doron/safe/testarea/acceptanceTesting/tmp/stdout_aggregator.txt", " IDLE Mode: rejected as potential position \\(41\\) > holding limit \\(40\\)"));
		System.out.println(isRegexInFileInOrder(
			"/home/doron/safe/testarea/acceptanceTesting/tmp/stdout_aggregator.txt", "_100000003| IDLE Mode: rejected as potential position \\(41\\) > holding limit \\(40\\)"));
		System.out.println(isRegexInFileInOrder(
			"/home/doron/safe/testarea/acceptanceTesting/tmp/stdout_aggregator.txt", " IDLE Mode: rejected as potential position (41) > holding limit (40)"));
		System.out.println(isRegexInFileInOrder(
			"/home/doron/safe/testarea/acceptanceTesting/tmp/stdout_aggregator.txt", " IDLE Mode: rejected as potential position (41) > holding limit (0)"));
		System.out.println(isRegexInFileInOrder(
			"/home/doron/safe/testarea/acceptanceTesting/tmp/stdout_aggregator.txt", " IDLE Mode: rejected as potential position "));
	}
}