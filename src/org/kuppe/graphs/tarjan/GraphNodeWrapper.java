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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//TODO Extract Interface to not extend GraphNode. After all, we don't want to inherit its fields.
public class GraphNodeWrapper extends GraphNode {

	private GraphNode wrapped;

	public GraphNodeWrapper(GraphNode wrapped) {
		super(4711, wrapped.graph);
		this.wrapped = wrapped;
	}
	
	public void setWrapped(GraphNode newWrapped) {
		this.wrapped = newWrapped;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return wrapped.hashCode();
	}

	/**
	 * @param v
	 * @return
	 * @see org.kuppe.graphs.tarjan.GraphNode#is(org.kuppe.graphs.tarjan.GraphNode.Visited)
	 */
	public boolean is(Visited v) {
		return wrapped.is(v);
	}

	/**
	 * @param v
	 * @return
	 * @see org.kuppe.graphs.tarjan.GraphNode#isNot(org.kuppe.graphs.tarjan.GraphNode.Visited)
	 */
	public boolean isNot(Visited v) {
		return wrapped.isNot(v);
	}

	/**
	 * @param visited
	 * @see org.kuppe.graphs.tarjan.GraphNode#set(org.kuppe.graphs.tarjan.GraphNode.Visited)
	 */
	public void set(Visited visited) {
		wrapped.set(visited);
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#isRoot()
	 */
	public boolean isRoot() {
		return wrapped.isRoot();
	}

	/**
	 * @param aParent
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#link(org.kuppe.graphs.tarjan.TreeNode)
	 */
	public void link(TreeNode aParent) {
		wrapped.link(aParent);
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.GraphNode#toString()
	 */
	public String toString() {
		return wrapped.toString();
	}

	/**
	 * @param parent
	 * @see org.kuppe.graphs.tarjan.GraphNode#setParent(org.kuppe.graphs.tarjan.GraphNode)
	 */
	public void setParent(GraphNode parent) {
		wrapped.setParent(parent);
	}

	/**
	 * @param other
	 * @return
	 * @see org.kuppe.graphs.tarjan.GraphNode#isInSameTree(org.kuppe.graphs.tarjan.GraphNode)
	 */
	public boolean isInSameTree(GraphNode other) {
		return wrapped.isInSameTree(other);
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#getRoot()
	 */
	public TreeNode getRoot() {
		return wrapped.getRoot();
	}

	/**
	 * @param sccs
	 * @param graph
	 * @param graphNode
	 * @see org.kuppe.graphs.tarjan.GraphNode#contract(java.util.Map, org.kuppe.graphs.tarjan.Graph, org.kuppe.graphs.tarjan.GraphNode)
	 */
	public void contract(Map<GraphNode, Set<GraphNode>> sccs, Graph graph, GraphNode graphNode) {
		wrapped.contract(sccs, graph, graphNode);
	}

	/**
	 * @param aTreeNode
	 * @return
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#isRootTo(org.kuppe.graphs.tarjan.TreeNode)
	 */
	public boolean isRootTo(TreeNode aTreeNode) {
		return wrapped.isRootTo(aTreeNode);
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#getParent()
	 */
	public TreeNode getParent() {
		return wrapped.getParent();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return wrapped.equals(obj);
	}

	/**
	 * @param newParent
	 * @param excludes
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#reLinkChildren(org.kuppe.graphs.tarjan.TreeNode, java.util.Set)
	 */
	public void reLinkChildren(TreeNode newParent, Set<? extends TreeNode> excludes) {
		wrapped.reLinkChildren(newParent, excludes);
	}

	/**
	 * 
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#cut()
	 */
	public void cut() {
		wrapped.cut();
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#getChildren()
	 */
	public Set<TreeNode> getChildren() {
		return wrapped.getChildren();
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#iterator()
	 */
	public Iterator<NaiveTreeNode> iterator() {
		return wrapped.iterator();
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.GraphNode#checkSCC()
	 */
	public boolean checkSCC() {
		return wrapped.checkSCC();
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.GraphNode#getId()
	 */
	public int getId() {
		return wrapped.getId();
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.GraphNode#tryLock()
	 */
	public boolean tryLock() {
		return wrapped.tryLock();
	}

	/**
	 * @return
	 * @see org.kuppe.graphs.tarjan.NaiveTreeNode#hasChildren()
	 */
	public boolean hasChildren() {
		return wrapped.hasChildren();
	}

	/**
	 * @param wait
	 * @param unit
	 * @return
	 * @see org.kuppe.graphs.tarjan.GraphNode#tryLock(long, java.util.concurrent.TimeUnit)
	 */
	public boolean tryLock(long wait, TimeUnit unit) {
		return wrapped.tryLock(wait, unit);
	}

	/**
	 * 
	 * @see org.kuppe.graphs.tarjan.GraphNode#unlock()
	 */
	public void unlock() {
		wrapped.unlock();
	}
}
