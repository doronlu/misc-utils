package net.katros.services.utils.io;

import static java.lang.System.out;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Writes to the log and to stdout/stderr.
 * This is useful in cases where the log might have already been shut down.
 * 
 * @author doron
 */
public class LogAndStdout
{
	public static void write(Logger log, Level level, String str)
	{
		out.println(str);
		log.log(level, str);
	}

	public static void write(Logger log, Level level, String str, Throwable t)
	{
		out.println(str);
		log.log(level, str, t);
		t.printStackTrace();
	}
}