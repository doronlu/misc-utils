package net.katros.services.utils;

/**
 * Utils germane to processing time.
 * 
 * @author doron
 */
public class TimeUtils
{
	public static String nanosToHumanStr(long nanos)
	{
		if (nanos >= 1000000000)
			return String.format("%.2f", nanos / 1000000000d) + "s";
		if (nanos >= 1000000)
			return String.format("%.2f", nanos / 1000000d) + "ms";
		if (nanos >= 1000)
			return String.format("%.2f", nanos / 1000d) + "micros";
		return nanos + "ns";
	}
}