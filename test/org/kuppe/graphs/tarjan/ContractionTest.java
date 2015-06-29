/*******************************************************************************
 * Copyright (c) 2015 Microsoft Research. All rights reserved. 
 *
 * The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/

package org.kuppe.graphs.tarjan;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Assert;
import org.junit.Test;
import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class ContractionTest {
	
	private final ExecutorService noopExecutor = new NoopExecutorService();

	@Test
	public void testInvalidVisitedStateChildContraction() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2);
		
		one.setParent(two);
		try {
			two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);
		} catch (AssertionError e) {
			return;
		}
		Assert.fail("A node has to be PRE-visited if its contracted into its root (or assertions \"-ea\" disabled)");
	}
	
	@Test
	public void testInvalidContraction() {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2,4,5);
		
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 2);
		
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 1);
		
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,4);
		
		final GraphNode five = new GraphNode(5);
		graph.addNode(five, 3);
		
		// The SCC in F
		five.setParent(three);
		one.setParent(four);
		// one is root at this
		three.setParent(one);

		// Cannot set two as parent of one, four is its parent already. 
		try {
			one.setParent(two);
		} catch (RuntimeException e) {
			Assert.assertEquals("non-root link", e.getMessage());
			return;
		}
		Assert.fail("Linked non-root node.");
	}
	
	@Test
	public void testContractionsInC() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one,3);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two,3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three,4,5);
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,1);
		final GraphNode five = new GraphNode(5);
		graph.addNode(five,2);
		
		// One possible permutation of child>parent relationships in the tree
		one.setParent(three);
		one.set(Visited.PRE);
		five.setParent(two);
		five.set(Visited.PRE);
		three.setParent(four);
		three.set(Visited.PRE);
		two.setParent(three);
		two.set(Visited.PRE);
		// 4 <- 3 <- 1
		// 4 <- 3 <- 2 <- 5 

		// Have all outgoing arcs
		Assert.assertTrue(graph.hasUntraversedArc(one));
		Assert.assertTrue(graph.hasUntraversedArc(two));
		Assert.assertTrue(graph.hasUntraversedArc(three));
		Assert.assertTrue(graph.hasUntraversedArc(four));
		Assert.assertTrue(graph.hasUntraversedArc(five));

		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);
		four.set(Visited.PRE);
		four.contract(sccs, graph, one);
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		
		// one and three have no outgoing arcs
		Assert.assertFalse(graph.hasUntraversedArc(one));
		Assert.assertFalse(graph.hasUntraversedArc(three));
		// but 2 and 5 still do
		Assert.assertTrue(graph.hasUntraversedArc(two));
		Assert.assertTrue(graph.hasUntraversedArc(five));

		four.contract(sccs, graph, five);
		Assert.assertTrue(five.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertFalse(graph.hasUntraversedArc(two));
		Assert.assertFalse(graph.hasUntraversedArc(five));
		
		// all arcs are now in four
		Assert.assertEquals(5, graph.getUntraversedArcs(four).size());

		final Set<GraphNode> expected = new HashSet<GraphNode>();
		expected.add(one);
		expected.add(two);
		expected.add(three);
		expected.add(four);
		expected.add(five);
		Set<GraphNode> actual = sccs.get(four);
		Assert.assertEquals(expected, actual);
	}
	
	
	@Test
	public void testNestedContraction() {
		final Graph graph = new Graph();

		// a unidirectional loop
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2);
		
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 3);
		
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 4);
		
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,5);
		
		final GraphNode five = new GraphNode(5);
		graph.addNode(five, 1);

		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);

		// Contract two into one
		two.setParent(one);
		two.set(Visited.PRE);
		one.set(Visited.PRE);
		one.contract(sccs, graph, two);
		Assert.assertTrue(graph.get(two.getId()) == one);
		
		// contract one into five
		one.setParent(five);
		one.set(Visited.PRE);
		five.set(Visited.PRE);
		five.contract(sccs, graph, one);
		Assert.assertTrue(graph.get(two.getId()) == five);
		Assert.assertTrue(graph.get(one.getId()) == five);

		// contract five into four
		five.setParent(four);
		five.set(Visited.PRE);
		four.set(Visited.PRE);
		four.contract(sccs, graph, five);
		Assert.assertTrue(graph.get(two.getId()) == four);
		Assert.assertTrue(graph.get(one.getId()) == four);
		Assert.assertTrue(graph.get(five.getId()) == four);
		
		// contract four into three
		four.setParent(three);
		four.set(Visited.PRE);
		three.set(Visited.PRE);
		three.contract(sccs, graph, four);
		Assert.assertTrue(graph.get(two.getId()) == three);
		Assert.assertTrue(graph.get(one.getId()) == three);
		Assert.assertTrue(graph.get(five.getId()) == three);
		Assert.assertTrue(graph.get(four.getId()) == three);
	}
	
	@Test
	public void testContractionsInF() throws Exception {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2,4,5);
		
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 2);
		
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 1);
		
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,4);
		
		final GraphNode five = new GraphNode(5);
		graph.addNode(five, 3);

		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);
		
		// The SCC in F
		five.setParent(three);
		five.set(Visited.PRE);
		one.setParent(four);
		one.set(Visited.PRE);
		// one is root at this
		three.setParent(one);
		three.set(Visited.PRE);
		// one is not at root at this point

		Assert.assertTrue(!one.isRoot());
		
		// one has outgoing arcs
		Assert.assertTrue(graph.hasUntraversedArc(one));
		
		Assert.assertTrue(one.is(Visited.PRE));
		
		// Roots: (2), (4)
		// Untraversed arcs: 1:5
		//
		// Once (4) gets explored, it is found that it has no children and its
		// child (1) must be cut loose.
		four.set(Visited.PRE);
		new SCCWorker(noopExecutor, graph, sccs, four).call();
		Assert.assertTrue(four.is(Visited.POST));
		
		// Since with the NoopExcecutor nested calls are executed recursively,
		// also one is done now.
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(one.isRoot());
		// one has no outgoing arcs
		Assert.assertFalse(graph.hasUntraversedArc(one));
		Assert.assertEquals(0, graph.getUntraversedArcs(one).size());
		
		final Set<GraphNode> expected = new HashSet<GraphNode>();
		expected.add(one);
		expected.add(three);
		expected.add(five);
		Assert.assertEquals(expected, sccs.get(one));
	}
	
	@Test
	public void testContractionsInFLoop() throws Exception {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2,4,5);
		
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 2);
		
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 1);
		
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,4);
		
		final GraphNode five = new GraphNode(5);
		graph.addNode(five, 3);

		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);

		final Collection<GraphNode> nodes = graph.getStartNodes();
		while (!graph.checkPostCondition()) {
			for (GraphNode graphNode : nodes) {
				new SCCWorker(noopExecutor, graph, sccs, graphNode).call();
			}
		}
		Assert.assertEquals(1, sccs.size());
		final Set<GraphNode> expected = new HashSet<GraphNode>();
		expected.add(one);
		expected.add(three);
		expected.add(five);
		Assert.assertEquals(expected, sccs.values().toArray()[0]);
	}
}
