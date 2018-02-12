package net.katros.services.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;

/**
 * Utilities related to spawning and running processes.
 * 
 * @author doron
 */
public abstract class ProcessUtils
{
	/**
	 * Will execute the specified content as a shell script, returning its stdout and
	 * stderr combined.
	 * Supports only POSIX systems.
	 * 
	 * @param fileContent
	 * @return
	 * @throws IOException
	 */
	public static List<String> runScriptContent(String fileContent) throws IOException
	{
		Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwx------");
		FileAttribute<Set<PosixFilePermission>> fileAttribute = PosixFilePermissions.asFileAttribute(permissions);
		Path tmpFile = Files.createTempFile("tmpScript_", ".sh", fileAttribute);
//	    System.out.format("The temporary file" + " has been created: %s%n", tmpFile);
	    Files.write(tmpFile, fileContent.getBytes());
	    List<String> result = runScriptFile(tmpFile);
	    Files.delete(tmpFile);
	    return result;
	}

	/**
	 * Will execute the specified file as a process, returning its stdout and
	 * stderr combined.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static List<String> runScriptFile(Path path) throws IOException
	{
		List<String> result = new ArrayList<>();
		ProcessBuilder processBuilder = new ProcessBuilder(path.toRealPath().toString());
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream())))
		{
	        String s = null;
	        while ((s = stdInput.readLine()) != null)
	        	result.add(s);
		}
		return result;
	}

	/**
	 * We don't use System.getenv() for security reasons - it returns an unmodifiable map that contains the password and
	 * will remain in memory until garbage collected.
	 */
	public static String getEnvironmentParams(Logger log)
	{
		try
		{
			return ProcessUtils.getEnvironmentParams();
		}
		catch (IOException e)
		{
			log.warn("An exception occurred when retrieving environment variables", e);
			return "";
		}
	}

	/**
	 * We don't use System.getenv() for security reasons - it returns an unmodifiable map that contains the password and
	 * will remain in memory until garbage collected.
	 */
	public static String getEnvironmentParams() throws IOException
	{
		return getEnvironmentParams(false);
	}

	/**
	 * We don't use System.getenv() for security reasons - it returns an unmodifiable map that contains the password and
	 * will remain in memory until garbage collected.
	 */
	public static String getEnvironmentParams(boolean isGetPasswords) throws IOException
	{
		StringBuilder sb = new StringBuilder(9000);
		Properties props = System.getProperties();
		Set<Entry<Object, Object>> entries = props.entrySet();
		SortedMap<String, String> map = new TreeMap<>();
		for (Entry<Object, Object> entry : entries)
			map.put(entry.getKey().toString(), entry.getValue().toString());
		sb.append("Java properties:\n");
		for (Entry<String, String> e : map.entrySet())
		{
			String key = e.getKey();
			if (key.equals("java.class.path"))
			{
				sb.append("  " + key + "=\n");
				split(e.getValue(), sb);
			}
			else if (key.equals("sun.boot.class.path"))
			{
				sb.append("  " + key + "=\n");
				split(e.getValue(), sb);
			}
			else
				sb.append("  " + key + "=" + e.getValue() + "\n");
		}
		sb.append("Environment variables:\n");
		List<String> result = isGetPasswords ? getSystemEnv() : ProcessUtils.runScriptContent("env | grep -v PASS | grep -v STS_USERNAME");
		Collections.sort(result);
		for (String s : result)
		{
			if (s.startsWith("LD_LIBRARY_PATH="))
			{
				sb.append("  LD_LIBRARY_PATH=\n");
				split(s.substring("LD_LIBRARY_PATH=".length()), sb);
			}
			else if (s.startsWith("PATH="))
			{
				sb.append("  PATH=\n");
				split(s.substring("PATH=".length()), sb);
			}
			else
				sb.append("  " + s + "\n");
		}
		return sb.toString();
	}

	private static List<String> getSystemEnv()
	{
		List<String> result = new ArrayList<>();
		Map<String, String> env = System.getenv();
		for (Map.Entry<String, String> entry : env.entrySet())
			result.add(entry.getKey() + "=" + entry.getValue());
		return result;
	}

	private static void split(String s, StringBuilder sb)
	{
		String[] elements = s.split(File.pathSeparator);
		for (String element : elements)
			sb.append("    " + element + "\n");
	}

	/**
	 * http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id
	 * Note that since this code uses sun.management.VMManagement which is not part of the API it will run only on Sun's
	 * JRE and may be forward-incompatible.
	 * 
	 * @param log	optional.
	 * @return	the process ID, -1 if couldn't find it.
	 */
	public static int findPid(Logger log)
	{
		try
		{
			java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
			java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			@SuppressWarnings("restriction")
			sun.management.VMManagement mgmt = (sun.management.VMManagement)jvm.get(runtime);
			java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);
			return (Integer)pid_method.invoke(mgmt);
		}
		catch (Throwable t)
		{
			if (log != null)
				log.warn("An exception occurred while trying to extract PID", t);
			return -1;
		}
	}

	public static void main(String[] args) throws IOException
	{
		List<String> result = runScriptContent("env | grep -v PASS | grep -v STS_USERNAME");
		Collections.sort(result);
		for (String s : result)
			System.out.println(s);
	}
}