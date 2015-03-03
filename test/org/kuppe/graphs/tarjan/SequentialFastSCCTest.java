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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

public class SequentialFastSCCTest {
	
	@Test
	public void testEmpty() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();
		
		// No vertices at all
		final Set<Stack<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		Assert.assertEquals(0, sccs.size());
	}

	@Test
	public void testSingleVertex() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();
		
		// single vertex with arc to self
		final GraphNode one = new GraphNode("1");
		roots.add(one);
		one.addEdge(one);

		final Set<Stack<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		Assert.assertEquals(0, sccs.size());
	}
	
	@Test
	public void testA() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();

		final GraphNode one = new GraphNode("1");
		roots.add(one);
		final GraphNode two = new GraphNode("2");
		roots.add(two);
		final GraphNode three = new GraphNode("3");
		roots.add(three);
		final GraphNode four = new GraphNode("4");
		roots.add(four);

		one.addEdge(two);
		one.addEdge(one);

		two.addEdge(one);
		two.addEdge(three);

		three.addEdge(four);

		four.addEdge(three);

		final Set<Stack<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		final Set<Stack<GraphNode>> expected = new HashSet<Stack<GraphNode>>();
		Stack<GraphNode> anSCC = new Stack<GraphNode>();
		anSCC.add(two);
		anSCC.add(one);
		expected.add(anSCC);
		anSCC = new Stack<GraphNode>();
		anSCC.add(four);
		anSCC.add(three);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
		
		Assert.assertEquals(sccs.toString(), 2, sccs.size());
		for (Stack<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 2, scc.size());
		}
	}
	
	@Test
	public void testB() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();

		final GraphNode one = new GraphNode("1");
		roots.add(one);
		final GraphNode two = new GraphNode("2");
		roots.add(two);
		final GraphNode three = new GraphNode("3");
		roots.add(three);

		one.addEdge(one);
		one.addEdge(two);
		one.addEdge(three);

		two.addEdge(one);
		two.addEdge(two);
		two.addEdge(three);

		three.addEdge(one);
		three.addEdge(two);
		three.addEdge(three);

		final Set<Stack<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		final Set<Stack<GraphNode>> expected = new HashSet<Stack<GraphNode>>();
		Stack<GraphNode> anSCC = new Stack<GraphNode>();
		anSCC.add(three);
		anSCC.add(two);
		anSCC.add(one);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Stack<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 3, scc.size());
		}
	}
	
	@Test
	public void testC() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();

		final GraphNode one = new GraphNode("1");
		roots.add(one);
		final GraphNode two = new GraphNode("2");
		roots.add(two);
		final GraphNode three = new GraphNode("3");
		roots.add(three);
		final GraphNode four = new GraphNode("4");
		roots.add(four);
		final GraphNode five = new GraphNode("5");
		roots.add(five);

		one.addEdge(three);

		two.addEdge(three);

		three.addEdge(four);
		three.addEdge(five);

		four.addEdge(one);
		
		five.addEdge(two);
		
		final Set<Stack<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		final Set<Stack<GraphNode>> expected = new HashSet<Stack<GraphNode>>();
		Stack<GraphNode> anSCC = new Stack<GraphNode>();
		anSCC.add(two);
		anSCC.add(one);
		anSCC.add(three);
		anSCC.add(four);
		anSCC.add(five);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);

		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Stack<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 5, scc.size());
		}
	}

	@Test
	public void testD() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();

		final GraphNode one = new GraphNode("1");
		roots.add(one);
		final GraphNode two = new GraphNode("2");
		roots.add(two);
		final GraphNode three = new GraphNode("3");
		roots.add(three);
		final GraphNode four = new GraphNode("4");
		roots.add(four);

		one.addEdge(two);

		two.addEdge(one);

		three.addEdge(four);

		four.addEdge(three);
		
		final Set<Stack<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		final Set<Stack<GraphNode>> expected = new HashSet<Stack<GraphNode>>();
		Stack<GraphNode> anSCC = new Stack<GraphNode>();
		anSCC.add(one);
		anSCC.add(two);
		expected.add(anSCC);
		anSCC = new Stack<GraphNode>();
		anSCC.add(three);
		anSCC.add(four);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
		
		Assert.assertEquals(sccs.toString(), 2, sccs.size());
		for (Stack<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 2, scc.size());
		}
	}
	
	@Test
	public void testE() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();

		// a ring
		final GraphNode one = new GraphNode("1");
		roots.add(one);
		final GraphNode two = new GraphNode("2");
		roots.add(two);
		final GraphNode three = new GraphNode("3");
		roots.add(three);
		final GraphNode four = new GraphNode("4");
		roots.add(four);
		final GraphNode five = new GraphNode("5");
		roots.add(five);
		final GraphNode six = new GraphNode("6");
		roots.add(six);

		one.addEdge(two);

		two.addEdge(three);

		three.addEdge(four);

		four.addEdge(five);

		five.addEdge(six);
		
		six.addEdge(one);

		final Set<Stack<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		final Set<Stack<GraphNode>> expected = new HashSet<Stack<GraphNode>>();
		Stack<GraphNode> anSCC = new Stack<GraphNode>();
		anSCC.add(one);
		anSCC.add(two);
		anSCC.add(three);
		anSCC.add(four);
		anSCC.add(five);
		anSCC.add(six);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Stack<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 6, scc.size());
		}
	}
	
	@Test
	public void testEBiDirectional() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();

		// a ring with bi-directional edges
		final GraphNode one = new GraphNode("1");
		roots.add(one);
		final GraphNode two = new GraphNode("2");
		roots.add(two);
		final GraphNode three = new GraphNode("3");
		roots.add(three);
		final GraphNode four = new GraphNode("4");
		roots.add(four);
		final GraphNode five = new GraphNode("5");
		roots.add(five);
		final GraphNode six = new GraphNode("6");
		roots.add(six);

		one.addEdge(two);
		one.addEdge(six);

		two.addEdge(three);
		two.addEdge(one);
		
		three.addEdge(four);
		three.addEdge(two);
		
		four.addEdge(five);
		four.addEdge(three);
		
		five.addEdge(six);
		five.addEdge(four);
		
		six.addEdge(one);
		six.addEdge(five);

		final Set<Stack<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		final Set<Stack<GraphNode>> expected = new HashSet<Stack<GraphNode>>();
		Stack<GraphNode> anSCC = new Stack<GraphNode>();
		anSCC.add(six);
		anSCC.add(three);
		anSCC.add(five);
		anSCC.add(four);
		anSCC.add(one);
		anSCC.add(two);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Stack<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 6, scc.size());
		}
	}
}
