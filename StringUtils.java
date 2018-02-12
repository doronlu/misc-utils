package net.katros.services.utils;

import static org.apache.logging.log4j.Level.INFO;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Some {@link String} utilities.
 * 
 * @author doron
 */
public class StringUtils
{
	private static final String[] ZEROS = {
		"00000000",
		"0000000",
		"000000",
		"00000",
		"0000",
		"000",
		"00",
		"0"
	};

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Left pads with zeros to length 9.
	 * Eg. 307 -> 000000307
	 * @param guid
	 * @return
	 */
	public static String zeroPad(int guid)
	{
		String guidStr = String.valueOf(guid);
		int length = guidStr.length();
		if (length > 8)
			return guidStr;
		return ZEROS[length - 1] + guidStr;
	}

	/**
	 * Indents a matrix of strings.
	 *
	 * @param matrix			all rows are assumed to be of the same size
	 * 							(number of strings).
	 * @param fieldSeparator	for example my be "," or ", " or "  "
	 * @param rowSeparator		would usually be line separator, as in "\n"
	 * @return
	 */
	public static String toString(String[][] matrix, String fieldSeparator,
			String rowSeparator, boolean isLeftJustified)
	{
		if (matrix.length == 0)
			return "";
		String leftJustification = isLeftJustified ? "-" : "";
		int numOfColumns = matrix[0].length;
		int[] sizes = new int[numOfColumns];
		for (int i = 0; i < matrix.length; i++)
		{
			for (int j = 0; j < numOfColumns; j++)
			{
				if (sizes[j] < matrix[i][j].length())
					sizes[j] = matrix[i][j].length();
			}
		}
		StringBuilder sb = new StringBuilder(1000);
		for (int i = 0; i < matrix.length; i++)
		{
			for (int j = 0; j < numOfColumns; j++)
			{
				sb.append(String.format("%" + leftJustification + sizes[j]
					+ "s", matrix[i][j]));
				if (j < numOfColumns - 1)
					sb.append(fieldSeparator);
			}
			if (i < matrix.length - 1)
				sb.append(rowSeparator);
		}
		return sb.toString();
	}

	/**
	 * Useful for example for parsing CSV files.
	 * 
	 * @param s
	 * @param c
	 * @return
	 */
	public static String[] split(String s, char c)
	{
		Deque<String> deque = new ArrayDeque<>();
		char[] chars = s.toCharArray();
		int last = 0;
		for (int i = 0; i < s.length(); i++)
		{
			if (chars[i] == c)
			{
				deque.add(s.substring(last, i));
				last = i + 1;
			}
		}
		deque.add(s.substring(last, s.length()));
		return deque.toArray(new String[0]);
	}

	public static void main(String... args)
	{
		testSplit();
		testToString();
	}

	private static void testSplit()
	{
		testSplit("12,34");
		testSplit("12,3,4");
		testSplit("12,3,,4");
		testSplit("12,3,,,,4");
		testSplit(",,,12,3,,,,4,,,");
		testSplit("");
		testSplit("123");
		testSplit(",");
		testSplit(",,,,");
	}

	private static void testSplit(String input)
	{
		String[] strs = split(input, ',');
		System.out.println(Arrays.asList(strs));
	}

	private static void testToString()
	{
		String[][] matrix =
		{
			{ "statType", "xchgToReceive", "receiveToStrgy" },
			{ "averageBlah", "6284571", "1738960" },
			{ "median", "-107000", "204447" }
		};
		System.out.print(toString(matrix, " ", "\n", false));
	}
	
	public static Map<String, String>  ToMap(String[] keys, String[] values)
	{
		Map<String, String> map = new HashMap<String, String>();
		for (int i=0; i<keys.length; i++)
			map.put(keys[i], values[i]);
		return map;
	}

	public static void print(String s)
	{
		print(INFO, s);
	}

	public static void print(Level level, String s)
	{
		String msg = String.format("%tT  %-16s %s\n", Calendar.getInstance(), Thread.currentThread().getName(), s);
//		if (level.compareTo(INFO) < 0)
//			System.err.print(msg);
//		else
//			System.out.print(msg); // TODO comment out
		LOG.log(level, msg);
	}
}