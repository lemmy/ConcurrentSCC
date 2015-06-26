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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class GraphNodeTest {

	@Test
	public void testIsInSameTree() {
		final GraphNode root = new GraphNode(0);
		
		final GraphNode left = new GraphNode(1);
		left.setParent(root);

		final GraphNode right = new GraphNode(2);
		right.setParent(root);
		
		assertTrue(root.isInSameTree(root));

		assertTrue(left.isInSameTree(root));
		assertTrue(root.isInSameTree(left));

		assertTrue(right.isInSameTree(root));
		assertTrue(root.isInSameTree(right));

		assertTrue(left.isInSameTree(left));
		assertTrue(left.isInSameTree(right));
		
		assertTrue(right.isInSameTree(right));
		assertTrue(right.isInSameTree(left));
	}
	
	@Test
	public void testInDifferentTrees() {
		final GraphNode A = new GraphNode(1);
		final GraphNode B = new GraphNode(2);

		assertTrue(A.isInSameTree(A));
		assertFalse(A.isInSameTree(B));
		
		assertTrue(B.isInSameTree(B));
		assertFalse(B.isInSameTree(A));
	}
	
	@Test
	public void testVisitedStateChange() {
		final GraphNode a = new GraphNode(1);
		assertTrue(a.is(Visited.UN));
		a.set(Visited.UN);
		a.set(Visited.PRE);
		assertTrue(a.is(Visited.PRE));
		a.set(Visited.PRE);
		a.set(Visited.POST);
		assertTrue(a.is(Visited.POST));
	}

	@Test
	public void testVisitedStateChangeInvalidDowngrade() {
		final GraphNode a = new GraphNode(1);
		a.set(Visited.PRE);
		assertTrue(a.is(Visited.PRE));
		try {
			a.set(Visited.UN);
		} catch (AssertionError e) {
			return;
		}
		fail("Invalid state downgrade PRE > UN");
	}

	@Test
	public void testVisitedStateChangeInvalidDowngrade2() {
		final GraphNode a = new GraphNode(1);
		a.set(Visited.POST);
		assertTrue(a.is(Visited.POST));
		try {
			a.set(Visited.PRE);
		} catch (AssertionError e) {
			return;
		}
		fail("Invalid state downgrade PRE > UN");
	}
}
