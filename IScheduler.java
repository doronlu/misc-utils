package net.katros.services.utils.concurrent;

import java.io.Closeable;

/**
 * Collects elements and executes them sometime in the future using an {@link ElementsProcessor} that is specified in
 * the c'tor.
 * 
 * @author doron
 *
 * @param <E>	the element type.
 * @param <V>	the return type from the {@link ElementsProcessor}.
 */
public interface IScheduler<E, V> extends Closeable
{
	public void add(E element) throws InterruptedException;
}