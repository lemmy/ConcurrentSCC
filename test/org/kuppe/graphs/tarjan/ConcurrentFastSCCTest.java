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
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class ConcurrentFastSCCTest {
	
	private final ConcurrentFastSCC concurrentFastScc = new ConcurrentFastSCC();

	@Rule public TestName name = new TestName();

	@Before
	public void before() {
		System.out.println("=================================================================");
		System.out.println("==================== " + name.getMethodName() + " =========================");
		System.out.println("=================================================================");
	}

	@After
	public void after() {
		System.out.println("=================================================================");
		System.out.println("==================== " + name.getMethodName() + " =========================");
		System.out.println("=================================================================\n\n");
	}
	
	@Test
	public void testEmpty() {
		final Graph graph = new Graph();
		
		// No vertices at all
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		Assert.assertEquals(0, sccs.size());
	}

	@Test
	public void testSingleVertex() {
		final Graph graph = new Graph();

		final GraphNode single = new GraphNode(1);
		graph.addNode(single, 1);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		
		Assert.assertEquals(0, sccs.size());
	}
	
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

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2,3);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 1,2,3);

		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		
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

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 4);
		final GraphNode four = new GraphNode(4);
		graph.addNode(four, 3);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
		
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
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
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
		final GraphNode left = new GraphNode(1);
		graph.addNode(left, 2);
		
		final GraphNode right = new GraphNode(2);
		graph.addNode(right, 1);
		
		final Set<Set<GraphNode>> sccs = concurrentFastScc.searchSCCs(graph);
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
	
	private String printSCC(Set<GraphNode> scc) {
		StringBuffer buf = new StringBuffer(scc.size());
		buf.append("{");
		for (GraphNode graphNode : scc) {
			buf.append(graphNode.getId());
			buf.append(",");
		}
		removeIfDangling(buf, ",");
		buf.append("}");
		return buf.toString();
	}

	private void removeIfDangling(StringBuffer buf, String string) {
		if (buf.lastIndexOf(",") == buf.length() - 1) {
			buf.setLength(buf.length() - 1);
		}
	}

	private String printSCCs(Set<Set<GraphNode>> sccs) {
		StringBuffer buf = new StringBuffer(sccs.size());
		buf.append("Found SCCs: [");
		for (Set<GraphNode> set : sccs) {
			buf.append(printSCC(set));
			buf.append(",");
		}
		removeIfDangling(buf, ",");
		buf.append("]");
		return buf.toString();
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
		five.setParent(two);
		three.setParent(four);
		two.setParent(three);

		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);
		four.contract(sccs, graph, one);
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		
		four.contract(sccs, graph, five);
		Assert.assertTrue(five.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));

		final Set<GraphNode> expected = new HashSet<GraphNode>();
		expected.add(one);
		expected.add(two);
		expected.add(three);
		expected.add(four);
		expected.add(five);
		Set<GraphNode> actual = sccs.get(four);
		Assert.assertEquals(expected, actual);
	}
}
