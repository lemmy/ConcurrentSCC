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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class ArcTest {
	
	@Test
	public void testContractionInBCorrectArcSet() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2,3);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);

		one.setParent(two);
		one.set(Visited.PRE);

		// Add redundant unvisited arc to node with arc in visited state:
		// Set one's arc to three as traversed
		Set<Arc> arcs = graph.getArcs(one);
		for (Arc arc : arcs) {
			if (arc.getTo() == 3) {
				arc.setTraversed();
			}
		}
		
		// Check arc to three is still untraversed
		two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);
		arcs = graph.getArcs(two);
		for (Arc arc : arcs) {
			Assert.assertFalse(arc.isTraversed());
		}
	}
	
	@Test
	public void testContractionInBCorrectArcSet2() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2,3,4);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);

		one.setParent(two);
		one.set(Visited.PRE);
		two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);
		
		// Check arc to four is there
		Set<Arc> arcs = graph.getArcs(two);
		Assert.assertEquals(4, arcs.size());
		for (Arc arc : arcs) {
			Assert.assertFalse(arc.isTraversed());
		}
	}
	
	@Test
	public void testContractionInBCorrectArcSet3() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2,3,4);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);

		// Mark all arcs of two done
		Set<Arc> arcs = graph.getArcs(two);
		for (Arc arc : arcs) {
			arc.setTraversed();
		}

		// contract one with one extra arc
		one.setParent(two);
		one.set(Visited.PRE);
		two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);
		
		arcs = graph.getArcs(two);
		Assert.assertEquals(1, arcs.size());
		for (Arc arc : arcs) {
			Assert.assertFalse(arc.isTraversed());
			Assert.assertEquals(4, arc.getTo());
		}
	}
	
	@Test
	public void testContractionInBCorrectArcSet4() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2,3,4);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);

		// Mark all arcs of two done
		Set<Arc> arcs = graph.getArcs(two);
		for (Arc arc : arcs) {
			arc.setTraversed();
		}

		// contract one with one extra arc
		one.setParent(two);
		one.set(Visited.PRE);
		two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);
		
		// test that the self loop is preserved
		arcs = graph.getArcs(two);
		Assert.assertEquals(2, arcs.size());
		for (Arc arc : arcs) {
			Assert.assertFalse(arc.isTraversed());
		}
	}

}
