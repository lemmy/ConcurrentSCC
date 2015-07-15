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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

/**
 * Abstraction of TLC's NodePtrTable.
 */
public class Graph {

	public static final int NO_ARC = -1;

	private final Map<Integer, GraphNode> nodePtrTable;
	//TODO Remove replaced in "production". It's here to strengthen the post condition.
	private final Set<GraphNode> replaced = new HashSet<>();
	private final String name;
	private final Deque<GraphNode> initNodes = new ArrayDeque<>();

	public Graph() {
		this(null);
	}
	
	public Graph(final String name) {
		this.name = name;
		this.nodePtrTable = new ConcurrentHashMap<Integer, GraphNode>();
	}
	
	public String getName() {
		return name;
	}
	
	/* nodes */
	
	public Iterator<GraphNode> iterator() {
		if (initNodes.isEmpty()) {
			return this.nodePtrTable.values().iterator();
		}
		return initNodes.iterator();
	}

	public GraphNode get(final int id) {
		return this.nodePtrTable.get(id);
	}
	
	/* contraction */

	public void contract(final GraphNode into, final GraphNode child) {
		// Globally Replace src with dst
		final GraphNode replaced = this.nodePtrTable.replace(child.getId(), into);
		assert replaced != into;
		this.replaced.add(replaced);
		
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
	
	/* Graph Locking */

	public boolean tryLock(GraphNode node) {
		return node.tryLock();
	}
	
	public void unlock(GraphNode node) {
		node.unlock();
	}

	/* Link cut tree locking */
	
	public GraphNode tryLockTrees(final GraphNode w) {
		if (!w.tryLock()) {
			// Nothing is locked
			return null;
		}
		
		if (w.isRoot()) {
			return w;
		}
		
		// w is locked from here on and not a root
		
		// traverse w all the way up to its root
		GraphNode parent = w.getParent();
		while (parent != null) {
			if (parent.is(Visited.POST)) {
				parent.unlock();
				w.unlock();
				return null;
			}
			if (parent.isRoot()) {
				if (parent.isRootTo(w)) {
					return parent;
				} else {
					parent.unlock();
					w.unlock();
					return null;
				}
			}
			// getParent acquires parent's lock
			GraphNode oldparent = parent;
			parent = (GraphNode) parent.getParent();
			oldparent.unlock();
		}
		w.unlock();
		return null;
	}

	public void unlockTrees(GraphNode w, GraphNode wRoot) {
		// do not unlock w twice if w's root is w itself.
		if (w != wRoot) {
			unlock(wRoot);
		}
		unlock(w);
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
		for (GraphNode graphNode : this.replaced) {
			if (graphNode.isNot(Visited.POST)) {
				return false;
			}
			if (graphNode.hasArcs()) {	
				return false;
			}
			if (graphNode.hasChildren()) {
				return false;
			}
			if (graphNode.isRoot()) {
				// Contracted nodes are part of the SCC linked list and thus
				// have a parent.
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
}
