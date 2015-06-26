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
		Collection<Arc> arcs = graph.getArcs(two);
		for (Arc arc : arcs) {
			if (arc.getTo() == 3) {
				arc.setTraversed();
			}
		}
		// Check arc to three is still untraversed
		two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);

		/*test*/

		// Un-traversed: {1,2,3}
		Assert.assertEquals(3, graph.getUntraversedArcs(two).size());
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
		
		/*test*/
		
		// Check arc to four is there
		Assert.assertEquals(4, graph.getUntraversedArcs(two).size());
	}
	
	@Test
	public void testContractionInBCorrectArcSet3() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2,3,4);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);

		// Mark all arcs of one done except 4
		Collection<Arc> arcs = graph.getArcs(one);
		for (Arc arc : arcs) {
			if (arc.getTo() != 4) {
				arc.setTraversed();
			}
		}

		// contract one with one extra arc
		one.setParent(two);
		one.set(Visited.PRE);
		two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);

		/*test*/

		// Un-traversed: {1,2,3,4}
		Assert.assertEquals(4, graph.getUntraversedArcs(two).size());
	}
	
	@Test
	public void testContractionInBNewSelfLoop() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2,3,4);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);

		// Mark all arcs of one done except self loop 1
		Collection<Arc> arcs = graph.getArcs(one);
		for (Arc arc : arcs) {
			if (arc.getTo() != 1) {
				arc.setTraversed();
			}
		}
		// Mark all arcs of two done
		graph.getArcs(two).forEach((arc) -> {arc.setTraversed();});

		// contract one with one extra arc
		one.setParent(two);
		one.set(Visited.PRE);
		two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);

		/*test*/
		
		// Un-traversed: {1}
		
		// ...one of which is untraversed (including one's selfloop)
		Assert.assertEquals(1, graph.getUntraversedArcs(two).size());
	}
}
