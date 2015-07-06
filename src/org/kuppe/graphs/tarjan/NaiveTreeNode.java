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
import java.util.Set;

public abstract class NaiveTreeNode implements TreeNode {

	protected final int id;
	private final Set<TreeNode> children = new HashSet<>();
	
	private NaiveTreeNode parent;
	

	public NaiveTreeNode(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#isRoot()
	 */
	@Override
	public boolean isRoot() {
		return parent == null;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#link(org.kuppe.graphs.tarjan.TreeNode)
	 */
	@Override
	public void link(TreeNode parent) {
		if (this.parent != null) {
			throw new RuntimeException("non-root link");
		}

		this.parent = (NaiveTreeNode) parent;

		// Add this to parent's set of children
		final NaiveTreeNode naiveParent = (NaiveTreeNode) parent;
		naiveParent.children.add(this); 
	}
	
	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#getRoot()
	 */
	@Override
	public TreeNode getRoot() {
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
		return aTreeNode.getRoot() == this;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#getParent()
	 */
	@Override
	public TreeNode getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#reLinkChildren(org.kuppe.graphs.tarjan.TreeNode, java.util.Set)
	 */
	@Override
	public void reLinkChildren(final TreeNode newParent, Set<? extends TreeNode> excludes) {
		for (TreeNode myChild : getChildren()) { // Take copy of this.children. this.children is modified during loop.
			if (!excludes.contains(myChild)) {
				myChild.cut();
				myChild.link(newParent);
			}
		}
		
		// Clear my own children now that they have been relinked.
		this.children.clear();
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#cut()
	 */
	@Override
	public void cut() {
		// Remove this from parents set of children
		final NaiveTreeNode naiveParent = (NaiveTreeNode) parent;
		naiveParent.children.remove(this); 
	
		this.parent = null;
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#getChildren()
	 */
	@Override
	public Set<TreeNode> getChildren() {
		return new HashSet<>(children);
	}

	/* (non-Javadoc)
	 * @see org.kuppe.graphs.tarjan.TreeNode#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}
}
