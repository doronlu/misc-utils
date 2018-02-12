package net.katros.services.utils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

/**
 * An {@link ArrayDeque} extension that supports specifying a boundary on its capacity.
 * When an insert would exceed the boundary an element will be removed from the side opposite to the one the element was
 * inserted at.
 * If the user always inserts on the same side and never removes from that side this will effectively become an LRU
 * linear collection. I.e. when an insertion is made to a collection that is already at capacity the Least Recently Used
 * element is the one removed.
 * This differs from a 'regular' limited capacity {@link Deque} in that the latter rejects the insertion if the Deque is
 * already filled to capacity.
 * 
 * @author doron
 *
 * @param <E>
 */
public class LruArrayDeque<E> extends ArrayDeque<E>
{
	private static final long serialVersionUID = -2313757204268905799L;
	private final int capacity;

	public LruArrayDeque()
	{
		this.capacity = Integer.MAX_VALUE;
	}

	public LruArrayDeque(int capacity)
	{
		this.capacity = capacity;
	}

	public LruArrayDeque(int numElements, int capacity)
	{
		super(numElements);
		this.capacity = capacity;
	}

	public LruArrayDeque(Collection<? extends E> c, int capacity)
	{
		this.capacity = capacity;
		addAll(c);
	}

	@Override
	public void push(E e)
	{
		addFirst(e);
	}

	@Override
	public void addFirst(E e)
	{
		super.addFirst(e);
		if (super.size() > capacity)
			super.removeLast();
	}

	@Override
	public boolean offerFirst(E e)
	{
		if (!super.offerFirst(e))
			return false;
		if (super.size() > capacity)
			super.removeLast();
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		for (E e : c)
			addLast(e);
		return true;
	}

	@Override
	public boolean add(E e)
	{
		addLast(e);
		return true;
	}

	@Override
	public void addLast(E e)
	{
		super.addLast(e);
		if (super.size() > capacity)
			super.removeFirst();
	}

	@Override
	public boolean offer(E e)
	{
		return offerLast(e);
	}

	@Override
	public boolean offerLast(E e)
	{
		if (!super.offerLast(e))
			return false;
		if (super.size() > capacity)
			super.removeFirst();
		return true;
	}
}