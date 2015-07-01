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

package edu.cmu.cs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.junit.Test;

/**
 * @see http://www.cs.cmu.edu/~avrim/451f12/lectures/lect1009-linkcut.txt
 */
public class LinkCutTest {

	@Test
	public void testRoot() {
		LinkCutTreeNode root = new LinkCutTreeNode();
		
		assertTrue(root.isroot());

		assertTrue(root.isRoot());
		
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

		assertTrue(root.isRoot());
		assertFalse(child.isRoot());
		
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

		assertTrue(root.isRoot());
		assertFalse(child.isRoot());
		assertFalse(childsChild.isRoot());
		
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
		assertFalse(childsChild.isRoot());
		assertTrue(root.isRoot());
		assertTrue(child.isRoot());
		
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
		assertTrue(childsChild.isRoot());
		assertTrue(root.isRoot());
		assertFalse(child.isRoot());
		
		LinkCut.link(childsChild, child);
		assertFalse(childsChild.isRoot());
		assertFalse(child.isRoot());
		assertTrue(root.isRoot());

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
		assertFalse(childsChildsChild.isRoot());
		assertTrue(childsChild.isRoot());
		assertTrue(child.isRoot());
		assertTrue(root.isRoot());

		LinkCut.link(child, root);
		assertFalse(childsChildsChild.isRoot());
		assertTrue(childsChild.isRoot());
		assertFalse(child.isRoot());
		assertTrue(root.isRoot());

		LinkCut.link(childsChild, child);
		assertFalse(childsChildsChild.isRoot());
		assertFalse(childsChild.isRoot());
		assertFalse(child.isRoot());
		assertTrue(root.isRoot());

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
		
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode childA = new LinkCutTreeNode(1);
		assertEquals(childA, LinkCut.root(childA));
		
		LinkCutTreeNode childB = new LinkCutTreeNode(2);
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
		
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode(1);
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
		
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode(1);
		assertEquals(child, LinkCut.root(child));
		LinkCut.link(child, root);

		LinkCutTreeNode childsChild = new LinkCutTreeNode(2);
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
		
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		assertEquals(root, LinkCut.root(root));
		
		LinkCutTreeNode child = new LinkCutTreeNode(1);
		assertEquals(child, LinkCut.root(child));
		LinkCut.link(child, root);

		LinkCutTreeNode childsChild = new LinkCutTreeNode(2);
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
	
	@Test
	public void testParentsChildrenOnChildCut() {
		LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);
		// Do some reads (mutations) on the graph. Reading a Link/Cut tree
		// changes its internal state.
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));


		final Stack<LinkCutTreeNode> result = (Stack<LinkCutTreeNode>) LinkCut.children(three,
				new Stack<LinkCutTreeNode>());
		assertEquals(6, result.size());
		assertTrue(result.contains(three));
		assertTrue(result.contains(two));
		assertTrue(result.contains(five));
		assertTrue(result.contains(six));
		assertTrue(result.contains(one));
		assertTrue(result.contains(seven));
		
		// cut two off of three
		LinkCut.cut(two);
		
		// two has no parent anymore
		assertNull(LinkCut.parent(two));
		assertTrue(two.isRoot());
		
		// two is the only elements returned by parents
		List<LinkCutTreeNode> parents = (List<LinkCutTreeNode>) LinkCut.parents(two, new ArrayList<LinkCutTreeNode>());
		assertEquals(1, parents.size());
		assertEquals(two, parents.get(0));
		
		// Test three's children don't include two's subtree
		final Stack<LinkCutTreeNode> children = (Stack<LinkCutTreeNode>) LinkCut.children(three,
				new Stack<LinkCutTreeNode>());
		assertEquals(3, children.size());
		assertTrue(children.contains(three));
		assertTrue(children.contains(one));
		assertTrue(children.contains(seven));
	}

	@Test
	public void testIsRootAndParent() {
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		assertEquals(root, LinkCut.root(root));
		assertTrue(root.isRoot());
		assertTrue(root.isroot());

		// Add one as child of root
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		assertTrue(one.isRoot());
		assertTrue(one.isroot());
		
		LinkCut.link(one, root);
		assertFalse(one.isRoot());
		assertTrue(root.isRoot());

		
		// parent alters the tree
		assertEquals(root, LinkCut.parent(one));
		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
	}
	
	@Test
	public void testGetPath() {
		// root > child > childsChildA /\
		// root > child > childsChildB
		
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
		
		
		assertEquals(child, LinkCut.parent(childAsChild));
		assertEquals(child, LinkCut.parent(childBsChild));
		assertEquals(root, LinkCut.parent(child));
		assertNull(LinkCut.parent(root));
	}

	@Test
	public void testGetPath2() {
		// root > childA > childAsChild /\
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
		
		// test
		assertEquals(childA, LinkCut.parent(childAsChild));
		assertEquals(childB, LinkCut.parent(childBsChild));
		assertEquals(root, LinkCut.parent(childA));
		assertEquals(root, LinkCut.parent(childB));
		assertNull(LinkCut.parent(root));
	}
	
	@Test
	public void testGetPath3() {
		// root > child > childsChild
		
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
		
		//test
		assertEquals(child, LinkCut.parent(childsChild));
		assertEquals(root, LinkCut.parent(child));
		assertNull(LinkCut.parent(root));
	}
	
	@Test
	public void testGetPath4() {
		// root > child > childsChild
		
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
		
		//test before tree modification
		assertEquals(child, LinkCut.parent(childsChild));
		assertEquals(root, LinkCut.parent(child));
		assertNull(LinkCut.parent(root));
		
		//add node to graph and test again...
		LinkCutTreeNode newNode = new LinkCutTreeNode();
		LinkCut.link(newNode, child);
		assertEquals(root, LinkCut.root(newNode));
		assertEquals(child, LinkCut.parent(newNode));
		
		// test
		assertEquals(child, LinkCut.parent(childsChild));
		assertEquals(root, LinkCut.parent(child));
		assertNull(LinkCut.parent(root));
	}

	@Test
	public void testGetPathAndCut() {
		// root > child > childsChild
		
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

		assertTrue(root.isRoot());
		assertFalse(child.isRoot());
		assertFalse(childsChild.isRoot());
		
		//test
		assertEquals(child, LinkCut.parent(childsChild));
		LinkCut.cut(childsChild);
		assertEquals(root, LinkCut.parent(child));
		assertTrue(root.isRoot());
		assertFalse(child.isRoot());
		assertTrue(childsChild.isRoot());
		
		LinkCut.cut(child);
		assertNull(LinkCut.parent(root));
		assertTrue(root.isRoot());
		assertTrue(child.isRoot());
		assertTrue(childsChild.isRoot());
	}

	@Test
	public void testGetPath5() {
		LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		
		// Add one as child of three
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		assertTrue(one.isRoot());
		LinkCut.link(one, three);
		assertFalse(one.isRoot());
		assertTrue(three.isRoot());
		assertNull(LinkCut.parent(three));
		assertEquals(three, LinkCut.root(three));
		assertEquals(three, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertFalse(one.isRoot());
		assertTrue(three.isRoot());

		// Add two as child of three
		LinkCutTreeNode two = new LinkCutTreeNode(2);
		assertTrue(two.isRoot());
		LinkCut.link(two, three);
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertTrue(three.isRoot());
		assertNull(LinkCut.parent(three));
		assertEquals(three, LinkCut.root(three));
		assertEquals(three, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertEquals(three, LinkCut.root(two));
		assertEquals(three, LinkCut.parent(two));
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertTrue(three.isRoot());
		
		// Add five as child of two
		LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		assertNull(LinkCut.parent(three));
		assertEquals(three, LinkCut.root(three));
		assertEquals(three, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertEquals(three, LinkCut.root(two));
		assertEquals(three, LinkCut.parent(two));
		assertEquals(three, LinkCut.root(five));
		assertEquals(two, LinkCut.parent(five));
		
		// Add root as parent of three
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		assertEquals(root, LinkCut.root(three));
		assertEquals(root, LinkCut.parent(three));
		assertEquals(root, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertEquals(root, LinkCut.root(two));
		assertEquals(three, LinkCut.parent(two));
		assertEquals(root, LinkCut.root(five));
		assertEquals(two, LinkCut.parent(five));
	}
	
	@Test
	public void testGetPath6() {
		LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		assertTrue(three.isRoot());
		
		// Add one as child of three
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		assertFalse(one.isRoot());
		assertTrue(three.isRoot());
		assertNull(LinkCut.parent(three));
		assertEquals(three, LinkCut.root(three));
		assertEquals(three, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertFalse(one.isRoot());
		assertTrue(three.isRoot());

		// Add two as child of three
		LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		assertNull(LinkCut.parent(three));
		assertEquals(three, LinkCut.root(three));
		assertEquals(three, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertEquals(three, LinkCut.root(two));
		assertEquals(three, LinkCut.parent(two));
		
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertTrue(three.isRoot());
		
		// Add root as parent of three
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		assertNull(LinkCut.parent(root));
		assertEquals(root, LinkCut.root(three));
		assertEquals(root, LinkCut.parent(three));
		assertEquals(root, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertEquals(root, LinkCut.root(two));
		assertEquals(three, LinkCut.parent(two));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		
		// Add five as child of two
		LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		assertNull(LinkCut.parent(root));
		assertEquals(root, LinkCut.root(three));
		assertEquals(root, LinkCut.parent(three));
		assertEquals(root, LinkCut.root(one));
		assertEquals(root, LinkCut.root(three));
		assertEquals(root, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertEquals(root, LinkCut.root(two));
		assertEquals(three, LinkCut.parent(two));
		assertEquals(two, LinkCut.parent(five));
		assertEquals(root, LinkCut.root(five));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());

		// Add six as child of five
		LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		assertNull(LinkCut.parent(root));
		assertEquals(root, LinkCut.root(three));
		assertEquals(root, LinkCut.parent(three));
		assertEquals(root, LinkCut.root(one));
		assertEquals(root, LinkCut.root(three));
		assertEquals(root, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertEquals(root, LinkCut.root(two));
		assertEquals(three, LinkCut.parent(two));
		assertEquals(root, LinkCut.root(five));
		assertEquals(two, LinkCut.parent(five));
		assertEquals(five, LinkCut.parent(six));
		assertEquals(root, LinkCut.root(six));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		
		// Add seven as child of one
		LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);
		assertNull(LinkCut.parent(root));
		assertEquals(root, LinkCut.root(three));
		assertEquals(root, LinkCut.parent(three));
		assertEquals(root, LinkCut.root(one));
		assertEquals(root, LinkCut.root(three));
		assertEquals(root, LinkCut.root(one));
		assertEquals(three, LinkCut.parent(one));
		assertEquals(root, LinkCut.root(two));
		assertEquals(three, LinkCut.parent(two));
		assertEquals(root, LinkCut.root(five));
		assertEquals(two, LinkCut.parent(five));
		assertEquals(five, LinkCut.parent(six));
		assertEquals(root, LinkCut.root(six));
		assertEquals(one, LinkCut.parent(seven));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());
	}
	
	@Test
	public void testParentsRoot() {
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		final Stack<LinkCutTreeNode> parents = (Stack<LinkCutTreeNode>) LinkCut.parents(root,
				new Stack<LinkCutTreeNode>());
		assertEquals(1, parents.size());
		assertTrue(parents.contains(root));

		assertTrue(root.isRoot());
	}

	@Test
	public void testParentsPathA() {
		LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);

		
		// Do some reads on the graph
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());
		
		final Stack<LinkCutTreeNode> parents = (Stack<LinkCutTreeNode>) LinkCut.parents(seven,
				new Stack<LinkCutTreeNode>());
		assertEquals(4, parents.size());
		assertEquals(seven, parents.pop());
		assertEquals(one, parents.pop());
		assertEquals(three, parents.pop());
		assertEquals(root, parents.pop());
	}
	
	@Test
	public void testParentsPathB() {
		LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);

		// Do some reads (mutations) on the graph
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());

		final Stack<LinkCutTreeNode> parents = (Stack<LinkCutTreeNode>) LinkCut.parents(six,
				new Stack<LinkCutTreeNode>());
		assertEquals(5, parents.size());
		assertEquals(six, parents.pop());
		assertEquals(five, parents.pop());
		assertEquals(two, parents.pop());
		assertEquals(three, parents.pop());
		assertEquals(root, parents.pop());
	}
	
	@Test
	public void testParentsPathBArray() {
		LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);

		// Do some reads (mutations) on the graph
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());

		final List<LinkCutTreeNode> parents = (List<LinkCutTreeNode>) LinkCut.parents(six,
				new ArrayList<LinkCutTreeNode>());
		assertEquals(5, parents.size());
		assertEquals(root, parents.get(0));
		assertEquals(three, parents.get(1));
		assertEquals(two, parents.get(2));
		assertEquals(five, parents.get(3));
		assertEquals(six, parents.get(4));
	}
	
	@Test
	public void testChildrenOnChild() {
		LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);
		// Do some reads (mutations) on the graph. Reading a Link/Cut tree
		// changes its internal state.
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());

		final Stack<LinkCutTreeNode> result = (Stack<LinkCutTreeNode>) LinkCut.children(six,
				new Stack<LinkCutTreeNode>());
		assertEquals(1, result.size());
		assertTrue(result.contains(six));
	}

	@Test
	public void testChildsRoot() {
		final LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		final LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		final LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		final LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		final LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		final LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		final LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);
		// Do some reads (mutations) on the graph. Reading a Link/Cut tree
		// changes its internal state.
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());

		final Stack<LinkCutTreeNode> result = (Stack<LinkCutTreeNode>) LinkCut.children(root,
				new Stack<LinkCutTreeNode>());
		assertEquals(7, result.size());
		assertTrue(result.contains(root));
		assertTrue(result.contains(three));
		assertTrue(result.contains(two));
		assertTrue(result.contains(five));
		assertTrue(result.contains(six));
		assertTrue(result.contains(one));
		assertTrue(result.contains(seven));
	}

	@Test
	public void testChildsSplitNode() {
		final LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		final LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		final LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		final LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		final LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		final LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		final LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);
		// Do some reads (mutations) on the graph. Reading a Link/Cut tree
		// changes its internal state.
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());

		final Stack<LinkCutTreeNode> result = (Stack<LinkCutTreeNode>) LinkCut.children(three,
				new Stack<LinkCutTreeNode>());
		assertEquals(6, result.size());
		assertTrue(result.contains(three));
		assertTrue(result.contains(two));
		assertTrue(result.contains(five));
		assertTrue(result.contains(six));
		assertTrue(result.contains(one));
		assertTrue(result.contains(seven));
	}

	@Test
	public void testChildsPathA() {
		final LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		final LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		final LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		final LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		final LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		final LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		final LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);
		// Do some reads (mutations) on the graph. Reading a Link/Cut tree
		// changes its internal state.
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());
	
		// TODO Add children
		final Stack<LinkCutTreeNode> result = (Stack<LinkCutTreeNode>) LinkCut.children(two,
				new Stack<LinkCutTreeNode>());
		assertEquals(3, result.size());
		assertTrue(result.contains(two));
		assertTrue(result.contains(five));
		assertTrue(result.contains(six));
	}		

	@Test
	public void testChildsPathB() {
		LinkCutTreeNode three = new LinkCutTreeNode(3);
		assertEquals(three, LinkCut.root(three));
		// Add one as child of three
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		LinkCut.link(one, three);
		// Add two as child of three
		LinkCutTreeNode two = new LinkCutTreeNode(2);
		LinkCut.link(two, three);
		// Add root as parent of three
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		LinkCut.link(three, root);
		// Add five as child of two
		LinkCutTreeNode five = new LinkCutTreeNode(5);
		LinkCut.link(five, two);
		// Add six as child of five
		LinkCutTreeNode six = new LinkCutTreeNode(6);
		LinkCut.link(six, five);
		// Add seven as child of one
		LinkCutTreeNode seven = new LinkCutTreeNode(7);
		LinkCut.link(seven, one);
		// Do some reads (mutations) on the graph
		assertEquals(root, LinkCut.root(six));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(seven));

		assertTrue(root.isRoot());
		assertFalse(one.isRoot());
		assertFalse(two.isRoot());
		assertFalse(three.isRoot());
		assertFalse(five.isRoot());
		assertFalse(six.isRoot());
		assertFalse(seven.isRoot());
				
		// TODO Add children
		final Stack<LinkCutTreeNode> result = (Stack<LinkCutTreeNode>) LinkCut.children(one,
				new Stack<LinkCutTreeNode>());
		assertEquals(2, result.size());
		assertTrue(result.contains(one));
		assertTrue(result.contains(seven));
	}
	
	@Test
	public void testThreeChilds() {
		LinkCutTreeNode root = new LinkCutTreeNode(0);
		
		LinkCutTreeNode one = new LinkCutTreeNode(1);
		one.link(root);
		LinkCutTreeNode two = new LinkCutTreeNode(1);
		two.link(root);
		LinkCutTreeNode three = new LinkCutTreeNode(1);
		three.link(root);
		LinkCutTreeNode four = new LinkCutTreeNode(1);
		four.link(root);
		
		assertEquals(root, LinkCut.root(four));
		assertEquals(root, LinkCut.root(two));
		assertEquals(root, LinkCut.root(root));
		assertEquals(root, LinkCut.root(one));
		
		assertEquals(4, root.children.size());
		
		four.cut();
		assertEquals(3, root.children.size());

		three.cut();
		assertEquals(2, root.children.size());
		
		two.cut();
		assertEquals(1, root.children.size());

		one.cut();
		assertEquals(0, root.children.size());
	}
}