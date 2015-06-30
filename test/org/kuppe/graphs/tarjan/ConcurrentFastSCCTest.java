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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class ConcurrentFastSCCTest extends AbstractConcurrentFastSCCTest {
	
	private final ConcurrentFastSCC concurrentFastScc = new ConcurrentFastSCC();
	
	@Test
	public void testEmpty() {
		final Graph graph = new Graph();
		
		// No vertices at all
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(0, sccs.size());
	}

	@Test
	public void testSingleVertex() {
		final Graph graph = new Graph();

		final GraphNode single = new GraphNode(1, graph);
		graph.addNode(single, 1);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
	
		// All nodes are post-visited
		Assert.assertTrue(single.is(Visited.POST));
	
		// All arcs have been explored
		Assert.assertFalse(graph.hasUntraversedArc(single));
		
		Assert.assertEquals(0, sccs.size());
	}
	
	@Test
	public void testA() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 1, 2);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 1, 3);
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 4);
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four, 3);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All arcs have been explored
		Assert.assertFalse(graph.hasUntraversedArc(one));
		Assert.assertFalse(graph.hasUntraversedArc(two));
		Assert.assertFalse(graph.hasUntraversedArc(three));
		Assert.assertFalse(graph.hasUntraversedArc(four));
		
		Assert.assertEquals(printSCCs(sccs), 2, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(printSCC(scc), 2, scc.size());
		}
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		
		Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(one);
		anSCC.add(two);
		
		expected.add(anSCC);

		anSCC = new HashSet<GraphNode>();
		anSCC.add(three);
		anSCC.add(four);
		
		expected.add(anSCC);
		
		Assert.assertEquals(expected, sccs);
	}
	
	@Test
	public void testB() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 1,2,3);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 1,2,3);
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 1,2,3);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(graph.hasUntraversedArc(one));
		Assert.assertFalse(graph.hasUntraversedArc(two));
		Assert.assertFalse(graph.hasUntraversedArc(three));
	
		Assert.assertEquals(printSCCs(sccs), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(printSCC(scc), 3, scc.size());
		}
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(three);
		anSCC.add(two);
		anSCC.add(one);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
	}
	
	@Test
	public void testC() {
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
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		Assert.assertTrue(four.is(Visited.POST));
		Assert.assertTrue(five.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(graph.hasUntraversedArc(one));
		Assert.assertFalse(graph.hasUntraversedArc(two));
		Assert.assertFalse(graph.hasUntraversedArc(three));
		Assert.assertFalse(graph.hasUntraversedArc(four));
		Assert.assertFalse(graph.hasUntraversedArc(five));

		Assert.assertEquals(printSCCs(sccs), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(printSCC(scc), 5, scc.size());
		}
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(two);
		anSCC.add(one);
		anSCC.add(three);
		anSCC.add(four);
		anSCC.add(five);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
	}

	@Test
	public void testD() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 2);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 1);
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 4);
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four, 3);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		Assert.assertTrue(four.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(graph.hasUntraversedArc(one));
		Assert.assertFalse(graph.hasUntraversedArc(two));
		Assert.assertFalse(graph.hasUntraversedArc(three));
		Assert.assertFalse(graph.hasUntraversedArc(four));
		
		Assert.assertEquals(printSCCs(sccs), 2, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(printSCC(scc), 2, scc.size());
		}

		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(one);
		anSCC.add(two);
		expected.add(anSCC);
		anSCC = new HashSet<GraphNode>();
		anSCC.add(three);
		anSCC.add(four);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
	}

	@Test
	public void testE() {
		final Graph graph = new Graph();

		// a ring
		final GraphNode one = new GraphNode(1, graph);
		graph.addNode(one, 2);
		final GraphNode two = new GraphNode(2, graph);
		graph.addNode(two, 3);
		final GraphNode three = new GraphNode(3, graph);
		graph.addNode(three, 4);
		final GraphNode four = new GraphNode(4, graph);
		graph.addNode(four, 5);
		final GraphNode five = new GraphNode(5, graph);
		graph.addNode(five, 6);
		final GraphNode six = new GraphNode(6, graph);
		graph.addNode(six, 1);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
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
		
		Assert.assertEquals(printSCCs(sccs), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(printSCC(scc), 6, scc.size());
		}
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(one);
		anSCC.add(two);
		anSCC.add(three);
		anSCC.add(four);
		anSCC.add(five);
		anSCC.add(six);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
	}
	
	@Test
	public void testEBiDirectional() {
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

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
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
		
		Assert.assertEquals(printSCCs(sccs), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(printSCC(scc), 6, scc.size());
		}
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(six);
		anSCC.add(three);
		anSCC.add(five);
		anSCC.add(four);
		anSCC.add(one);
		anSCC.add(two);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
	}

	@Test
	public void testF() {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode center = new GraphNode(1, graph);
		graph.addNode(center, 2,4,5);
		
		final GraphNode leftUpper = new GraphNode(2, graph);
		graph.addNode(leftUpper, 2);
		
		final GraphNode rightUpper = new GraphNode(3, graph);
		graph.addNode(rightUpper, 1);
		
		final GraphNode leftBottom = new GraphNode(4, graph);
		graph.addNode(leftBottom,4);
		
		final GraphNode rightBottom = new GraphNode(5, graph);
		graph.addNode(rightBottom, 3);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());

		// All nodes are post-visited
		Assert.assertTrue(center.is(Visited.POST));
		Assert.assertTrue(leftUpper.is(Visited.POST));
		Assert.assertTrue(leftBottom.is(Visited.POST));
		Assert.assertTrue(rightUpper.is(Visited.POST));
		Assert.assertTrue(rightBottom.is(Visited.POST));

		// All arcs have been explored
		Assert.assertFalse(graph.hasUntraversedArc(center));
		Assert.assertFalse(graph.hasUntraversedArc(leftUpper));
		Assert.assertFalse(graph.hasUntraversedArc(leftBottom));
		Assert.assertFalse(graph.hasUntraversedArc(rightUpper));
		Assert.assertFalse(graph.hasUntraversedArc(rightBottom));

		Assert.assertEquals(printSCCs(sccs), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(printSCC(scc), 3, scc.size());
		}
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		final Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(center);
		anSCC.add(rightBottom);
		anSCC.add(rightUpper);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
	}

	@Test
	public void testG() {
		final Graph graph = new Graph();

		// two nodes with bi-directional connection
		final GraphNode left = new GraphNode(1, graph);
		graph.addNode(left, 2);
		
		final GraphNode right = new GraphNode(2, graph);
		graph.addNode(right, 1);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(left.is(Visited.POST));
		Assert.assertTrue(right.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(graph.hasUntraversedArc(left));
		Assert.assertFalse(graph.hasUntraversedArc(right));
		
		Assert.assertEquals(printSCCs(sccs), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(printSCC(scc), 2, scc.size());
		}
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		final Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(left);
		anSCC.add(right);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
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
			two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);
		} catch (AssertionError e) {
			Assert.fail("A node has to be PRE-visited if its contracted into its root (or assertions \"-ea\" disabled)");
		}
	}
}
