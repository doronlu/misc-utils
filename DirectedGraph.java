package net.katros.services.utils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A directed graph, methods that return nodes return them in the order received.
 * Note that a simpler and faster implementation is easily possible when the requirement for the order of returned nodes
 * is omitted.
 * 
 * @author doron
 *
 * @param <T>
 */
public class DirectedGraph<T>
{
	private final Map<T, Set<T>> graph = new HashMap<>(); // a map from each node to the nodes its directed edges go to
	private final Map<T, Set<T>> reverseGraph = new HashMap<>(); // we maintain this for a more efficient? removal of nodes
	private final Deque<T> nodesInOrderAdded = new ArrayDeque<>(); // used only for ordering outputs

	public DirectedGraph(Collection<T> nodes)
	{
		for (T node : nodes)
			addNode(node);
	}

	@SafeVarargs // see http://docs.oracle.com/javase/8/docs/technotes/guides/language/non-reifiable-varargs.html
	public DirectedGraph(T... nodes)
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
		graph.put(node, new HashSet<T>());
		reverseGraph.put(node, new HashSet<T>());
		nodesInOrderAdded.add(node);
	}

	public void addNodeIfNotExists(T node)
	{
		if (!isNodeExists(node))
			addNode(node);
	}

	/**
	 * Assumes both nodes exist; adding an existing edge is fine as it doesn't change the DAG.
	 * 
	 * @param fromNode
	 * @param toNode
	 */
	public void addEdge(T fromNode, T toNode)
	{
		graph.get(fromNode).add(toNode);
		reverseGraph.get(toNode).add(fromNode);
	}

	/**
	 * Will complain if one of the nodes doesn't exist; adding an existing edge is fine as it doesn't change the DAG.
	 * 
	 * @param fromNode
	 * @param toNode
	 * @throws InvalidArgumentException
	 */
	public void addEdgeSafe(T fromNode, T toNode) throws InvalidArgumentException
	{
		if (!isNodeExists(fromNode))
			throw new InvalidArgumentException(fromNode + " doesn't exist");
		if (!isNodeExists(toNode))
			throw new InvalidArgumentException(toNode + " doesn't exist");
		addEdge(fromNode, toNode);
	}

	/**
	 * Adding an existing node or edge is fine as it doesn't change the DAG.
	 * 
	 * @param fromNode
	 * @param toNode
	 */
	public void addEdgeAndNodesIfNeeded(T fromNode, T toNode)
	{
		addNodeIfNotExists(fromNode);
		addNodeIfNotExists(toNode);
		addEdge(fromNode, toNode);
	}

	/**
	 * 
	 * @param fromNode
	 * @return	nodes pointed to from the specified node.
	 */
	public Deque<T> getToNodes(T fromNode)
	{
		return orderNodes(graph.get(fromNode));
	}

	/**
	 * 
	 * @param toNode
	 * @return	nodes pointing to the specified node.
	 */
	public Deque<T> getFromNodes(T toNode)
	{
		return orderNodes(reverseGraph.get(toNode));
	}

	/**
	 * 
	 * @param nodes
	 * @return	of the specified nodes returns those that are nodes in this graph, in the order they were inserted to
	 * 			the graph.
	 */
	public Deque<T> orderNodes(Collection<T> nodes)
	{
		Deque<T> orderedNodes = new ArrayDeque<>();
		for (T node : nodesInOrderAdded)
			if (nodes.contains(node))
				orderedNodes.add(node);
		return orderedNodes;
	}

	/**
	 * Removes a node and all its edges.
	 * 
	 * @param node
	 * @return	nodes that where pointed to by the removed node.
	 */
	public Deque<T> remove(T node)
	{
		Deque<T> result = getToNodes(node);
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
		nodesInOrderAdded.remove(node);
System.out.println("*************************** Removed node: " + node);
		return result;
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
	 * The returned {@link Deque} will reflect any future changes to the underlying {@link Map#keySet()}.
	 * 
	 * @return	the nodes in the order added to the graph.
	 */
	public Deque<T> getNodes()
	{
		return nodesInOrderAdded;
	}

	/**
	 * The returned {@link Deque} is a shallow copy of the underlying {@link Map#keySet()}.
	 * 
	 * @return	the nodes in the order added to the graph.
	 */
	public Deque<T> getNodesCopy()
	{
		Deque<T> copy = new ArrayDeque<>();
		for (T node : nodesInOrderAdded)
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
		Iterator<T> it = nodesInOrderAdded.iterator();
		if (!it.hasNext())
			return "{}";
		StringBuilder sb = new StringBuilder(1024);
		sb.append("{");
		for (;;)
		{
			T node = it.next();
			sb.append(node + "=" + getToNodes(node));
			if (!it.hasNext())
				return sb.append("}").toString();
			sb.append(", ");
		}
	}
}