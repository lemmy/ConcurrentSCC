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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class GraphNode extends NaiveTreeNode {
	
	public static final AtomicLong AVERAGE_FIX_AMOUNT = new AtomicLong();
	public static final AtomicLong AVERAGE_FIX_CNT = new AtomicLong();

	public static final AtomicLong CONTRACTIONS = new AtomicLong();
	public static final AtomicLong CONTRACTION_LENGTH = new AtomicLong();

	private static final int SCC_NODE = -23;
	
	public enum Visited {
		// This also constitutes an order (see ordinal)
		UN, POST;
	};
	
	// package protected for unit tests only
	volatile Visited visited = Visited.UN;

	private List<Integer> arcs;

	public GraphNode(int id) {
		super(id);
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
		assert visited != Visited.POST || !this.hasArcs();

		this.visited = visited;
	}

	public void setArcs(List<Integer> arcs) {
		assert isNot(Visited.POST);
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

	public boolean removeArc(Integer arcId) {
		// has to be Integer (not int) to remove the element and not element at
		// position.
		return this.arcs.remove(arcId);
	}

	public void addArcs(List<Integer> other) {
		assert isNot(Visited.POST);
		this.arcs.addAll(other);
	}

	public void clearArcs() {
		this.arcs.clear();
		this.arcs = null;
	}

	public int getArc() {
		assert isNot(Visited.POST);
		if (this.arcs.isEmpty()) {
			return Graph.NO_ARC;
		}
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

	public void setParent(final GraphNode parent) {
		assert this.isNot(Visited.POST);
		link(parent);
	}
	
	/**
	 * Locks parent if there is one. The parent might not be a root anymore or
	 * could have transitioned into POST once the lock is acquired.
	 */
	public GraphNode getParent() {
		final GraphNode parent = (GraphNode) super.getParent();
		if (parent != null) {
			if (parent.tryLock()) {
				return parent;
			}
		}
		return null;
	}

	public boolean isInSameTree(final GraphNode other) {
		return this.getRoot() == other.getRoot();
	}
	
	public Set<GraphNode> getSCC() {
		assert is(Visited.POST);
		assert this.getId() == SCC_NODE;
		final Set<GraphNode> result = new HashSet<>();
		result.add((GraphNode) this.parent);
		if (!hasChildren()) {
			return result;
		}
		GraphNode node = (GraphNode) leftChild;
		while (node != rightChild) {
			if (node.id == SCC_NODE) {
				result.addAll(node.getSCC());
			} else {
				result.add(node);
			}
			node = (GraphNode) node.rightSibling;
		}
		result.add((GraphNode) rightChild);
		return result;
	}

	public void contract(final Map<GraphNode, GraphNode> sccs, final Graph graph, final GraphNode graphNode) {
		CONTRACTIONS.incrementAndGet();
		
		assert isNot(Visited.POST);

		// We have to be a root in the tree...
		assert this.isRoot();
		// ...and the other has to be in our tree
		assert this.isRootTo(graphNode);

		// Get the subset SCCs (if any) which has been contracted into this
		// before.
		GraphNode head = sccs.get(this);
		if (head == null) {
			// No previous scc for this node, create a new graphNode as new head
			head = new GraphNode(SCC_NODE);
			head.visited = Visited.POST;
			head.parent = this;
			sccs.put(this, head);
		}
		
		// Traverse GraphNode's tree up to the root which is us/this.
		int length = 0;
		GraphNode parent = graphNode;
		while (parent != this) {
			length++;
			// Acquire parent's lock so that I cannot be change concurrently by
			// SCCWorker either as v or w. Since we know that we own the root
			// of w (which is v), we can wait for parent. Other threads trying
			// to acquire w's root (v) will eventually give up.
			//TODO Change lock to be fair
			parent.lock.lock();

			assert parent.isNot(Visited.POST);
			// This should be the only place where visited is accessed directly
			// (except to its setter). It is done, to skip the pre-condition,
			// that all of parents outgoing arcs are traversed. Here we merge
			// all unprocessed outgoing arcs into this node.
			parent.visited = Visited.POST;

			// Logically replace parent with this GraphNode in the Graph.
			graph.contract(this, parent);
			// parent's arcs should have been contracted into this now.
			assert !parent.hasArcs();
			
			// Before unlink/cut, remember parent's parent. Don't use getParent
			// though, which might return null if lock acquisition fails.
			// However, we don't need to acquire parent's lock, because we know
			// that we hold the root lock. The other thread holding parent's
			// lock will abort when it gets to the root and fails to acquire its
			// lock.
			final GraphNode parentsParent = (GraphNode) parent.parent;

			// Link parent's children to us.
			parent.reLinkChildren(this);
			assert !parent.hasChildren();
			
			// Unlink the parent from the path so that it can be gc'ed
			parent.cut();
			assert parent.isRoot();
			
			// Merge the other's subset SCC (if any) into this new one and
			// remove it from the set of sccs.
			mergeSubSetSCC(sccs, graph, head, parent);
			
			// parent is potentially locked more than once (e.g. if it is w in
			// SCCWorker). Thus, release all locks.
			parent.unlock();
			
			// Continue with parent's parent.
			parent = parentsParent;
		}
		CONTRACTION_LENGTH.addAndGet(length);
		
		// We remain a root in the tree.
		assert this.isRoot();
		// Must not be POST-visited now
		assert this.isNot(Visited.POST);
	}

	private void mergeSubSetSCC(final Map<GraphNode, GraphNode> sccs, final Graph graph, GraphNode newHead, final GraphNode node) {
		// TODO Use (custom) LinkedLists instead of Set that can be merged in
		// O(1). Would also preserve order of nodes and thus the actual path of
		// the SCC. Reuse parent or sibling pointers for space efficiency reasons.
		final GraphNode nodesHead = sccs.remove(node);
		if (nodesHead != null) {
			assert nodesHead.getId() == SCC_NODE;
			fixDanglingMappings(graph, nodesHead);
			nodesHead.reLinkChildren(newHead);
		}
		node.link(newHead);
	}

	private void fixDanglingMappings(final Graph graph, final GraphNode head) {
		//TODO move into Graph and do in parallel unless Graph's concurrent map becomes the bottleneck.
		// Correct all 'id to node' mappings for the previous contracted
		// nodes. Otherwise, if an arc is later explored
		// going to one node in parentsSubset "t", it will be skipped as
		// "t" is post-visited. It has to be pre-visited though, which
		// is this' visited state after contraction.
		Iterator<NaiveTreeNode> iterator = head.iterator();
		while(iterator.hasNext()) {
			GraphNode child = (GraphNode) iterator.next();
			assert child.is(Visited.POST);
			if (child.getId() != SCC_NODE) {
				graph.contract(this, child);
			}
			AVERAGE_FIX_AMOUNT.incrementAndGet();
		}
		AVERAGE_FIX_CNT.incrementAndGet();
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

	private final ReentrantLock lock = new ReentrantLock();

	public boolean tryLock() {
		return lock.tryLock();
	}

	public void unlock() {
		// Ideally it would use the stronger precondition that the calling
		// thread is the current thread. However, some unit tests don't use the
		// scheduler but call SCCWorker#call recursively. Thus, a lock is
		// acquired multiple times during recursion but completely unlocked upon
		// the first invocation. Subsequent unlocks would fail.
		int holdCount = lock.getHoldCount();
		for (int i = 0; i < holdCount; i++) {
			assert lock.isHeldByCurrentThread();
			lock.unlock();
		}
	}

	public boolean isLocked() {
		return this.lock.isLocked();
	}
}
