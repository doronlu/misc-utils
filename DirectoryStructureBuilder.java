package net.katros.services.utils.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Given instructions in the form of int[] will split the specified Strings
 * according to those instructions and use it to build a hierarchical directory
 * structure.
 * 
 * Examples:
 * DirectoryStructureBuilder.asFile("root", "abcd", ".zip", 1, 2, 3, 4)
 * will result in: root/a/bc/d.zip
 * DirectoryStructureBuilder.asFile("root", "abcd", ".zip", 1, 2)
 * will result in: root/a/bc/d.zip
 * DirectoryStructureBuilder.asFile("root", "abcd", ".zip", 3)
 * will result in: root/abc/d.zip
 * DirectoryStructureBuilder.asFile("root", "abcd", ".zip", 4)
 * will result in: root/abcd.zip
 * DirectoryStructureBuilder.asFile("root", "abcd", ".zip", 5)
 * will result in: root/abcd.zip
 * 
 * Note that the directory structure is created on disk but the file is not.
 * 
 * @author doron
 */
public class DirectoryStructureBuilder
{
	/**
	 * These determine how the strings will be split, i.e. what the directory
	 * structure will be.
	 */
	private final int[] lengths;

	/**
	 * 
	 * @param lengths	all assumed to be >0. They determine how the strings
	 * 					will be split, i.e. what the directory structure will be.
	 */
	public DirectoryStructureBuilder(int... lengths)
	{
		this.lengths = lengths;
	}

	/**
	 * Builds a directory structure at the specified root by splitting the
	 * specified preSplitFilename according to the specified lengths, then
	 * adding the specified extension.
	 * 
	 * @param root
	 * @param preSplitFilename
	 * @param extension	the file extension, should include the '.'
	 * @return	the resulting file object. The directory structure is created on
	 * 			disk but the file is not.
	 */
	public File asFile(String root, String preSplitFilename, String extension)
	{
		return asFile(root, preSplitFilename, extension, lengths);
	}

	/**
	 * Builds a directory structure at the specified root by splitting the
	 * specified preSplitFilename according to the specified lengths, then
	 * adding the specified extension.
	 * 
	 * @param root
	 * @param preSplitFilename
	 * @param extension	the file extension, should include the '.'
	 * @param lengths	all assumed to be >0. They determine how the strings
	 * 					will be split, i.e. what the directory structure will be.
	 * @return	the resulting file object. The directory structure is created on
	 * 			disk but the file is not.
	 * TODO consider returning filename as opposed to a File and doing the {@link #mkdirs(File)} elsewhere
	 */
	public static File asFile(String root, String preSplitFilename,
			String extension, int... lengths)
	{
		String[] strings = split(preSplitFilename, lengths);
		StringBuilder sb
			= new StringBuilder(root.length() + preSplitFilename.length()
				+ extension.length() + lengths.length + 1);
		sb.append(root);
		for (int i = 0; i < strings.length - 1; i++)
			sb.append(File.separator).append(strings[i]);
		File path = new File(sb.toString());
		mkdirs(path);
		File file = new File(path, strings[strings.length - 1] + extension);
		return file;
	}

	/**
	 * Makes directories in a safe (synchronized) way.
	 * 
	 * @param pathStr
	 */
	public static void mkdirs(String pathStr)
	{
		if (pathStr != null)
			mkdirs(new File(pathStr));
	}

	static synchronized void mkdirs(File path)
	{
		path.mkdirs();
	}

	/**
	 * 
	 * @param preSplitFilename	assumed to be at least 1 character in length.
	 * @param lengths	all assumed to be >0. They determine how the strings
	 * 					will be split, i.e. what the directory structure will be.
	 * @return			the specified preSplitFilename after it is split
	 * 					according to the specified lengths.
	 */
	static String[] split(String preSplitFilename, int... lengths)
	{
		List<String> strings = new ArrayList<String>();
		split(strings, preSplitFilename, 1, lengths);
		return strings.toArray(new String[0]);
	}

	private static void split(List<String> strings, String remainder, int i, int[] lengths)
	{
		if (lengths.length < i || remainder.length() <= lengths[i - 1])
		{
			strings.add(remainder);
			return;
		}
		strings.add(remainder.substring(0, lengths[i - 1]));
		split(strings, remainder.substring(lengths[i - 1]), i + 1, lengths);
	}
}