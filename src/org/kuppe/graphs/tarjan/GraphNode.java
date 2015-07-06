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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class GraphNode extends NaiveTreeNode {

	public enum Visited {
		// This also constitutes an order (see ordinal)
		UN, POST;
	};
	
	private final Graph graph;
	
	// package protected for unit tests only
	volatile Visited visited = Visited.UN;

	public GraphNode(int id, Graph graph) {
		super(id);
		this.graph = graph;
	}

	public boolean is(Visited v) {
		return visited == v;
	}
	
	public boolean isNot(Visited v) {
		return !is(v);
	}

	public void set(Visited visited) {
		// Only state changes from UN > PRE > POST are allowed
		assert this.visited.ordinal() <= visited.ordinal();
		
		// When a GraphNode transitions into post, all its arcs have to be
		// explored.
		assert visited != Visited.POST || !graph.hasUntraversedArc(this);

		this.visited = visited;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isRoot()) {
			return "GN [id=" + id
					+ ", visited=" + visited
					+ ", ROOT"
					+ "]";
		}
		return "GN [id=" + id
				+ ", visited=" + visited
				+ ", CHILD"
				+ "]";
	}

	public void setParent(final GraphNode parent) {
		assert this.isNot(Visited.POST);
		link(parent);
	}

	public boolean isInSameTree(final GraphNode other) {
		return this.getRoot() == other.getRoot();
	}

	public void contract(final Map<GraphNode, Set<GraphNode>> sccs, final Graph graph, final GraphNode graphNode) {
		// We have to be a root in the tree...
		assert this.isRoot();
		// ...and the other has to be in our tree
		assert this.isRootTo(graphNode);

		// Get the subset SCCs (if any) which has been contracted into this
		// before.
		Set<GraphNode> scc = sccs.get(this);
		if (scc == null) {
			// No previous scc for this node
			scc = new HashSet<GraphNode>(); // TODO We need a stack here for liveness checking!
			scc.add(this);
			sccs.put(this, scc);
		}
		
		// Traverse GraphNode's tree up to the root which is us/this.
		GraphNode parent = graphNode;
		while (parent != this) {
			// Merge the other's subset SCC (if any) into this new one and
			// remove it from the set of sccs.
			mergeSubSetSCC(sccs, graph, scc, parent);

			assert parent.isNot(Visited.POST);
			// This should be the only place where visited is accessed directly
			// (except to its setter). It is done, to skip the pre-condition,
			// that all of parents outgoing arcs are traversed. Here we merge
			// all unprocessed outgoing arcs into this node.
			parent.visited = Visited.POST;

			// Logically replace parent with this GraphNode in the Graph.
			graph.contract(this, parent);
			// parent's arcs should have been contracted into this now.
			assert !graph.hasUntraversedArc(parent);
			
			// Before unlink/cut, remember parent's parent
			final GraphNode parentsParent = (GraphNode) parent.getParent();

			// Unlink the parent from the path so that it can be gc'ed
			parent.cut();
			assert parent.isRoot();
			
			// Link parent's children to us except for the ones that are part of
			// the SCC. Those are done now and don't need to be re-linked.
			parent.reLinkChildren(this, scc);
			
			// Continue with parent's parent.
			parent = parentsParent;
		}

		// We remain a root in the tree.
		assert this.isRoot();
		// Must not be POST-visited now
		assert this.isNot(Visited.POST);
	}

	private void mergeSubSetSCC(final Map<GraphNode, Set<GraphNode>> sccs, final Graph graph, Set<GraphNode> scc, final GraphNode parent) {
		final Set<GraphNode> parentsSubset = sccs.remove(parent);
		if (parentsSubset != null) {
			// Correct all 'id to node' mappings for the previous contracted
			// nodes. Otherwise, if an arc is later explored
			// going to one node in parentsSubset "t", it will be skipped as
			// "t" is post-visited. It has to be pre-visited though, which
			// is this' visited state after contraction.
			for (GraphNode s : parentsSubset) {
				// s' mapping will be updated down below
				if (s != parent) {
					graph.contract(this, s);
				}
			}
			scc.addAll(parentsSubset);
		} else {
			scc.add(parent);
		}
	}

	public boolean checkSCC() {
		// TODO Check liveness here. However, this can also be done while the
		// nodes are contracted into the root. Best is though, if it's done
		// without holding any locks on the graph/tree nodes.
		return true;
	}

	public int getId() {
		return id;
	}

	/**
	 * Cuts off the direct tree children. 
	 */
	public Set<GraphNode> cutChildren() {
		// A subset of our children which are still unprocessed.
		final Set<GraphNode> unprocessedChildren = new HashSet<GraphNode>();

		final Set<TreeNode> directChildren = getChildren();
		for (TreeNode linkCutTreeNode : directChildren) {
			final GraphNode child = (GraphNode) linkCutTreeNode;
			if (child.isNot(Visited.POST)) {
				unprocessedChildren.add(child);
			}
			child.cut();
		}
		
		return unprocessedChildren;
	}

	private final ReentrantLock lock = new ReentrantLock();

	public boolean tryLock() {
		return lock.tryLock();
	}

	public void unlock() {
		int holdCount = lock.getHoldCount();
		for (int i = 0; i < holdCount; i++) {
			lock.unlock();
		}
	}
}
