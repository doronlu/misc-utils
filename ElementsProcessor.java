package net.katros.services.utils.concurrent;

import static net.katros.services.utils.StringUtils.print;
import static org.apache.logging.log4j.Level.ERROR;

import java.util.Collection;

/**
 * Can process a {@link Collection} of elements.
 * 
 * @author doron
 *
 * @param <E>	element type.
 * @param <V>	the result type of method {@link #process(Collection)}.
 */
public interface ElementsProcessor<E, V>
{
	public V process(Collection<E> elements);

	/**
	 * The implementation may want to hold the underlying {@link Collection} to promote its reuse.
	 * 
	 * @return
	 */
	default public Collection<E> getCollection()
	{
		print(ERROR, "Unsupported operation");
		throw new UnsupportedOperationException();
	}
}