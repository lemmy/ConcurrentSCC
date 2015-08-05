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

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

/**
 * Abstraction of TLC's NodePtrTable.
 */
@SuppressWarnings("serial")
public class Graph implements Serializable {

	public static final int NO_ARC = -1;
	
	private final static Histogram findroot = ConcurrentFastSCC.metrics.histogram(MetricRegistry.name("findroot-succ"));
	private final static Histogram postFail = ConcurrentFastSCC.metrics.histogram(MetricRegistry.name("findroot-post-fail"));
	private final static Histogram lockFail = ConcurrentFastSCC.metrics.histogram(MetricRegistry.name("findroot-lock-fail"));
	
	private final Map<Integer, GraphNode> nodePtrTable;
	private final String name;
	private final Deque<GraphNode> initNodes = new ArrayDeque<>();

	public Graph() {
		this(null);
	}
	
	public Graph(final String name) {
		this.name = name;
		this.nodePtrTable = new HashMap<Integer, GraphNode>();
	}
	
	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}
	
	/* nodes */
	
	public Iterator<GraphNode> iterator() {
		if (initNodes.isEmpty()) {
			if (this.nodePtrTable.isEmpty()) {
				return this.nodePtrTable.values().iterator();
			}
			return new PseudoRandomIterator<GraphNode>(this.nodePtrTable);
		}
		return initNodes.iterator();
	}
	
	private static class PseudoRandomIterator<T> implements Iterator<GraphNode> {

		private final Map<Integer, GraphNode> tbl;
		private final Integer[] indices;
		
		private int idx = 0;

		public PseudoRandomIterator(final Map<Integer, GraphNode> nodePtrTable) {
			tbl = nodePtrTable;

			int size = nodePtrTable.size();
			final List<Integer> tmp = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				tmp.add(i);
			}
			Collections.shuffle(tmp);
			indices = tmp.toArray(new Integer[tmp.size()]);
		}

		@Override
		public boolean hasNext() {
			return idx < this.tbl.size();
		}

		@Override
		public GraphNode next() {
			return this.tbl.get(indices[idx++]);
		}
	}
	
	private static class PartitioningIterator<T> implements Iterator<GraphNode> {

		private final Map<Integer, GraphNode> table;
		private final int[] partitions;
		private int index = 0;
		private int read = 0;
		
		public PartitioningIterator(Map<Integer, GraphNode> aTable, int partitions) {
			this.table = aTable;
			
			final int length = this.table.size() / partitions;

			this.partitions = new int[partitions];
			for (int i = 0; i < this.partitions.length - 1; i++) {
				this.partitions[i] = i * length;
			}
			
			// remainder goes to last partition
			this.partitions[partitions - 1] = this.table.size() - length;
		}
		
		@Override
		public boolean hasNext() {
			return read < this.table.size();
		}

		@Override
		public GraphNode next() {
			// Used up an element
			read++;
			// lookup the next element in the current partition.
			final int elem = this.partitions[index++]++;
			// switch to next partition		
			index = index % this.partitions.length;
			// Return the element
			return this.table.get(elem);
		}
	}

	public GraphNode get(final int id) {
		return this.nodePtrTable.get(id);
	}

	/* contraction */

	public void contract(final GraphNode into, final GraphNode child) {
		// Globally Replace src with dst
		final GraphNode replaced = this.nodePtrTable.replace(child.getId(), into);
		assert replaced != into;
		
		// add all outgoing arcs to dstRecord
		// TODO LinkedList might not be the ideal data structure here:
		// - Java's implementation doesn't do an O(1) concat but copies the
		// second list
		// - List does not discard duplicates and thus causes SCCWorker to
		// explore arcs multiple times. Since lock acquisition is expensive,
		// it's probably cheaper to discard duplicate arcs here.
		// - Explored arcs are also removed from the collection of arcs. That
		// also adds to re-exploration of arcs even if duplicated of un-explored
		// arcs would be discarded.
		// => SCCWorker checks PRIOR to lock acquisition, if the arc's endpoint
		// node is POST
		if (replaced.hasArcs()) {
			into.addArcs(replaced.getArcs());
			replaced.clearArcs();
		}
		
		assert this.nodePtrTable.get(child.getId()) == into;
	}

	/* Tree locking */
	
	public GraphNode tryLockTrees(final GraphNode w) {
		if (!w.tryLock()) {
			lockFail.update(0);
			// Nothing is locked
			return null;
		}
		
		if (w.isRoot()) {
			findroot.update(1);
			return w;
		}
		
		// w is locked from here on and not a root
		
		// traverse w all the way up to its root
		int length = 1;
		GraphNode parent = w.getParent();
		while (parent != null) {
			length++;
			if (parent.is(Visited.POST)) {
				postFail.update(length);
				parent.readUnlock();
				w.unlock();
				return null;
			}
			if (parent.isRoot()) {
				findroot.update(length);
				return parent;
			}
			// getParent acquires parent's lock
			GraphNode oldParent = parent;
			parent = (GraphNode) parent.getParent();
			oldParent.readUnlock();
		}
		lockFail.update(length);
		w.unlock();
		return null;
	}

	public void unlockTrees(GraphNode w, GraphNode wRoot) {
		// do not unlock w twice if w's root is w itself.
		if (w != wRoot) {
			wRoot.readUnlock();
		}
		w.unlock();
	}

	/* aux methods for testing */ 
	
	void addArc(int nodeId, int arcId) {
		assert this.nodePtrTable.containsKey(nodeId);
		GraphNode graphNode = this.nodePtrTable.get(nodeId);
		graphNode.getArcs().add(arcId);
	}
	
	Collection<Integer> getUntraversedArcs(GraphNode node) {
		final GraphNode graphNode = this.nodePtrTable.get(node.getId());
		// 'node' has been contracted already. Thus return no untraversed arcs.
		if (graphNode.getId() != node.getId()) {
			return new ArrayList<Integer>();
		}
		return new HashSet<>(graphNode.getArcs());
	}

	Collection<Integer> getArcs(GraphNode node) {
		return this.nodePtrTable.get(node.getId()).getArcs();
	}
	
	boolean checkPostCondition() {
		// All arcs of all nodes have to be traversed, all nodes have to be
		// post-visited.
		final Set<GraphNode> graphNodes = new HashSet<>(this.nodePtrTable.values());
		for (GraphNode graphNode : graphNodes) {
			if (graphNode.isNot(Visited.POST)) {
				return false;
			}
			if (graphNode.hasArcs()) {
				return false;
			}
			if (graphNode.hasChildren()) {
				return false;
			}
			if (!graphNode.isRoot()) {
				return false;
			}
			if (graphNode.isLocked()) {
				return false;
			}
		}
		return true;
	}
	
	boolean hasNode(final int id) {
		return this.nodePtrTable.containsKey(id);
	}
	
	// Convenience method for unit tests (see AbstractGraph#addNode)
	void addNode(GraphNode node, Integer... successors) {
		assert !this.nodePtrTable.containsKey(node.getId());

		// Create the entry in the nodePtrTable
		final List<Integer> s = new LinkedList<Integer>();
		for (Integer integer : successors) {
			s.add(integer);
		}
		node.setArcs(s);
		
		this.nodePtrTable.put(node.getId(), node);
	}
	
	void setInit(final int nodeId) {
		initNodes.add(get(nodeId));
	}
	
	public synchronized GraphNode put(final int id, final GraphNode node) {
		return this.nodePtrTable.put(id, node);
	}

	public int size() {
		return this.nodePtrTable.size();
	}
}
