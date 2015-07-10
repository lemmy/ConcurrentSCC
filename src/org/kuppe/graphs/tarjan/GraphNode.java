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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class GraphNode extends NaiveTreeNode {

	public enum Visited {
		// This also constitutes an order (see ordinal)
		UN, POST;
	};
	
	private final Graph graph;
	
	// package protected for unit tests only
	volatile Visited visited = Visited.UN;

	private List<Integer> arcs;

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

	public void setArcs(List<Integer> arcs) {
		this.arcs = arcs;
	}
	
	public List<Integer> getArcs() {
		return this.arcs;
	}
	
	public boolean hasArcs() {
		if (arcs == null) {
			return false;
		}
		return !this.arcs.isEmpty();
	}

	public void removeArc(Integer arcId) {
		// has to be Integer (not int) to remove the element and not element at
		// position.
		this.arcs.remove(arcId);
	}

	public void addArcs(List<Integer> other) {
		this.arcs.addAll(other);
	}

	public void clearArcs() {
		this.arcs.clear();
		this.arcs = null;
	}

	public int getArc() {
		return this.arcs.get(0);
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
	
	public GraphNode getParent() {
		return null;
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

			// Now that parent is unlinked/cut from its tree, null the lock so
			// it can be garbage collected. It can't be done prior to cutting though,
			// because workers trying 
			parent.lock = null;

			// Continue with parent's parent.
			parent = parentsParent;
		}

		// We remain a root in the tree.
		assert this.isRoot();
		// Must not be POST-visited now
		assert this.isNot(Visited.POST);
	}

	private void mergeSubSetSCC(final Map<GraphNode, Set<GraphNode>> sccs, final Graph graph, Set<GraphNode> scc, final GraphNode parent) {
		// TODO Use (custom) LinkedLists instead of Set that can be merged in
		// O(1). Would also preserve order of nodes and thus the actual path of
		// the SCC. Reuse parent or sibling pointers for space efficiency reasons.
		final Set<GraphNode> parentsSubset = sccs.remove(parent);
		if (parentsSubset != null) {
			fixDanglingMappings(graph, parent, parentsSubset);
			scc.addAll(parentsSubset);
		} else {
			scc.add(parent);
		}
	}

	private void fixDanglingMappings(final Graph graph, final GraphNode parent, final Set<GraphNode> parentsSubset) {
		//TODO move into Graph and do in parallel unless Graph's concurrent map becomes the bottleneck.
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
	
	/* Locking */
	
	private ReentrantLock lock = new ReentrantLock();

	public boolean tryLock() {
		if (lock == null) {
			// There might still be a scheduled worker in the system for this
			// GraphNode that wants to lock this node in order to determine its
			// visited state.
			assert is(Visited.POST);
			return true;
		}
		return lock.tryLock();
	}

	public boolean tryLock(long wait, TimeUnit unit) {
		if (lock == null) {
			// A worker #1 is trying to lock a tree from w to its root r.
			// Concurrently, an ancestor a of w is being contracted by another
			// worker #2 into r. Worker #1 reads the pointer from w to a but the
			// lock is replaced by worker #2 with null before #1 acquires the
			// lock.
			assert is(Visited.POST);
			return true;
		}
		try {
			return lock.tryLock(wait, unit);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void unlock() {
		if (lock == null) {
			// There might still be workers in the system that need to unlock
			// this node after determining its visited state.
			assert is(Visited.POST);
		} else {
			int holdCount = lock.getHoldCount();
			for (int i = 0; i < holdCount; i++) {
				lock.unlock();
			}
		}
	}
}
