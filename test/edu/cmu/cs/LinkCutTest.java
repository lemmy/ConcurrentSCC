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
import edu.cmu.cs.LinkCutTreeNode;

/**
 * @see http://www.cs.cmu.edu/~avrim/451f12/lectures/lect1009-linkcut.txt
 */
public class LinkCutTest {

	@Test
	public void testRoot() {
		LinkCutTreeNode root = new LinkCutTreeNode();
		
		assertTrue(root.isroot());
		
		assertEquals(root, LinkCut.root(root));
	}
	
	public void testLinkIdentical() {
		LinkCutTreeNode n = new LinkCutTreeNode();

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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
		assertEquals(child, LinkCut.root(child));

		LinkCut.link(child, root);

		assertTrue(root.isroot());
		assertTrue(child.isroot());
		
		assertEquals(root, LinkCut.root(child));
		assertEquals(root, LinkCut.root(root));
	}

	@Test
	public void testLinkThreeInPath() {
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
		assertEquals(child, LinkCut.root(child));
		
		LinkCutTreeNode childsChild = new LinkCutTreeNode();
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
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
		assertEquals(child, LinkCut.root(child));
		
		LinkCutTreeNode childsChild = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
		assertEquals(child, LinkCut.root(child));
		
		LinkCutTreeNode childsChild = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
		assertEquals(child, LinkCut.root(child));
		
		LinkCutTreeNode childsChild = new LinkCutTreeNode();
		assertEquals(childsChild, LinkCut.root(childsChild));

		LinkCutTreeNode childsChildsChild = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode childA = new LinkCutTreeNode();
		assertEquals(childA, LinkCut.root(childA));
		
		LinkCutTreeNode childB = new LinkCutTreeNode();
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
		assertNotEquals(root, LinkCut.root(new LinkCutTreeNode()));
	}
	
	@Test
	public void testCutRoot() {
		// root
		
		LinkCutTreeNode root = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
		assertEquals(child, LinkCut.root(child));
		LinkCut.link(child, root);

		LinkCutTreeNode childsChild = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
		assertEquals(child, LinkCut.root(child));
		LinkCut.link(child, root);

		LinkCutTreeNode childsChild = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode childA = new LinkCutTreeNode();
		assertEquals(childA, LinkCut.root(childA));
		LinkCut.link(childA, root);

		LinkCutTreeNode childAsChild = new LinkCutTreeNode();
		assertEquals(childAsChild, LinkCut.root(childAsChild));
		LinkCut.link(childAsChild, childA);

		LinkCutTreeNode childB = new LinkCutTreeNode();
		assertEquals(childB, LinkCut.root(childB));
		LinkCut.link(childB, root);

		LinkCutTreeNode childBsChild = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode childA = new LinkCutTreeNode();
		assertEquals(childA, LinkCut.root(childA));
		LinkCut.link(childA, root);

		LinkCutTreeNode childAsChild = new LinkCutTreeNode();
		assertEquals(childAsChild, LinkCut.root(childAsChild));
		LinkCut.link(childAsChild, childA);

		LinkCutTreeNode childB = new LinkCutTreeNode();
		assertEquals(childB, LinkCut.root(childB));
		LinkCut.link(childB, root);

		LinkCutTreeNode childBsChild = new LinkCutTreeNode();
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
		
		LinkCutTreeNode root = new LinkCutTreeNode();
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode();
		assertEquals(child, LinkCut.root(child));
		LinkCut.link(child, root);

		LinkCutTreeNode childAsChild = new LinkCutTreeNode();
		assertEquals(childAsChild, LinkCut.root(childAsChild));
		LinkCut.link(childAsChild, child);

		LinkCutTreeNode childBsChild = new LinkCutTreeNode();
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
