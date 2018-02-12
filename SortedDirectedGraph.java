package net.katros.services.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A directed graph, methods that return nodes return them sorted by their natural order.
 * 
 * @author doron
 *
 * @param <T>
 */
public class SortedDirectedGraph<T extends Comparable<? super T>>
{
	private final Map<T, Set<T>> graph = new TreeMap<>(); // a map from each node to the nodes its directed edges go to
	private final Map<T, Set<T>> reverseGraph = new TreeMap<>(); // we maintain this for a more efficient? removal of nodes

	public SortedDirectedGraph(Collection<T> nodes)
	{
		for (T node : nodes)
			addNode(node);
	}

	@SafeVarargs // see http://docs.oracle.com/javase/8/docs/technotes/guides/language/non-reifiable-varargs.html
	public SortedDirectedGraph(T... nodes)
	{
		for (T node : nodes)
			addNode(node);
	}

	/**
	 * Add a node to this graph.
	 * 
	 * @param node	assumed to not already be in the graph.
	 */
	public void addNode(T node)
	{
		graph.put(node, new TreeSet<T>());
		reverseGraph.put(node, new TreeSet<T>());
	}

	/**
	 * Assumes both nodes exist.
	 * 
	 * @param fromNode
	 * @param toNode
	 */
	public void addEdge(T fromNode, T toNode)
	{
		graph.get(fromNode).add(toNode);
		reverseGraph.get(toNode).add(fromNode);
	}

	public void addEdgeSafe(T fromNode, T toNode) throws InvalidArgumentException
	{
		if (!isNodeExists(fromNode))
			throw new InvalidArgumentException(fromNode + " doesn't exist");
		if (!isNodeExists(toNode))
			throw new InvalidArgumentException(toNode + " doesn't exist");
		addEdge(fromNode, toNode);
	}

	/**
	 * 
	 * @param fromNode
	 * @return	nodes pointed to from the specified node.
	 */
	public Set<T> getToNodes(T fromNode)
	{
		return graph.get(fromNode);
	}

	/**
	 * 
	 * @param toNode
	 * @return	nodes pointing to the specified node.
	 */
	public Set<T> getFromNodes(T toNode)
	{
		return reverseGraph.get(toNode);
	}

	/**
	 * Removes a node and all its edges.
	 * 
	 * @param node
	 * @return	nodes that where pointed to by the removed node.
	 */
	public Set<T> remove(T node)
	{
		Set<T> pointingToRemovedNode = reverseGraph.remove(node);
		for (T pointingNode : pointingToRemovedNode)
		{
			Set<T> pointedByAPointingNode = graph.get(pointingNode);
			pointedByAPointingNode.remove(node);
		}
		Set<T> pointedByRemovedNode = graph.remove(node);
		for (T pointingNodeInReverseGraph : pointedByRemovedNode)
		{
			Set<T> pointedByAPointingNode = reverseGraph.get(pointingNodeInReverseGraph);
			pointedByAPointingNode.remove(node);
		}
		return pointedByRemovedNode;
	}

	/**
	 * 
	 * @param node
	 * @return	true iff there are no edges directed to the specified node.
	 */
	public boolean isSource(T node)
	{
		return reverseGraph.get(node).isEmpty();
	}

	/**
	 * The returned {@link Set} will reflect any future changes to the underlying {@link Map#keySet()}.
	 * 
	 * @return
	 */
	public Set<T> getNodes()
	{
		return graph.keySet();
	}

	/**
	 * The returned {@link Set} is a shallow copy of the underlying {@link Map#keySet()}.
	 * 
	 * @return
	 */
	public Set<T> getNodesCopy()
	{
		Set<T> copy = new TreeSet<>();
		for (T node : graph.keySet())
			copy.add(node);
		return copy;
	}

	/**
	 * Returns true iff this graph contains no nodes.
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		return graph.isEmpty();
	}

	public boolean isNodeExists(T node)
	{
		return graph.containsKey(node);
	}

	public int size()
	{
		return graph.size();
	}

	@Override
	public String toString()
	{
		return graph.toString();
	}
}