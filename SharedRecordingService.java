package net.katros.services.utils.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An MT-safe file writing service for use by multiple users writing to the same file.
 * Assumes not using different filenames for the same file.
 * 
 * @author doron
 */
public class SharedRecordingService
{
	private static Map<String, SharedRecordingService> sharedRecordingServices = new HashMap<>();
	private IRecordingService recordingService;
	private int numOfSubscribers;
	private final String filename;

	private SharedRecordingService(String filename)
	{
		this(filename, false);
	}

	/**
	 * 
	 * @param filename
	 * @param append
	 */
	private SharedRecordingService(String filename, boolean append)
	{
		this.filename = filename;
		recordingService = new RecordingService(new File(filename), new SimpleFileOutputStreamFactory(), append);
	}

	public static synchronized SharedRecordingService getInstance(String filename)
	{
		SharedRecordingService instance;
		if (!sharedRecordingServices.containsKey(filename))
		{
			instance = new SharedRecordingService(filename);
			sharedRecordingServices.put(filename, instance);
		}
		else
		{
			instance = sharedRecordingServices.get(filename);
			if (instance == null) // we already wrote to this file and closed it, now we want to append to it
			{
				instance = new SharedRecordingService(filename, true);
				sharedRecordingServices.put(filename, instance);
			}
		}
		instance.subscribe();
		return instance;
	}

	public void print(String str)
	{
		print(str.getBytes());
	}

	public synchronized void print(byte[] bytes)
	{
		recordingService.print(bytes);
	}

	private synchronized void subscribe()
	{
		numOfSubscribers++;
	}

	public synchronized void unsubscribe() throws IOException
	{
		numOfSubscribers--;
		if (numOfSubscribers == 0)
		{
			recordingService.close();
			sharedRecordingServices.put(filename, null); // by putting null and keeping the key we know that we already wrote to this file so later on we'll append to it
		}
	}

	public synchronized void unsubscribeEatException()
	{
		numOfSubscribers--;
		if (numOfSubscribers == 0)
		{
			recordingService.closeEatException();
			sharedRecordingServices.put(filename, null); // by putting null and keeping the key we know that we already wrote to this file so later on we'll append to it
		}
	}

	public static void main(String[] args)
	{
		Runnable r1 = new Runnable()
		{
			@Override
			public void run()
			{
				SharedRecordingService s = getInstance("/tmp/doron/filename1.txt");
				s.print("1 before\n");
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
				s.print("1 after\n");
				s.unsubscribeEatException();
			}
		};
		Runnable r2 = new Runnable()
		{
			@Override
			public void run()
			{
				SharedRecordingService s = getInstance("/tmp/doron/filename1.txt");
				s.print("2 before\n");
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
				s.print("2 after\n");
				s.unsubscribeEatException();
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
//				s.print("trying to write after file had been closed\n");
			}
		};
		Runnable r3 = new Runnable()
		{
			@Override
			public void run()
			{
				SharedRecordingService s = getInstance("/tmp/doron/filename1.txt");
				s.print("3 before\n");
				try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
				s.print("3 after\n");
				s.unsubscribeEatException();
			}
		};
		Thread t1 = new Thread(r1);
		t1.setName("t1");
		Thread t2 = new Thread(r2);
		t2.setName("t2");
		t1.start();
		t2.start();
		try { t2.join(); } catch (InterruptedException e) { e.printStackTrace(); }
		try { t1.join(); } catch (InterruptedException e) { e.printStackTrace(); }
		new Thread(r3).start();
	}
}