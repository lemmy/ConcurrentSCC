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

import java.util.Set;

public interface TreeNode {

	/**
	 * @return true iff this node is a root of one tree in the forest of trees;
	 */
	boolean isRoot();

	/**
	 * Connects/Links this node to the given node. This node will be a child of
	 * the given Node n in a tree in the forest of trees. Afterwards,
	 * isRootTo will return true for these two nodes.
	 * <p>
	 * The pre-condition to this method is that both nodes are in separate
	 * trees. A RuntimeException is thrown if this node it *not* a root prior
	 * too calling link.
	 * 
	 * @param n
	 */
	void link(TreeNode parent);

	/**
	 * @return The root to which this tree belongs (potentially itself).
	 */
	TreeNode getRoot();

	/**
	 * @param n A {@link TreeNode}, potentially this node.
	 * @return true iff n and this are in the same tree in the forest of trees.
	 */
	boolean isRootTo(TreeNode aTreeNdde);

	/**
	 * @return The direct parent of this node or null if this node is a root.
	 */
	TreeNode getParent();

	/**
	 * Disconnects the children of this node from this and makes them children of the newParent.
	 * @param newParent The new parent for my children
	 * @param excludes Exclude the nodes in this set from the reLinkChildren operation.
	 */
	void reLinkChildren(TreeNode newParent, Set<? extends TreeNode> excludes);

	/**
	 * Cuts this node off of its tree parent. It becomes a root. Afterwards,
	 * isRootTo will return false for this node and its former parent nodes
	 * all the way to the root.
	 */
	void cut();
	
	/**
	 * @return The set of all (direct) child nodes of this node. Empty if this
	 *         node has no children.
	 */
	Set<TreeNode> getChildren();
	
	/**
	 * @return true iff this node has children.
	 */
	boolean hasChildren();
}