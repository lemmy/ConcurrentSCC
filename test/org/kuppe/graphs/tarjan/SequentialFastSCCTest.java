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

import org.junit.Assert;
import org.junit.Test;

public class SequentialFastSCCTest {
	
	@Test
	public void testEmpty() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();
		
		// No vertices at all
		final Set<Set<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		Assert.assertEquals(0, sccs.size());
	}

	@Test
	public void testSingleVertex() {
		final List<GraphNode> roots = new ArrayList<GraphNode>();
		
		// single vertex with arc to self
		final GraphNode one = new GraphNode("1");
		roots.add(one);
		one.addSuccessor(one);

		final Set<Set<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
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

		one.addSuccessor(two);
		one.addSuccessor(one);

		two.addSuccessor(one);
		two.addSuccessor(three);

		three.addSuccessor(four);

		four.addSuccessor(three);

		final Set<Set<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
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
		
		Assert.assertEquals(sccs.toString(), 2, sccs.size());
		for (Set<GraphNode> scc : sccs) {
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

		one.addSuccessor(one);
		one.addSuccessor(two);
		one.addSuccessor(three);

		two.addSuccessor(one);
		two.addSuccessor(two);
		two.addSuccessor(three);

		three.addSuccessor(one);
		three.addSuccessor(two);
		three.addSuccessor(three);

		final Set<Set<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(one);
		anSCC.add(two);
		anSCC.add(three);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
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

		one.addSuccessor(three);

		two.addSuccessor(three);

		three.addSuccessor(four);
		three.addSuccessor(five);

		four.addSuccessor(one);
		
		five.addSuccessor(two);
		
		final Set<Set<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
		final Set<Set<GraphNode>> expected = new HashSet<Set<GraphNode>>();
		Set<GraphNode> anSCC = new HashSet<GraphNode>();
		anSCC.add(one);
		anSCC.add(two);
		anSCC.add(three);
		anSCC.add(four);
		anSCC.add(five);
		expected.add(anSCC);
		Assert.assertEquals(expected, sccs);

		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
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

		one.addSuccessor(two);

		two.addSuccessor(one);

		three.addSuccessor(four);

		four.addSuccessor(three);
		
		final Set<Set<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
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
		
		Assert.assertEquals(sccs.toString(), 2, sccs.size());
		for (Set<GraphNode> scc : sccs) {
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

		one.addSuccessor(two);

		two.addSuccessor(three);

		three.addSuccessor(four);

		four.addSuccessor(five);

		five.addSuccessor(six);
		
		six.addSuccessor(one);

		final Set<Set<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
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
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
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

		one.addSuccessor(two);
		one.addSuccessor(six);

		two.addSuccessor(three);
		two.addSuccessor(one);
		
		three.addSuccessor(four);
		three.addSuccessor(two);
		
		four.addSuccessor(five);
		four.addSuccessor(three);
		
		five.addSuccessor(six);
		five.addSuccessor(four);
		
		six.addSuccessor(one);
		six.addSuccessor(five);

		final Set<Set<GraphNode>> sccs = new SequentialFastSCC().searchSCCs(roots);
		
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
		
		Assert.assertEquals(sccs.toString(), 1, sccs.size());
		for (Set<GraphNode> scc : sccs) {
			Assert.assertEquals(scc.toString(), 6, scc.size());
		}
	}
}
