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

import org.junit.Assert;
import org.junit.Test;

public class ArcTest {

	@Test
	public void testEmptyArcSet() {
		final Graph graph = new Graph();
		final GraphNode one = new GraphNode(1);
		graph.addNode(one);
		
		Assert.assertFalse(one.hasArcs());
		Assert.assertEquals(0, graph.getUntraversedArcs(one).size());
	}

	@Test
	public void testOneArcSet() {
		final Graph graph = new Graph();
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 4711);
		
		Assert.assertTrue(one.hasArcs());
		Assert.assertEquals(1, graph.getUntraversedArcs(one).size());
		
		Assert.assertTrue(one.hasArcs());
		Assert.assertEquals(1, graph.getUntraversedArcs(one).size());

		one.removeArc(4711);
		Assert.assertFalse(one.hasArcs());
		Assert.assertEquals(0, graph.getUntraversedArcs(one).size());
	}
		
	@Test
	public void testContractionInBCorrectArcSet() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);

		one.setParent(two);
		
		// Check arc to three is still untraversed
		two.contract(new HashMap<GraphNode, GraphNode>(0), graph, one);

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
		
		two.contract(new HashMap<GraphNode, GraphNode>(0), graph, one);
		
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

		// contract one with one extra arc
		one.setParent(two);
		two.contract(new HashMap<GraphNode, GraphNode>(0), graph, one);

		/*test*/

		// Un-traversed: {1,2,3,4}
		Assert.assertEquals(4, graph.getUntraversedArcs(two).size());
	}
	
	@Test
	public void testContractionInBNewSelfLoop() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2,3);


		// Mark all arcs of two done
		graph.getArcs(two).clear();

		// contract one with one extra arc
		one.setParent(two);
		two.contract(new HashMap<GraphNode, GraphNode>(0), graph, one);

		/*test*/
		
		// Un-traversed: {1}
		
		// ...one of which is untraversed (including one's selfloop)
		Assert.assertEquals(1, graph.getUntraversedArcs(two).size());
	}
}
