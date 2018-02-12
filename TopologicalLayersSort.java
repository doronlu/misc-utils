package net.katros.services.utils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Sorts a DAG into layers so that each node in layer 1 has no incoming edges and each node in layer i:i>1 has at least
 * one incoming edge from a node in layer i-1 and no incoming edges from a node in a layer j:j>=i.
 * In a job scheduling system where the nodes represent jobs and the edges represent dependencies between jobs, all jobs
 * in the same layer may execute concurrently (as long as they don't share resources that are not MT safe).
 * 
 * @author doron
 */
public abstract class TopologicalLayersSort
{
	/**
	 * 
	 * @param dag	assumed to be acyclic.
	 * @return	the nodes sorted and layered.
	 */
	public static <T extends Comparable<? super T>> Set<T>[] sort(SortedDirectedGraph<T> dag)
	{
		Set<T> nextLayerCandidates = dag.getNodes();
		Map<T, IntWrapper> depths = new HashMap<>();
		for (T node : nextLayerCandidates)
			depths.put(node, new IntWrapper());
		int depth = 0;
		for (; !nextLayerCandidates.isEmpty(); depth++)
			nextLayerCandidates = getNextLayerCandidates(dag, depths, nextLayerCandidates);
		@SuppressWarnings("unchecked")
		Set<T>[] layers = new Set[depth];
		for (int i = 0; i < depth; i++)
			layers[i] = new TreeSet<T>();
		for (Map.Entry<T, IntWrapper> entry : depths.entrySet())
			layers[entry.getValue().get()].add(entry.getKey());
		return layers;
	}

	/**
	 * 
	 * @param dag	assumed to be acyclic.
	 * @return	the nodes sorted and layered.
	 */
	public static <T> Deque<T>[] sort(DirectedGraph<T> dag)
	{
		Set<T> nextLayerCandidates = new HashSet<T>(dag.getNodes());
		Map<T, IntWrapper> depths = new HashMap<>();
		for (T node : nextLayerCandidates)
			depths.put(node, new IntWrapper());
		int depth = 0;
		for (; !nextLayerCandidates.isEmpty(); depth++)
			nextLayerCandidates = getNextLayerCandidates(dag, depths, nextLayerCandidates);
		@SuppressWarnings("unchecked")
		Deque<T>[] layers = new Deque[depth];
		for (int i = 0; i < depth; i++)
			layers[i] = new ArrayDeque<T>();
		for (Map.Entry<T, IntWrapper> entry : depths.entrySet())
			layers[entry.getValue().get()].add(entry.getKey());
		for (int i = 0; i < depth; i++)
			layers[i] = dag.orderNodes(layers[i]);
		return layers;
	}

	private static <T extends Comparable<? super T>> Set<T> getNextLayerCandidates(SortedDirectedGraph<T> dag,
			Map<T, IntWrapper> depths, Set<T> currentLayerCandidates)
	{
		Set<T> nextLayerCandidates = new HashSet<>();
		for (T node : currentLayerCandidates)
			nextLayerCandidates.addAll(dag.getToNodes(node));
		for (T node : nextLayerCandidates)
			depths.get(node).increment();
		return nextLayerCandidates;
	}

	private static <T> Set<T> getNextLayerCandidates(DirectedGraph<T> dag, Map<T, IntWrapper> depths,
			Set<T> currentLayerCandidates)
	{
		Set<T> nextLayerCandidates = new HashSet<>();
		for (T node : currentLayerCandidates)
			nextLayerCandidates.addAll(dag.getToNodes(node));
		for (T node : nextLayerCandidates)
			depths.get(node).increment();
		return nextLayerCandidates;
	}

	public static <T extends Comparable<? super T>> String toString(SortedDirectedGraph<T> dag)
	{
		return toString(TopologicalLayersSort.sort(dag));
	}

	public static <T> String toString(DirectedGraph<T> dag)
	{
		return toString(TopologicalLayersSort.sort(dag));
	}

	public static <T> String toString(Set<T>[] layers)
	{
		StringBuilder sb = new StringBuilder(1024);
		sb.append(layers.length).append(" layers:");
		for (Set<T> layer : layers)
			sb.append("\n\t").append(layer);
		return sb.toString();
	}

	public static <T> String toString(Deque<T>[] layers)
	{
		StringBuilder sb = new StringBuilder(1024);
		sb.append(layers.length).append(" layers:");
		for (Deque<T> layer : layers)
			sb.append("\n\t").append(layer);
		return sb.toString();
	}

	public static void main(String[] args)
	{
		// The graph is from: http://en.wikipedia.org/wiki/Topological_sorting
		Integer[] integers = { 5, 11, 9, 2, 3, 10, 8, 7 };
		String[] strings = { "5", "11", "9", "2", "3", "10", "8", "7" };

		SortedDirectedGraph<Integer> sortedDagInt = new SortedDirectedGraph<>(integers);
		sortedDagInt.addEdge(11, 9);
		sortedDagInt.addEdge(11, 10);
		sortedDagInt.addEdge(3, 8);
		sortedDagInt.addEdge(3, 10);
		sortedDagInt.addEdge(5, 11);
		sortedDagInt.addEdge(7, 8);
		sortedDagInt.addEdge(7, 11);
		sortedDagInt.addEdge(8, 9);
		sortedDagInt.addEdge(11, 2);
		System.out.println("ints\nsorted dag before: " + sortedDagInt);
		Set<Integer>[] layersInt = TopologicalLayersSort.sort(sortedDagInt);
		System.out.println("layers: " + Arrays.asList(layersInt));
		System.out.println(" sorted dag after: " + sortedDagInt);

		SortedDirectedGraph<String> sortedDagStr = new SortedDirectedGraph<>(strings);
		sortedDagStr.addEdge("3", "8");
		sortedDagStr.addEdge("11", "9");
		sortedDagStr.addEdge("8", "9");
		sortedDagStr.addEdge("11", "10");
		sortedDagStr.addEdge("11", "2");
		sortedDagStr.addEdge("3", "10");
		sortedDagStr.addEdge("5", "11");
		sortedDagStr.addEdge("7", "8");
		sortedDagStr.addEdge("7", "11");
		System.out.println("\nstrings\nsorted dag before: " + sortedDagStr);
		Set<String>[] layersStr = TopologicalLayersSort.sort(sortedDagStr);
		System.out.println("layers: " + Arrays.asList(layersStr));
		System.out.println(" sorted dag after: " + sortedDagStr);
		System.out.println("\n" + toString(sortedDagStr));

		DirectedGraph<Integer> dagInt = new DirectedGraph<>(integers);
		dagInt.addEdge(11, 9);
		dagInt.addEdge(11, 10);
		dagInt.addEdge(3, 8);
		dagInt.addEdge(3, 10);
		dagInt.addEdge(5, 11);
		dagInt.addEdge(7, 8);
		dagInt.addEdge(7, 11);
		dagInt.addEdge(8, 9);
		dagInt.addEdge(11, 2);
		System.out.println("\n\nints\ndag before: " + dagInt);
		Deque<Integer>[] layersInt2 = TopologicalLayersSort.sort(dagInt);
		System.out.println("layers: " + Arrays.asList(layersInt2));
		System.out.println(" dag after: " + dagInt);

		DirectedGraph<String> dagStr = new DirectedGraph<>(strings);
		dagStr.addEdge("3", "8");
		dagStr.addEdge("11", "9");
		dagStr.addEdge("8", "9");
		dagStr.addEdge("11", "10");
		dagStr.addEdge("11", "2");
		dagStr.addEdge("3", "10");
		dagStr.addEdge("5", "11");
		dagStr.addEdge("7", "8");
		dagStr.addEdge("7", "11");
		System.out.println("\nstrings\ndag before: " + dagStr);
		Deque<String>[] layersStr2 = TopologicalLayersSort.sort(dagStr);
		System.out.println("layers: " + Arrays.asList(layersStr2));
		System.out.println(" dag after: " + dagStr);
		System.out.println("\n" + toString(dagStr));

		// a graph containing a cycle
//		dag = new DirectedGraph<>();
//		dag.addNode("2");
//		dag.addNode("3");
//		dag.addEdge("2", "3");
//		dag.addEdge("3", "2");
//		layers = TopologicalLayersSort.sort(dag);
//		System.out.println(layers);
	}

	private static class IntWrapper
	{
		private int value;

		private void increment()
		{
			value++;
		}

		private int get()
		{
			return value;
		}

		@Override
		public String toString()
		{
			return String.valueOf(value);
		}
	}
}