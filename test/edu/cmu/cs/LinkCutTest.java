package edu.cmu.cs;
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
import static org.junit.Assert.*;
import org.junit.Test;

import edu.cmu.cs.LinkCut;
import edu.cmu.cs.Node;

/**
 * @see http://www.cs.cmu.edu/~avrim/451f12/lectures/lect1009-linkcut.txt
 */
public class LinkCutTest {

	@Test
	public void testRoot() {
		final int id = 1;
		Node root = new Node(id);
		
		assertTrue(root.isroot());
		
		assertEquals(root, LinkCut.root(root));
	}
	
	public void testLinkIdentical() {
		Node n = new Node(1);

		try {
			LinkCut.link(n, n);
		} catch (RuntimeException e) {
			return;
		}
		fail();
	}

	@Test
	public void testLinkTwo() {
		// root > child
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));

		LinkCut.link(child, root);

		assertTrue(root.isroot());
		assertTrue(child.isroot());
		
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
	}

	@Test
	public void testLinkThreeInPath() {
		Node root = new Node(1);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));
		
		Node childsChild = new Node(3);
		assertEquals(childsChild, LinkCut.root(childsChild));
	
		
		LinkCut.link(childsChild, child);
		LinkCut.link(child, root);

		assertTrue(root.isroot());
		assertTrue(child.isroot());
		assertTrue(childsChild.isroot());
		
		assertEquals(root, LinkCut.root(childsChild));
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
	}

	@Test
	public void testLinkChildThree() {
		Node root = new Node(1);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));
		
		Node childsChild = new Node(3);
		assertEquals(childsChild, LinkCut.root(childsChild));
	
		LinkCut.link(childsChild, child);
		assertTrue(root.isroot());
		assertTrue(child.isroot());
		assertTrue(childsChild.isroot());
		assertEquals(child, LinkCut.root(childsChild));
		assertEquals(child, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));

		// Invalid link
		try {
			LinkCut.link(childsChild, root);
		} catch (RuntimeException e) {
			return;
		}
		fail();
	}

	@Test
	public void testLinkThreeInPathLinkChilds() {
		// root > child > childsChild
		
		Node root = new Node(1);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));
		
		Node childsChild = new Node(3);
		assertEquals(childsChild, LinkCut.root(childsChild));
	
		// reverse the order in which the nodes are linked
		LinkCut.link(child, root);
		LinkCut.link(childsChild, child);

		assertTrue(root.isroot());
		assertTrue(child.isroot());
		assertTrue(childsChild.isroot());
		
		assertEquals(root, LinkCut.root(childsChild));
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
	}
	
	@Test
	public void testLinkFourInPathLinkChilds() {
		// root > child > childsChild
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));
		
		Node childsChild = new Node(3);
		assertEquals(childsChild, LinkCut.root(childsChild));

		Node childsChildsChild = new Node(4);
		assertEquals(childsChildsChild, LinkCut.root(childsChildsChild));
	
		// reverse the order in which the nodes are linked
		LinkCut.link(childsChildsChild, child);
		LinkCut.link(child, root);
		LinkCut.link(childsChild, child);

		assertTrue(root.isroot());
		assertTrue(child.isroot());
		assertTrue(childsChild.isroot());
		assertTrue(childsChildsChild.isroot());
		
		assertEquals(root, LinkCut.root(childsChildsChild));
		assertEquals(root, LinkCut.root(childsChild));
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
	}

	@Test
	public void testLinkThree() {
		// root with child left and right
		// root > childA /\ root > childB
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node childA = new Node(2);
		assertEquals(childA, LinkCut.root(childA));
		
		Node childB = new Node(3);
		assertEquals(childB, LinkCut.root(childB));
	

		LinkCut.link(childA, root);
		LinkCut.link(childB, root);

		assertTrue(root.isroot());
		assertTrue(childA.isroot());
		assertTrue(childB.isroot());
		
		assertEquals(root, LinkCut.root(childA));
		assertEquals(root, LinkCut.root(childB));
		assertEquals(root, LinkCut.root(root));
		
		//
		assertNotEquals(root, LinkCut.root(new Node(3)));
	}
	
	@Test
	public void testCutRoot() {
		// root
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		assertTrue(root.isroot());

		// Doesn't do anything
		LinkCut.cut(root);
		assertEquals(root, LinkCut.root(root));
		assertTrue(root.isroot());
	}
	
	@Test
	public void testCutTwo() {
		// root > child
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));

		LinkCut.link(child, root);

		assertTrue(root.isroot());
		assertTrue(child.isroot());
		
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
		
		// up to this line identical to testLinkTwo()
		LinkCut.cut(child);
		
		assertNotEquals(root, LinkCut.root(child));
		assertEquals(child, LinkCut.root(child));
	}

	@Test
	public void testCutThreeInPath() {
		// root > child (cut) > childsChild
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));
		LinkCut.link(child, root);

		Node childsChild = new Node(3);
		assertEquals(childsChild, LinkCut.root(childsChild));
		LinkCut.link(childsChild, child);

		assertTrue(root.isroot());
		assertTrue(child.isroot());
		assertTrue(childsChild.isroot());
		
		assertEquals(root, LinkCut.root(childsChild));
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
		
		// Cut off child
		LinkCut.cut(child);
		
		assertEquals(child, LinkCut.root(childsChild));
		assertEquals(child, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
	}
	
	@Test
	public void testCutThreeInPathChildsChild() {
		// root > child > childsChild (cut)
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));
		LinkCut.link(child, root);

		Node childsChild = new Node(3);
		assertEquals(childsChild, LinkCut.root(childsChild));
		LinkCut.link(childsChild, child);

		assertTrue(root.isroot());
		assertTrue(child.isroot());
		assertTrue(childsChild.isroot());
		
		assertEquals(root, LinkCut.root(childsChild));
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
		
		// Cut off child
		LinkCut.cut(childsChild);
		
		assertEquals(childsChild, LinkCut.root(childsChild));
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
	}
	
	@Test
	public void testCutThree() {
		// root > childA > childAsChild (cut) /\
		// root > childB > childBsChild
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node childA = new Node(2);
		assertEquals(childA, LinkCut.root(childA));
		LinkCut.link(childA, root);

		Node childAsChild = new Node(3);
		assertEquals(childAsChild, LinkCut.root(childAsChild));
		LinkCut.link(childAsChild, childA);

		Node childB = new Node(4);
		assertEquals(childB, LinkCut.root(childB));
		LinkCut.link(childB, root);

		Node childBsChild = new Node(5);
		assertEquals(childBsChild, LinkCut.root(childBsChild));
		LinkCut.link(childBsChild, childB);
		
		assertTrue(root.isroot());
		assertTrue(childA.isroot());
		assertTrue(childAsChild.isroot());
		assertTrue(childB.isroot());
		assertTrue(childBsChild.isroot());
		
		assertEquals(root, LinkCut.root(childAsChild));
		assertEquals(root, LinkCut.root(childA));
		assertEquals(root, LinkCut.root(childBsChild));
		assertEquals(root, LinkCut.root(childB));
		assertEquals(root, LinkCut.root(root));
		
		// Cut off child
		LinkCut.cut(childAsChild);
		
		assertEquals(childAsChild, LinkCut.root(childAsChild));
		assertEquals(root, LinkCut.root(childA));
		assertEquals(root, LinkCut.root(childBsChild));
		assertEquals(root, LinkCut.root(childB));
		assertEquals(root, LinkCut.root(root));
	}
	
	@Test
	public void testCutThreeDifferentCut() {
		// root > childA(cut) > childAsChild /\
		// root > childB > childBsChild
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node childA = new Node(2);
		assertEquals(childA, LinkCut.root(childA));
		LinkCut.link(childA, root);

		Node childAsChild = new Node(3);
		assertEquals(childAsChild, LinkCut.root(childAsChild));
		LinkCut.link(childAsChild, childA);

		Node childB = new Node(4);
		assertEquals(childB, LinkCut.root(childB));
		LinkCut.link(childB, root);

		Node childBsChild = new Node(5);
		assertEquals(childBsChild, LinkCut.root(childBsChild));
		LinkCut.link(childBsChild, childB);
		
		assertTrue(root.isroot());
		assertTrue(childA.isroot());
		assertTrue(childAsChild.isroot());
		assertTrue(childB.isroot());
		assertTrue(childBsChild.isroot());
		
		assertEquals(root, LinkCut.root(childAsChild));
		assertEquals(root, LinkCut.root(childA));
		assertEquals(root, LinkCut.root(childBsChild));
		assertEquals(root, LinkCut.root(childB));
		assertEquals(root, LinkCut.root(root));
		
		// Cut off child
		LinkCut.cut(childA);
		
		assertEquals(childA, LinkCut.root(childAsChild));
		assertEquals(childA, LinkCut.root(childA));
		
		assertEquals(root, LinkCut.root(childBsChild));
		assertEquals(root, LinkCut.root(childB));
		assertEquals(root, LinkCut.root(root));
	}
	
	@Test
	public void testCutThreeCutSplitNode() {
		// root > child(cut) > childsChildA /\
		// root > child(cut) > childsChildB
		
		final int id = 1;
		Node root = new Node(id);
		assertEquals(root, LinkCut.root(root));
		
		Node child = new Node(2);
		assertEquals(child, LinkCut.root(child));
		LinkCut.link(child, root);

		Node childAsChild = new Node(3);
		assertEquals(childAsChild, LinkCut.root(childAsChild));
		LinkCut.link(childAsChild, child);

		Node childBsChild = new Node(5);
		assertEquals(childBsChild, LinkCut.root(childBsChild));
		LinkCut.link(childBsChild, child);
		
		assertTrue(root.isroot());
		assertTrue(child.isroot());
		assertTrue(childAsChild.isroot());
		assertTrue(childBsChild.isroot());
		
		assertEquals(root, LinkCut.root(childAsChild));
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(childBsChild));
		assertEquals(root, LinkCut.root(root));
		
		// Cut off child
		LinkCut.cut(child);
		
		assertEquals(child, LinkCut.root(childAsChild));
		assertEquals(child, LinkCut.root(child));
		assertEquals(child, LinkCut.root(childBsChild));
		
		assertEquals(root, LinkCut.root(root));
	}
}
