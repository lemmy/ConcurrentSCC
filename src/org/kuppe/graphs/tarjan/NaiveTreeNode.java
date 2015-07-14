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

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class NaiveTreeNode implements TreeNode {

	protected final int id;
	
	protected volatile NaiveTreeNode parent;
	
	protected NaiveTreeNode leftChild;
	protected NaiveTreeNode rightChild;
	
	/**
	 * null if this is leftmost sibling of parent's child aka parent's leftChild
	 */
	protected NaiveTreeNode leftSibling;
	/**
	 * null if this is rightmost sibling of parent's child aka parent's rightChild
	 */
	protected NaiveTreeNode rightSibling;

	public NaiveTreeNode(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#isRoot()
	 */
	@Override
	public boolean isRoot() {
		//Lock on this 
		return parent == null;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#link(org.kuppe.graphs.tarjan.TreeNode)
	 */
	@Override
	public void link(TreeNode aParent) {
		//Lock on this and parent
		if (this.parent != null) {
			throw new RuntimeException("non-root link");
		}

		this.parent = (NaiveTreeNode) aParent;

		// Add this to parent's set of children
		if (parent.leftChild == null) {
			parent.leftChild = this;
			parent.rightChild = this;
		} else {
			this.rightSibling = parent.leftChild;
			this.rightSibling.leftSibling = this;
			parent.leftChild = this;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#getRoot()
	 */
	@Override
	public TreeNode getRoot() {
		//Lock on path from this to root
		NaiveTreeNode p = this;
		while (p != null) {
			if (p.parent == null) {
				return p;
			}
			p = p.parent;
		}
		
		throw new RuntimeException("couldn't determine TreeNode's parent.");
	}
	

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#isRootTo(org.kuppe.graphs.tarjan.TreeNode)
	 */
	@Override
	public boolean isRootTo(TreeNode aTreeNode) {
		//Lock on aTreeNode and its path to its root 
		return aTreeNode.getRoot() == this;
	}

	/* (non-Javadoc) 
	 * @see org.kuppe.graphs.tarjan.TreeNode#getParent()
	 */
	@Override
	public TreeNode getParent() {
		//Lock on this 
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#reLinkChildren(org.kuppe.graphs.tarjan.TreeNode)
	 */
	@Override
	public void reLinkChildren(final TreeNode newParent) {
		if (this.leftChild == null) {
			return;
		}
		
		//Lock on this and this children
		final NaiveTreeNode naiveNewParent = (NaiveTreeNode) newParent;
		
		// update my childs parent pointers
		//TODO run in parallel?
		NaiveTreeNode node = leftChild;
		while (node != rightChild) {
			node.parent = naiveNewParent;
			node = node.rightSibling;
		}
		rightChild.parent = naiveNewParent;

		// append my double linked list of children to the new parent's one.
		this.rightChild.rightSibling = naiveNewParent.leftChild;
		if (naiveNewParent.leftChild != null) {
			naiveNewParent.leftChild.leftSibling = this.rightChild;
		} else {
			// naiveNewParent has no children yet
			naiveNewParent.rightChild = this.rightChild;
		}
		naiveNewParent.leftChild = this.leftChild;
		
		// forget my former children
		this.rightChild = null;
		this.leftChild = null;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#cut()
	 */
	@Override
	public void cut() {
		//Lock on this and parent
		if (this.leftSibling == null && this.rightSibling == null) {
			this.parent.leftChild = null;
			this.parent.rightChild = null;
			this.parent = null;
			return;
		} 
		if (this.leftSibling == null) {
			// Left node:
			this.rightSibling.leftSibling = null;
			this.parent.leftChild = this.rightSibling;
		} else if (this.rightSibling == null) {
			// Right node: 
			this.leftSibling.rightSibling = null;
			this.parent.rightChild = this.leftSibling;	
		} else {
			// Middle node: Unlink this from double linked list by directly connecting our
			// left and right siblings.
			this.leftSibling.rightSibling = this.rightSibling;
			this.rightSibling.leftSibling = this.leftSibling;
		}
			
		this.leftSibling = null;
		this.rightSibling = null;
		
		this.parent = null;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#getChildren()
	 */
	@Override
	public Set<TreeNode> getChildren() {
		//Lock on this
		final Set<TreeNode> result = new HashSet<>();
		if (!hasChildren()) {
			return result;
		}
		NaiveTreeNode node = leftChild;
		while (node != rightChild) {
			result.add(node);
			node = node.rightSibling;
		}
		result.add(rightChild);
		return result;
	}
	
	public Iterator<NaiveTreeNode> iterator() {
		return new Iterator<NaiveTreeNode>() {
			private NaiveTreeNode node = leftChild;
			
			@Override
			public boolean hasNext() {
				return node != null;
			}

			@Override
			public NaiveTreeNode next() {
				if (node == null) {
					throw new NoSuchElementException();
				}
				final NaiveTreeNode oldNode = node;
				node = node.rightSibling;
				return oldNode;
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		//Lock on this and children
		return leftChild != null;
	}
}
