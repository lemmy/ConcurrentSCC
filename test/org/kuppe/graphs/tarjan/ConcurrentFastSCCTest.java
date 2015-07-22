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

		final GraphNode single = new GraphNode(0);
		graph.addNode(single, 0);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
	
		// All nodes are post-visited
		Assert.assertTrue(single.is(Visited.POST));
	
		// All arcs have been explored
		Assert.assertFalse(single.hasArcs());
		
		Assert.assertEquals(0, sccs.size());
	}
	
	@Test
	public void testA() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(0);
		graph.addNode(one, 0, 1);
		final GraphNode two = new GraphNode(1);
		graph.addNode(two, 0, 2);
		final GraphNode three = new GraphNode(2);
		graph.addNode(three, 3);
		final GraphNode four = new GraphNode(3);
		graph.addNode(four, 2);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All arcs have been explored
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		
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
	public void testAWithOneAsInit() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1, 2);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1, 3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 4);
		final GraphNode four = new GraphNode(4);
		graph.addNode(four, 3);

		graph.setInit(one.getId());
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All arcs have been explored
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		
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

		final GraphNode one = new GraphNode(0);
		graph.addNode(one, 0,1,2);
		final GraphNode two = new GraphNode(1);
		graph.addNode(two, 0,1,2);
		final GraphNode three = new GraphNode(2);
		graph.addNode(three, 0,1,2);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
	
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
	public void testBWithOneAsInit() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2,3);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 1,2,3);

		graph.setInit(one.getId());
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
	
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

		final GraphNode one = new GraphNode(0);
		graph.addNode(one,2);
		final GraphNode two = new GraphNode(1);
		graph.addNode(two,2);
		final GraphNode three = new GraphNode(2);
		graph.addNode(three,3,4);
		final GraphNode four = new GraphNode(3);
		graph.addNode(four,0);
		final GraphNode five = new GraphNode(4);
		graph.addNode(five,1);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		Assert.assertTrue(four.is(Visited.POST));
		Assert.assertTrue(five.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		Assert.assertFalse(five.hasArcs());

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
	public void testCWithFourAsInit() {
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
		
		graph.setInit(four.getId());
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		Assert.assertTrue(four.is(Visited.POST));
		Assert.assertTrue(five.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		Assert.assertFalse(five.hasArcs());

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

		final GraphNode one = new GraphNode(0);
		graph.addNode(one, 1);
		final GraphNode two = new GraphNode(1);
		graph.addNode(two, 0);
		final GraphNode three = new GraphNode(2);
		graph.addNode(three, 3);
		final GraphNode four = new GraphNode(3);
		graph.addNode(four, 2);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		Assert.assertTrue(four.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		
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
	public void testDWithOneAndTreeAsInits() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(0);
		graph.addNode(one, 1);
		final GraphNode two = new GraphNode(1);
		graph.addNode(two, 0);
		final GraphNode three = new GraphNode(2);
		graph.addNode(three, 3);
		final GraphNode four = new GraphNode(3);
		graph.addNode(four, 2);
		
		graph.setInit(one.getId());
		graph.setInit(three.getId());
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		Assert.assertTrue(four.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		
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
		final GraphNode one = new GraphNode(0);
		graph.addNode(one, 1);
		final GraphNode two = new GraphNode(1);
		graph.addNode(two, 2);
		final GraphNode three = new GraphNode(2);
		graph.addNode(three, 3);
		final GraphNode four = new GraphNode(3);
		graph.addNode(four, 4);
		final GraphNode five = new GraphNode(4);
		graph.addNode(five, 5);
		final GraphNode six = new GraphNode(5);
		graph.addNode(six, 0);

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
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		Assert.assertFalse(five.hasArcs());
		Assert.assertFalse(six.hasArcs());
		
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
	public void testEWithOneAsInit() {
		final Graph graph = new Graph();

		// a ring
		final GraphNode one = new GraphNode(0);
		graph.addNode(one, 1);
		final GraphNode two = new GraphNode(1);
		graph.addNode(two, 2);
		final GraphNode three = new GraphNode(2);
		graph.addNode(three, 3);
		final GraphNode four = new GraphNode(3);
		graph.addNode(four, 4);
		final GraphNode five = new GraphNode(4);
		graph.addNode(five, 5);
		final GraphNode six = new GraphNode(5);
		graph.addNode(six, 0);

		graph.setInit(one.getId());
		
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
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		Assert.assertFalse(five.hasArcs());
		Assert.assertFalse(six.hasArcs());
		
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
		final GraphNode one = new GraphNode(0);
		graph.addNode(one, 1,5);
		final GraphNode two = new GraphNode(1);
		graph.addNode(two,0,2);
		final GraphNode three = new GraphNode(2);
		graph.addNode(three,1,3);
		final GraphNode four = new GraphNode(3);
		graph.addNode(four,2,4);
		final GraphNode five = new GraphNode(4);
		graph.addNode(five,3,5);
		final GraphNode six = new GraphNode(5);
		graph.addNode(six,4,0);

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
		Assert.assertFalse(one.hasArcs());
		Assert.assertFalse(two.hasArcs());
		Assert.assertFalse(three.hasArcs());
		Assert.assertFalse(four.hasArcs());
		Assert.assertFalse(five.hasArcs());
		Assert.assertFalse(six.hasArcs());
		
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
		final GraphNode center = new GraphNode(0);
		graph.addNode(center, 1,3,4);
		
		final GraphNode leftUpper = new GraphNode(1);
		graph.addNode(leftUpper, 1);
		
		final GraphNode rightUpper = new GraphNode(2);
		graph.addNode(rightUpper, 0);
		
		final GraphNode leftBottom = new GraphNode(3);
		graph.addNode(leftBottom,3);
		
		final GraphNode rightBottom = new GraphNode(4);
		graph.addNode(rightBottom, 2);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());

		// All nodes are post-visited
		Assert.assertTrue(center.is(Visited.POST));
		Assert.assertTrue(leftUpper.is(Visited.POST));
		Assert.assertTrue(leftBottom.is(Visited.POST));
		Assert.assertTrue(rightUpper.is(Visited.POST));
		Assert.assertTrue(rightBottom.is(Visited.POST));

		// All arcs have been explored
		Assert.assertFalse(center.hasArcs());
		Assert.assertFalse(leftUpper.hasArcs());
		Assert.assertFalse(leftBottom.hasArcs());
		Assert.assertFalse(rightUpper.hasArcs());
		Assert.assertFalse(rightBottom.hasArcs());

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
	public void testFWithOneAsInit() {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode center = new GraphNode(1);
		graph.addNode(center, 2,4,5);
		
		final GraphNode leftUpper = new GraphNode(2);
		graph.addNode(leftUpper, 2);
		
		final GraphNode rightUpper = new GraphNode(3);
		graph.addNode(rightUpper, 1);
		
		final GraphNode leftBottom = new GraphNode(4);
		graph.addNode(leftBottom,4);
		
		final GraphNode rightBottom = new GraphNode(5);
		graph.addNode(rightBottom, 3);
		
		graph.setInit(center.getId());
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());

		// All nodes are post-visited
		Assert.assertTrue(center.is(Visited.POST));
		Assert.assertTrue(leftUpper.is(Visited.POST));
		Assert.assertTrue(leftBottom.is(Visited.POST));
		Assert.assertTrue(rightUpper.is(Visited.POST));
		Assert.assertTrue(rightBottom.is(Visited.POST));

		// All arcs have been explored
		Assert.assertFalse(center.hasArcs());
		Assert.assertFalse(leftUpper.hasArcs());
		Assert.assertFalse(leftBottom.hasArcs());
		Assert.assertFalse(rightUpper.hasArcs());
		Assert.assertFalse(rightBottom.hasArcs());

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
		final GraphNode left = new GraphNode(0);
		graph.addNode(left, 1);
		
		final GraphNode right = new GraphNode(1);
		graph.addNode(right, 0);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertTrue(printSCCs(sccs), graph.checkPostCondition());
		
		// All nodes are post-visited
		Assert.assertTrue(left.is(Visited.POST));
		Assert.assertTrue(right.is(Visited.POST));
		
		// All arcs have been explored
		Assert.assertFalse(left.hasArcs());
		Assert.assertFalse(right.hasArcs());
		
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
}
