package net.katros.services.utils.concurrent;

import static net.katros.services.utils.StringUtils.print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IScheduler} that processes each added element immediately, in the thread adding the element.
 * MT-safe.
 * 
 * @author doron
 *
 * @param <E>	the element type.
 * @param <V>	the return type from the {@link ElementsProcessor}.
 */
public class ExecuteNowScheduler<E, V> implements IScheduler<E, V>
{
	private final ElementsProcessor<E, V> elementsProcessor;
	private final List<E> elements = new ArrayList<>();

	public ExecuteNowScheduler(ElementsProcessor<E, V> elementsProcessor)
	{
		this.elementsProcessor = elementsProcessor;
		elements.add(null);
	}

	@Override
	public synchronized void add(E element) throws InterruptedException
	{
		elements.set(0, element);
		elementsProcessor.process(elements);
	}

	@Override
	public synchronized void close() throws IOException
	{
		print(ExecuteNowScheduler.class.getSimpleName() + ".close() terminated successfully");
	}
}