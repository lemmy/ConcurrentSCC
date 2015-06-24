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

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ConcurrentFastSCCTest {
	
	private final ConcurrentFastSCC concurrentFastScc = new ConcurrentFastSCC();

//	@Test
//	public void testEmpty() {
//		final List<GraphNode> roots = new ArrayList<GraphNode>();
//		
//		// No vertices at all
//		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(roots);
//		Assert.assertEquals(0, sccs.size());
//	}
//
//	@Test
//	public void testSingleVertex() {
//		final List<GraphNode> roots = new ArrayList<GraphNode>();
//		
//		// single vertex with arc to self
//		final GraphNode one = new GraphNode(1);
//		roots.add(one);
//		one.addEdge(one);
//
//		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(roots);
//		
//		Assert.assertEquals(0, sccs.size());
//	}
	
	@Test
	public void testA() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1, 2);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1, 3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 4);
		final GraphNode four = new GraphNode(4);
		graph.addNode(four, 3);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		
		Assert.assertEquals(sccs.toString(), 2, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 2, scc.size());
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

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2,3);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 1,2,3);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 3, scc.size());
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
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);

		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 5, scc.size());
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

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 4);
		final GraphNode four = new GraphNode(4);
		graph.addNode(four, 3);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		
		Assert.assertEquals(sccs.toString(), 2, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 2, scc.size());
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
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 4);
		final GraphNode four = new GraphNode(4);
		graph.addNode(four, 5);
		final GraphNode five = new GraphNode(5);
		graph.addNode(five, 6);
		final GraphNode six = new GraphNode(6);
		graph.addNode(six, 1);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 6, scc.size());
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
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2,6);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two,1,3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three,2,4);
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,3,5);
		final GraphNode five = new GraphNode(5);
		graph.addNode(five,4,6);
		final GraphNode six = new GraphNode(6);
		graph.addNode(six,5,1);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 6, scc.size());
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
		
		concurrentFastScc.searchSCCs(graph);
		
		fail("Not yet implemented");
	}
}
