package net.katros.services.clojure;

import static net.katros.services.utils.DeepCollectionUtils.indent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import clojure.lang.Keyword;

/**
 * An {@link ArrayList} that employs 'deep' operations on values that are {@link Map}s or {@link List}s.
 * 
 * Note that when putting elements that are maps or lists we should first transform them to {@link DeepClojureConfMap} and
 * {@link DeepClojureConfList}. This is to prevent values that are for example immutable maps which in turn may cause
 * failure of data modifying operations.
 * 
 * The class is not complete in the sense that not all possibly needed methods have been implemented.
 * 
 * @author doron
 *
 * @param <E>
 */
public class DeepClojureConfList<E> extends ArrayList<E>
{
	private final Comparator<? super Keyword> comparator;
	private static final long serialVersionUID = 1L;

	public DeepClojureConfList(Collection<? extends E> coll)
	{
		this(coll, null);
	}

	/**
	 * 
	 * @param coll
	 * @param comparator	used when creating inner {@link DeepClojureConfMap}s, may be null.
	 */
	public DeepClojureConfList(Collection<? extends E> coll, Comparator<? super Keyword> comparator)
	{
		this.comparator = comparator;
		this.addAll(coll);
	}

	@Override
	public boolean addAll(Collection<? extends E> coll)
	{
		for (E e : coll)
			add(e);
		return !coll.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(E e)
	{
		if (e instanceof DeepClojureConfMap<?, ?> || e instanceof DeepClojureConfList<?>)
			return super.add(e);
		if (e instanceof Map<?, ?>)
			return super.add((E)new DeepClojureConfMap<>((Map<? extends Keyword, ?>)e, comparator));
		if (e instanceof List<?>)
			return super.add((E)new DeepClojureConfList<>((List<?>)e, comparator)); // note that the inner list doesn't have to be of the same type as this list (this list's type is E)
		return super.add(e);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb, 0, true);
		return sb.toString();
	}

	/**
	 * 
	 * @param sb
	 * @param depth
	 * @param isSuccinctList	if the list doesn't contain any map or list present its elements in a single line.
	 */
	void toString(StringBuilder sb, int depth, boolean isSuccinctList)
	{
		if (isSuccinctList)
			toSuccinctString(sb, depth);
		else
			toRegularString(sb, depth, false);
	}

	private void toSuccinctString(StringBuilder sb, int depth)
	{
		if (doesContainMapOrList())
		{
			toRegularString(sb, depth, true);
			return;
		}
		if (depth > 0)
			sb.append(' ');
		sb.append('[');
		Iterator<E> it = iterator();
		if (!it.hasNext())
		{
			sb.append(']');
			return;
		}
		for (;;)
		{
			E value = it.next();
			if (value instanceof String)
				sb.append("\"" + value + "\"");
			else
				sb.append(value);
			if (!it.hasNext())
			{
				sb.append(']');
				return;
			}
			sb.append(' ');
		}
	}

	private void toRegularString(StringBuilder sb, int depth, boolean isSuccinctList)
	{
		if (depth > 0)
			sb.append('\n');
		indent(sb, depth, "[");
		for (Iterator<E> it = iterator(); it.hasNext(); )
		{
			E value = it.next();
			if (value instanceof DeepClojureConfMap<?, ?>)
				((DeepClojureConfMap<?, ?>)value).toString(sb, depth + 1, isSuccinctList);
			else if (value instanceof DeepClojureConfList<?>)
				((DeepClojureConfList<?>)value).toString(sb, depth + 1, isSuccinctList);
			else if (value instanceof String)
				indent(sb, "\n", depth + 1, "\"" + value + "\"");
			else
				indent(sb, "\n", depth + 1, String.valueOf(value));
		}
		indent(sb, "\n", depth, "]");
	}

	private boolean doesContainMapOrList()
	{
		for (Iterator<E> it = iterator(); it.hasNext(); )
		{
			E value = it.next();
			if (value instanceof DeepClojureConfMap<?, ?>
				|| value instanceof DeepClojureConfList<?>)
				return true;
		}
		return false;
	}
}