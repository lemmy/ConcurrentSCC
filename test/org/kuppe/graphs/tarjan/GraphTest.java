package org.kuppe.graphs.tarjan;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;


public class GraphTest {

	@Test
	public void testPartitionEmtpyGraph() {
		// create a graph
		final Graph graph = new Graph();
		final List<AppendableIterator<GraphNode>> partition = graph.partition(5);
		Assert.assertEquals(5, partition.size());
		for (Iterator<GraphNode> iterator : partition) {
			Assert.assertFalse(iterator.hasNext());
		}
	}
	
	@Test
	public void testPartition() {
		final int nodes = 10001;
		
		// Create a graph
		final Graph graph = new Graph();
		for (int i = 0; i < nodes; i++) {
			graph.addNode(new GraphNode(i), i - 1);
		}
		
		// Partition the graph and check that all nodes are contained
		final List<AppendableIterator<GraphNode>> partition = graph.partition(5);
		Assert.assertEquals(5, partition.size());
		
		// Check that the union of all partitions is equal to the original graph
		final Set<GraphNode> allNodes = new HashSet<>(nodes);
		for (Iterator<GraphNode> iterator : partition) {
			while (iterator.hasNext()) {
				final GraphNode graphNode = iterator.next();
				// Assert the partitions are disjunct
				Assert.assertFalse(allNodes.contains(graphNode));
				allNodes.add(graphNode);
			}
		}
		Assert.assertEquals(nodes, allNodes.size());
	}
	
	@Test
	public void testPartitionAppend() {
		final int nodes = 10001;
		
		// Create a graph
		final Graph graph = new Graph();
		for (int i = 0; i < nodes; i++) {
			graph.addNode(new GraphNode(i), i - 1);
		}
		
		final List<AppendableIterator<GraphNode>> partition = graph.partition(1);
		Assert.assertEquals(1, partition.size());
	}
}
