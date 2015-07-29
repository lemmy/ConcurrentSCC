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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;
import org.kuppe.graphs.tarjan.GraphNode.Visited;
import org.kuppe.graphs.tarjan.copy.DeepCopy;

public class GraphTest {
	
	@Test
	public void testClone() {
		final Graph graph = new Graph();
		
		final GraphNode gn = new GraphNode(0);
		graph.addNode(gn, 3);

		final LinkedList<Integer> arcs = new LinkedList<>();
		arcs.add(1);
		arcs.add(2);
		arcs.add(3);
		gn.setArcs(arcs);
		assertEquals(3, gn.getArcs().size());
		
		assertTrue(gn.is(Visited.UN));
		
		
		final Graph clone = (Graph) DeepCopy.copy(graph);
		assertFalse(graph == clone);
		
		final GraphNode cloneNode = clone.get(0);
		assertEquals(3, cloneNode.getArcs().size());
		assertTrue(cloneNode.is(Visited.UN));
		
		gn.removeArc(1);
		assertEquals(3, cloneNode.getArcs().size());
		
		gn.clearArcs();
		assertNull(gn.getArcs());
		assertEquals(3, cloneNode.getArcs().size());
		
		gn.set(Visited.POST);
		assertTrue(gn.is(Visited.POST));
		assertTrue(cloneNode.is(Visited.UN));
		
		assertTrue(gn.tryLock());
		assertFalse(cloneNode.isLocked());
		
		cloneNode.clearArcs();
		cloneNode.set(Visited.POST);
		assertTrue(clone.checkPostCondition());
		
		gn.unlock();
		assertTrue(graph.checkPostCondition());
	}
}
