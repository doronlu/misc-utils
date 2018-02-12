package net.katros.utils.disrupt2;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * @author doron
 */
public class TestDisruptor2
{
	private final static String USAGE =
		"Usage example:\n"
		+ "java -cp target/utils-disrupt-0.1.0-jar-with-dependencies.jar net.katros.utils.disrupt.TestDisruptor 200 10000";

	private static int numOfStrategies;
	private static int numOfEvents;

	public static void main(String[] args) throws Exception
	{
		if (args.length != 2)
		{
			System.out.println(USAGE);
			System.exit(1);
		}
		numOfStrategies = Integer.parseInt(args[0]);
		numOfEvents = Integer.parseInt(args[1]);
		execute(new SleepingWaitStrategy()); // my understanding: busy wait loop, initially spins trying to consume, after repeated failures calls Thread.yield(), eventually sleeps by calling LockSupport.parkNanos(1)
		execute(new YieldingWaitStrategy()); // my understanding: like SleepingWaitStrategy but without the LockSupport.parkNanos(1) part
		execute(new BusySpinWaitStrategy()); // my understanding: busy loop, no yield no park
		execute(new BlockingWaitStrategy());
	}

	private static void execute(WaitStrategy waitStrategy)
	{
		// Connect the handler
		EventHandler<LongEvent>[] strategies = new LongEventHandler[numOfStrategies];
		for (int i = 0; i < numOfStrategies; i++)
			strategies[i] = new LongEventHandler(1001 + i);
		DisruptorIQueueAdapter disruptorIQueueAdapter = new DisruptorIQueueAdapter(waitStrategy, strategies);
		disruptorIQueueAdapter.start();
		ByteBuffer bb = ByteBuffer.allocate(8);
		System.out.println("Starting");
		long start = System.nanoTime();
		for (long l = 1; l <= numOfEvents; l++)
		{
			bb.putLong(0, l);
			disruptorIQueueAdapter.put(bb);
//			Thread.sleep(1000);
		}
		disruptorIQueueAdapter.shutdown();
		long end = System.nanoTime();
//		System.out.println(handler1);
		System.out.println(strategies[numOfStrategies - 1]);
		long timePerEvent = Math.round((end - start) / (double)numOfEvents);
		String timeStr = new DecimalFormat().format(timePerEvent);
		System.out.println("#consumers=" + numOfStrategies + "\t#events=" + numOfEvents
			+ "\tWaitStrategy=" + waitStrategy.getClass().getSimpleName() + "\ttimePerEvent=" + timeStr + " ns");
	}
}

class DisruptorIQueueAdapter
{
	final Disruptor<LongEvent> disruptor;
	final LongEventProducer producer;

	DisruptorIQueueAdapter(WaitStrategy waitStrategy, EventHandler<LongEvent>[] strategies)
	{
		ThreadFactory threadFactory
			= new ThreadFactoryBuilder()
				.setDaemon(false)
				.setNameFormat("ConsumerThread-%d")
//				.setUncaughtExceptionHandler(new ErrorLoggingUncaughtExceptionHandler(LOG))
				.setPriority(Thread.MIN_PRIORITY)
				.build();

		// The factory for the event
		EventFactory<LongEvent> factory = new LongEventFactory();

		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 65536;

		// Construct the Disruptor
		disruptor = new Disruptor<>(
			factory, 
			bufferSize,
			threadFactory,
			ProducerType.SINGLE, // Single producer
			waitStrategy);
//		LongEventHandler handler1 = new LongEventHandler(1);
//		disruptor.handleEventsWith(handler1);
//		disruptor.after(handler1).handleEventsWith(strategies);
		disruptor.handleEventsWith(strategies);

		// Get the ring buffer from the Disruptor to be used for publishing.
		RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
		producer = new LongEventProducer(ringBuffer);
	}

	void start()
	{
		disruptor.start();
	}

	void put(ByteBuffer bb)
	{
		producer.onData(bb);
	}

	void shutdown()
	{
		disruptor.shutdown();
	}
}

//class LongEventProducerWithTranslator
//{
//	private final RingBuffer<LongEvent> ringBuffer;
//
//	private static final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR =
//		new EventTranslatorOneArg<LongEvent, ByteBuffer>()
//		{
//			public void translateTo(LongEvent event, long sequence, ByteBuffer bb)
//			{
//				event.set(bb.getLong(0));
//			}
//		};
//
//	public LongEventProducerWithTranslator(RingBuffer<LongEvent> ringBuffer)
//	{
//		this.ringBuffer = ringBuffer;
//	}
//
//	public void onData(ByteBuffer bb)
//	{
//		ringBuffer.publishEvent(TRANSLATOR, bb);
//	}
//}

class LongEventProducer
{
	private final RingBuffer<LongEvent> ringBuffer;

	public LongEventProducer(RingBuffer<LongEvent> ringBuffer)
	{
		this.ringBuffer = ringBuffer;
	}

	public void onData(ByteBuffer bb)
	{
		long sequence = ringBuffer.next();  // Grab the next sequence
		try
		{
			LongEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			event.set(bb.getLong(0));  // Fill with data
		}
		finally
		{
			ringBuffer.publish(sequence);
		}
	}
}

class LongEventHandler implements EventHandler<LongEvent>
{
	private final int id;
	private long sum;

	public LongEventHandler(int id)
	{
		this.id = id;
	}

	public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
	{
		sum += event.get();
//		System.out.println(id + " Event: " + event);
	}

	@Override
	public String toString()
	{
		return id + ": sum=" + sum;
	}
}

class LongEventFactory implements EventFactory<LongEvent>
{
	public LongEvent newInstance()
	{
		return new LongEvent();
	}
}

class LongEvent
{
	private long value;

	public void set(long value)
	{
		this.value = value;
	}

	public long get()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return String.valueOf(value);
	}
}