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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;
import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class ContractionTest {
	
	private final ExecutorService noopExecutor = new NoopExecutorService();

	@Test
	public void testInvalidVisitedStateChildContraction() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 1,2);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 1,2);
		
		one.setParent(two);
		one.visited = Visited.POST;
		try {
			two.contract(new HashMap<GraphNode, GraphNode>(0), graph, one);
		} catch (AssertionError e) {
			return;
		}
		Assert.fail("A node has to be UN-visited if its contracted into its root (or assertions \"-ea\" disabled)");
	}
	
	@Test
	public void testInvalidContraction() {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 2,4,5);
		
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 2);
		
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 1);
		
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four,4);
		
		final GraphNode five = new GraphNode(5, graph);
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

		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one,3);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two,3);
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three,4,5);
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four,1);
		final GraphNode five = new GraphNode(5, graph);
		graph.addNode(five,2);
		
		// One possible permutation of child>parent relationships in the tree
		one.setParent(three);
		five.setParent(two);
		three.setParent(four);
		two.setParent(three);
		// 4 <- 3 <- 1
		// 4 <- 3 <- 2 <- 5 

		// Have all outgoing arcs
		Assert.assertTrue(graph.hasUntraversedArc(one));
		Assert.assertTrue(graph.hasUntraversedArc(two));
		Assert.assertTrue(graph.hasUntraversedArc(three));
		Assert.assertTrue(graph.hasUntraversedArc(four));
		Assert.assertTrue(graph.hasUntraversedArc(five));

		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);
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

		Assert.assertEquals(expected, sccs.get(four).getSCC());
	}
	
	@Test
	public void testNestedContraction() {
		final Graph graph = new Graph();

		// a unidirectional loop
		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 2);
		
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 3);
		
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 4);
		
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four,5);
		
		final GraphNode five = new GraphNode(5, graph);
		graph.addNode(five, 1);

		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);

		// Contract two into one
		two.setParent(one);
		one.contract(sccs, graph, two);
		Assert.assertTrue(graph.get(two.getId()) == one);
		
		// contract one into five
		one.setParent(five);
		five.contract(sccs, graph, one);
		Assert.assertTrue(graph.get(two.getId()) == five);
		Assert.assertTrue(graph.get(one.getId()) == five);

		// contract five into four
		five.setParent(four);
		four.contract(sccs, graph, five);
		Assert.assertTrue(graph.get(two.getId()) == four);
		Assert.assertTrue(graph.get(one.getId()) == four);
		Assert.assertTrue(graph.get(five.getId()) == four);
		
		// contract four into three
		four.setParent(three);
		three.contract(sccs, graph, four);
		Assert.assertTrue(graph.get(two.getId()) == three);
		Assert.assertTrue(graph.get(one.getId()) == three);
		Assert.assertTrue(graph.get(five.getId()) == three);
		Assert.assertTrue(graph.get(four.getId()) == three);
	}
	
	@Test
	public void testNestedContractionWithSplitNode() {
		final Graph graph = new Graph();

		final GraphNode zero = new GraphNode(0, graph);
		graph.addNode(zero);
		
		// 1 splits into 3 nodes
		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 1);
		
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 1);
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 1);
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four, 1);
		
		// 2 has two childs
		final GraphNode five = new GraphNode(5, graph);
		graph.addNode(five, 2);
		final GraphNode six = new GraphNode(6, graph);
		graph.addNode(six, 2);

		// 3 has one child (with childs)
		final GraphNode seven = new GraphNode(7, graph);
		graph.addNode(seven, 3);

		final GraphNode nine = new GraphNode(9, graph);
		graph.addNode(nine, 7);
		final GraphNode ten = new GraphNode(10, graph);
		graph.addNode(ten, 9);
		
		// four has one child
		final GraphNode eight = new GraphNode(8, graph);
		graph.addNode(eight, 4);
		
		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);

		// Contract two into one
		ten.setParent(nine);
		nine.contract(sccs, graph, ten);
		Assert.assertTrue(graph.get(zero.getId()) == zero);
		Assert.assertTrue(graph.get(one.getId()) == one);
		Assert.assertTrue(graph.get(two.getId()) == two);
		Assert.assertTrue(graph.get(three.getId()) == three);
		Assert.assertTrue(graph.get(four.getId()) == four);
		Assert.assertTrue(graph.get(five.getId()) == five);
		Assert.assertTrue(graph.get(six.getId()) == six);
		Assert.assertTrue(graph.get(seven.getId()) == seven);
		Assert.assertTrue(graph.get(eight.getId()) == eight);
		Assert.assertTrue(graph.get(nine.getId()) == nine);
		Assert.assertTrue(graph.get(ten.getId()) == nine);
		
		// contract one into zero
		one.setParent(zero);
		zero.contract(sccs, graph, one);
		Assert.assertTrue(graph.get(zero.getId()) == zero);
		Assert.assertTrue(graph.get(one.getId()) == zero);
		Assert.assertTrue(graph.get(two.getId()) == two);
		Assert.assertTrue(graph.get(three.getId()) == three);
		Assert.assertTrue(graph.get(four.getId()) == four);
		Assert.assertTrue(graph.get(five.getId()) == five);
		Assert.assertTrue(graph.get(six.getId()) == six);
		Assert.assertTrue(graph.get(seven.getId()) == seven);
		Assert.assertTrue(graph.get(eight.getId()) == eight);
		Assert.assertTrue(graph.get(nine.getId()) == nine);
		Assert.assertTrue(graph.get(ten.getId()) == nine);

		// contract eight into four
		eight.setParent(four);
		four.contract(sccs, graph, eight);
		Assert.assertTrue(graph.get(zero.getId()) == zero);
		Assert.assertTrue(graph.get(one.getId()) == zero);
		Assert.assertTrue(graph.get(two.getId()) == two);
		Assert.assertTrue(graph.get(three.getId()) == three);
		Assert.assertTrue(graph.get(four.getId()) == four);
		Assert.assertTrue(graph.get(five.getId()) == five);
		Assert.assertTrue(graph.get(six.getId()) == six);
		Assert.assertTrue(graph.get(seven.getId()) == seven);
		Assert.assertTrue(graph.get(eight.getId()) == four);
		Assert.assertTrue(graph.get(nine.getId()) == nine);
		Assert.assertTrue(graph.get(ten.getId()) == nine);
		
		// contract seven into three
		seven.setParent(three);
		three.contract(sccs, graph, seven);
		Assert.assertTrue(graph.get(zero.getId()) == zero);
		Assert.assertTrue(graph.get(one.getId()) == zero);
		Assert.assertTrue(graph.get(two.getId()) == two);
		Assert.assertTrue(graph.get(three.getId()) == three);
		Assert.assertTrue(graph.get(four.getId()) == four);
		Assert.assertTrue(graph.get(five.getId()) == five);
		Assert.assertTrue(graph.get(six.getId()) == six);
		Assert.assertTrue(graph.get(seven.getId()) == three);
		Assert.assertTrue(graph.get(eight.getId()) == four);
		Assert.assertTrue(graph.get(nine.getId()) == nine);
		Assert.assertTrue(graph.get(ten.getId()) == nine);
		
		// contract nine into three
		nine.setParent(three);
		three.contract(sccs, graph, nine);
		Assert.assertTrue(graph.get(zero.getId()) == zero);
		Assert.assertTrue(graph.get(one.getId()) == zero);
		Assert.assertTrue(graph.get(two.getId()) == two);
		Assert.assertTrue(graph.get(three.getId()) == three);
		Assert.assertTrue(graph.get(four.getId()) == four);
		Assert.assertTrue(graph.get(five.getId()) == five);
		Assert.assertTrue(graph.get(six.getId()) == six);
		Assert.assertTrue(graph.get(seven.getId()) == three);
		Assert.assertTrue(graph.get(eight.getId()) == four);
		Assert.assertTrue(graph.get(nine.getId()) == three);
		Assert.assertTrue(graph.get(ten.getId()) == three);
		
		// 3 into 0
		three.setParent(zero);
		zero.contract(sccs, graph, three);
		Assert.assertTrue(graph.get(zero.getId()) == zero);
		Assert.assertTrue(graph.get(one.getId()) == zero);
		Assert.assertTrue(graph.get(two.getId()) == two);
		Assert.assertTrue(graph.get(three.getId()) == zero);
		Assert.assertTrue(graph.get(four.getId()) == four);
		Assert.assertTrue(graph.get(five.getId()) == five);
		Assert.assertTrue(graph.get(six.getId()) == six);
		Assert.assertTrue(graph.get(seven.getId()) == zero);
		Assert.assertTrue(graph.get(eight.getId()) == four);
		Assert.assertTrue(graph.get(nine.getId()) == zero);
		Assert.assertTrue(graph.get(ten.getId()) == zero);
		
		// 4 into 0
		four.setParent(zero);
		zero.contract(sccs, graph, four);
		Assert.assertTrue(graph.get(zero.getId()) == zero);
		Assert.assertTrue(graph.get(one.getId()) == zero);
		Assert.assertTrue(graph.get(two.getId()) == two);
		Assert.assertTrue(graph.get(three.getId()) == zero);
		Assert.assertTrue(graph.get(four.getId()) == zero);
		Assert.assertTrue(graph.get(five.getId()) == five);
		Assert.assertTrue(graph.get(six.getId()) == six);
		Assert.assertTrue(graph.get(seven.getId()) == zero);
		Assert.assertTrue(graph.get(eight.getId()) == zero);
		Assert.assertTrue(graph.get(nine.getId()) == zero);
		Assert.assertTrue(graph.get(ten.getId()) == zero);
		
		Assert.assertEquals(1, sccs.size());
		GraphNode values = (GraphNode) sccs.values().toArray()[0];
		Assert.assertEquals(8, values.getSCC().size());
	}
	
	@Test
	public void testContractionsInF() throws Exception {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 2,4,5);
		
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 2);
		
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 1);
		
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four,4);
		
		final GraphNode five = new GraphNode(5, graph);
		graph.addNode(five, 3);

		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);
		
		// The SCC in F
		five.setParent(three);
		one.setParent(four);
		// one is root at this
		three.setParent(one);
		// one is not at root at this point

		Assert.assertTrue(!one.isRoot());
		
		// one has outgoing arcs
		Assert.assertTrue(graph.hasUntraversedArc(one));
		
		Assert.assertTrue(one.isNot(Visited.POST));
		
		// Roots: (2), (4)
		// Untraversed arcs: 1:5
		//
		// Once (4) gets explored, it is found that it has no children and its
		// child (1) must be cut loose.
		new SCCWorker(noopExecutor, new AtomicLong(Long.MAX_VALUE), new CountDownLatch(1), graph, sccs, four).run();
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
		Assert.assertEquals(expected, sccs.get(one).getSCC());
	}
	
	@Test
	public void testContractionsInFLoop() throws Exception {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 2,4,5);
		
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 2);
		
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 1);
		
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four,4);
		
		final GraphNode five = new GraphNode(5, graph);
		graph.addNode(five, 3);

		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);

		CountDownLatch latch = new CountDownLatch(1);
		final Collection<GraphNode> nodes = graph.getStartNodes();
		while (!graph.checkPostCondition()) {
			for (GraphNode graphNode : nodes) {
				new SCCWorker(noopExecutor, new AtomicLong(Long.MAX_VALUE), latch, graph, sccs, graphNode).run();
			}
		}
		Assert.assertEquals(1, sccs.size());
		final Set<GraphNode> expected = new HashSet<GraphNode>();
		expected.add(one);
		expected.add(three);
		expected.add(five);
		GraphNode actual = (GraphNode) sccs.values().toArray()[0];
		Assert.assertEquals(expected, actual.getSCC());
	}
	
	@Test
	public void testEBiDirectionalLoop() {
		final Graph graph = new Graph();

		// a ring with bi-directional edges
		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 2,6);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two,1,3);
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three,2,4);
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four,3,5);
		final GraphNode five = new GraphNode(5, graph);
		graph.addNode(five,4,6);
		final GraphNode six = new GraphNode(6, graph);
		graph.addNode(six,5,1);

		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);

		final Collection<GraphNode> nodes = graph.getStartNodes();
		CountDownLatch latch = new CountDownLatch(1);
		while (!graph.checkPostCondition()) {
			for (GraphNode graphNode : nodes) {
				new SCCWorker(noopExecutor, new AtomicLong(Long.MAX_VALUE), latch, graph, sccs, graphNode).run();
			}
		}
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		Assert.assertTrue(four.is(Visited.POST));
		Assert.assertTrue(five.is(Visited.POST));
		Assert.assertTrue(six.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(graph.hasUntraversedArc(one));
		Assert.assertFalse(graph.hasUntraversedArc(two));
		Assert.assertFalse(graph.hasUntraversedArc(three));
		Assert.assertFalse(graph.hasUntraversedArc(four));
		Assert.assertFalse(graph.hasUntraversedArc(five));
		Assert.assertFalse(graph.hasUntraversedArc(six));
		
		Assert.assertEquals(1, sccs.size());
		GraphNode actual = (GraphNode) sccs.values().toArray()[0];
		Set<GraphNode> expected = new HashSet<GraphNode>();
		expected.add(six);
		expected.add(three);
		expected.add(five);
		expected.add(four);
		expected.add(one);
		expected.add(two);
		Assert.assertEquals(expected, actual.getSCC());
	}

	
	@Test
	public void testContractionChildren() {
		final Graph graph = new Graph();
		
		final GraphNode zero = new GraphNode(0, graph);
		graph.addNode(zero);
		Assert.assertFalse(zero.hasChildren());
		
		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one);
		one.setParent(zero);
		final GraphNode oneChild = new GraphNode(4711, graph);
		graph.addNode(oneChild);
		oneChild.setParent(one);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two);
		two.setParent(zero);
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three);
		three.setParent(zero);
		Assert.assertTrue(zero.hasChildren());
		Assert.assertTrue(one.hasChildren());
		Assert.assertFalse(two.hasChildren());
		Assert.assertFalse(three.hasChildren());
		Assert.assertEquals(3, zero.getChildren().size());
		Assert.assertTrue(zero.getChildren().contains(one));
		Assert.assertTrue(zero.getChildren().contains(two));
		Assert.assertTrue(zero.getChildren().contains(three));
		Assert.assertEquals(zero, one.getParent());
		Assert.assertEquals(zero, two.getParent());
		
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four);
		four.setParent(three);
		final GraphNode five = new GraphNode(5, graph);
		graph.addNode(five);
		five.setParent(three);
		final GraphNode six = new GraphNode(6, graph);
		graph.addNode(six);
		six.setParent(three);
		Assert.assertTrue(zero.hasChildren());
		Assert.assertTrue(one.hasChildren());
		Assert.assertFalse(two.hasChildren());
		Assert.assertTrue(three.hasChildren());
		Assert.assertFalse(four.hasChildren());
		Assert.assertFalse(five.hasChildren());
		Assert.assertFalse(six.hasChildren());
		Assert.assertEquals(3, zero.getChildren().size());
		Assert.assertTrue(zero.getChildren().contains(one));
		Assert.assertTrue(zero.getChildren().contains(two));
		Assert.assertTrue(zero.getChildren().contains(three));
		Assert.assertEquals(3, three.getChildren().size());
		Assert.assertTrue(three.getChildren().contains(four));
		Assert.assertTrue(three.getChildren().contains(five));
		Assert.assertTrue(three.getChildren().contains(six));
		Assert.assertEquals(zero, one.getParent());
		Assert.assertEquals(zero, two.getParent());
		Assert.assertEquals(three, five.getParent());
		Assert.assertEquals(three, six.getParent());
		
		final GraphNode seven = new GraphNode(7, graph);
		graph.addNode(seven);
		seven.setParent(four);
		final GraphNode sevenChildA = new GraphNode(815, graph);
		graph.addNode(sevenChildA);
		sevenChildA.setParent(seven);
		final GraphNode sevenChildB = new GraphNode(42, graph);
		graph.addNode(sevenChildB);
		sevenChildB.setParent(seven);
		final GraphNode eight = new GraphNode(8, graph);
		graph.addNode(eight);
		eight.setParent(four);
		final GraphNode nine = new GraphNode(9, graph);
		graph.addNode(nine);
		nine.setParent(four);
		Assert.assertTrue(zero.hasChildren());
		Assert.assertTrue(one.hasChildren());
		Assert.assertFalse(two.hasChildren());
		Assert.assertTrue(three.hasChildren());
		Assert.assertTrue(four.hasChildren());
		Assert.assertFalse(five.hasChildren());
		Assert.assertFalse(six.hasChildren());
		Assert.assertTrue(seven.hasChildren());
		Assert.assertFalse(eight.hasChildren());
		Assert.assertFalse(nine.hasChildren());
		Assert.assertEquals(3, zero.getChildren().size());
		Assert.assertTrue(zero.getChildren().contains(one));
		Assert.assertTrue(zero.getChildren().contains(two));
		Assert.assertTrue(zero.getChildren().contains(three));
		Assert.assertEquals(3, three.getChildren().size());
		Assert.assertTrue(three.getChildren().contains(four));
		Assert.assertTrue(three.getChildren().contains(five));
		Assert.assertTrue(three.getChildren().contains(six));
		Assert.assertEquals(3, four.getChildren().size());
		Assert.assertTrue(four.getChildren().contains(seven));
		Assert.assertTrue(four.getChildren().contains(eight));
		Assert.assertTrue(four.getChildren().contains(nine));
		Assert.assertEquals(zero, one.getParent());
		Assert.assertEquals(zero, two.getParent());
		Assert.assertEquals(three, five.getParent());
		Assert.assertEquals(three, six.getParent());
		Assert.assertEquals(three, five.getParent());
		Assert.assertEquals(three, six.getParent());
		
		final GraphNode ten = new GraphNode(10, graph);
		graph.addNode(ten);
		ten.setParent(eight);
		final GraphNode tenChildA = new GraphNode(23, graph);
		graph.addNode(tenChildA);
		tenChildA.setParent(ten);
		final GraphNode tenChildB = new GraphNode(666, graph);
		graph.addNode(tenChildB);
		tenChildB.setParent(ten);
		final GraphNode eleven = new GraphNode(11, graph);
		graph.addNode(eleven);
		eleven.setParent(eight);
		Assert.assertTrue(zero.hasChildren());
		Assert.assertTrue(one.hasChildren());
		Assert.assertFalse(two.hasChildren());
		Assert.assertTrue(three.hasChildren());
		Assert.assertTrue(four.hasChildren());
		Assert.assertFalse(five.hasChildren());
		Assert.assertFalse(six.hasChildren());
		Assert.assertTrue(seven.hasChildren());
		Assert.assertTrue(eight.hasChildren());
		Assert.assertFalse(nine.hasChildren());
		Assert.assertTrue(ten.hasChildren());
		Assert.assertEquals(2, ten.getChildren().size());
		Assert.assertFalse(eleven.hasChildren());
		Assert.assertEquals(3, zero.getChildren().size());
		Assert.assertTrue(zero.getChildren().contains(one));
		Assert.assertTrue(zero.getChildren().contains(two));
		Assert.assertTrue(zero.getChildren().contains(three));
		Assert.assertEquals(3, three.getChildren().size());
		Assert.assertTrue(three.getChildren().contains(four));
		Assert.assertTrue(three.getChildren().contains(five));
		Assert.assertTrue(three.getChildren().contains(six));
		Assert.assertEquals(3, four.getChildren().size());
		Assert.assertTrue(four.getChildren().contains(seven));
		Assert.assertTrue(four.getChildren().contains(eight));
		Assert.assertTrue(four.getChildren().contains(nine));
		Assert.assertEquals(2, eight.getChildren().size());
		Assert.assertTrue(eight.getChildren().contains(ten));
		Assert.assertTrue(eight.getChildren().contains(eleven));
	
		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);
		zero.contract(sccs, graph, eight);
		
		Assert.assertEquals(8, zero.getChildren().size());
		for (TreeNode child : zero.getChildren()) {
			Assert.assertEquals(zero, child.getParent());
		}
		
		Assert.assertEquals(1, one.getChildren().size());
		Assert.assertTrue(one.getChildren().contains(oneChild));
		one.getChildren().forEach((child) -> Assert.assertEquals(one, child.getParent()));
		
		Assert.assertEquals(2, seven.getChildren().size());
		Assert.assertTrue(seven.getChildren().contains(sevenChildA));
		Assert.assertTrue(seven.getChildren().contains(sevenChildB));
		seven.getChildren().forEach((child) -> Assert.assertEquals(seven, child.getParent()));

		Assert.assertEquals(2, ten.getChildren().size());
		Assert.assertTrue(ten.getChildren().contains(tenChildA));
		Assert.assertTrue(ten.getChildren().contains(tenChildB));
		ten.getChildren().forEach((child) -> Assert.assertEquals(ten, child.getParent()));
	}
	
	@Test
	public void testVisitedStateChildContraction() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 1,2);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 1,2);
		
		one.setParent(two);
		try {
			two.contract(new HashMap<GraphNode, GraphNode>(0), graph, one);
		} catch (AssertionError e) {
			Assert.fail("A node has to be PRE-visited if its contracted into its root (or assertions \"-ea\" disabled)");
		}
	}
}
