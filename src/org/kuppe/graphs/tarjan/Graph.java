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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

/**
 * Abstraction of TLC's NodePtrTable.
 */
public class Graph {

	public static final int NO_ARC = -1;

	private final Map<Integer, GraphNode> nodePtrTable;
	private final String name;

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
	
	/**
	 * @return The initial nodes?!
	 */
	public List<GraphNode> getStartNodes() {
		final List<GraphNode> start = new ArrayList<GraphNode>(this.nodePtrTable.size());
		for (GraphNode graphNode : nodePtrTable.values()) {
			start.add(graphNode);
		}
		return start;
	}

	public GraphNode get(final int id) {
		return this.nodePtrTable.get(id);
	}

	/* (outgoing) arcs */

	public void removeTraversedArc(GraphNode node, int arcId) {
		assert this.nodePtrTable.containsKey(node.getId());
		GraphNode graphNode = this.nodePtrTable.get(node.getId());
		graphNode.removeArc((Integer)arcId); // Explicitly cast to Integer to remove the element arcId and not the element at position arcId;
	}

	public int getUntraversedArc(GraphNode node) {
		final GraphNode record = this.nodePtrTable.get(node.getId());
		// 'node' has been contracted already. Thus return no untraversed arcs.
		if (isContracted(record, node)) {
			return NO_ARC;
		}
		if (!record.hasArcs()) {
			return NO_ARC;
		}
		return record.getArc();
	}

	public boolean hasUntraversedArc(GraphNode node) {
		final GraphNode graphNode = this.nodePtrTable.get(node.getId());
		// If this node has been replaced by one into which it was contracted,
		// there will obviously be arcs in the set. Thus, check if it's indeed
		// replaced.
		if (isContracted(graphNode, node)) {
			return false;
		}
		return graphNode.hasArcs();
	}

	private boolean isContracted(GraphNode graphNode, GraphNode node) {
		int id = graphNode.getId();
		if (id != node.getId()) {
			assert node.is(Visited.POST);
			return true;
		}
		return false;
	}
	
	/* contraction */

	public void contract(final GraphNode parent, final GraphNode child) {
		final GraphNode dstRecord = this.nodePtrTable.get(parent.getId());
		assert dstRecord == parent;
		assert dstRecord != null;
		
		// Globally Replace src with dst
		final GraphNode replaced = this.nodePtrTable.replace(child.getId(), dstRecord);
		assert replaced != dstRecord;
		
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
			dstRecord.addArcs(replaced.getArcs());
			replaced.clearArcs();
		}
		
		assert this.nodePtrTable.get(child.getId()) == parent;
	}
	
	/* Graph Locking */

	public boolean tryLock(GraphNode node) {
		return node.tryLock();
	}
	
	public void unlock(GraphNode node) {
		node.unlock();
	}

	/* Link cut tree locking */
	
	public GraphNode tryLockTrees(GraphNode w, GraphNode v) {
		if (!w.tryLock()) {
			return null;
		}
		// traverse w all the way up to its root
		final List<GraphNode> lockedNodes = new ArrayList<>(); 
		GraphNode parent = (GraphNode) w.getParent();
		while (parent != null) {
			// The more locks we've managed to acquire, the longer we are
			// willing to wait for remaining locks to become avilable.
			if (!parent.tryLock(lockedNodes.size() + 1L, TimeUnit.NANOSECONDS)) {
				// Unlock what's locked so far
				unlockPartial(w, lockedNodes);
				return null;
			}
			assert parent.isNot(Visited.POST);
			lockedNodes.add(parent);
			if (parent.getParent() == null) {
				return parent;
			}
			parent = (GraphNode) parent.getParent();
		}
		return w;
	}

	private void unlockPartial(GraphNode node, List<GraphNode> ancestors) {
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			GraphNode graphNode = ancestors.get(i);
			unlock(graphNode);
		}
		unlock(node);
	}

	public void unlockTrees(GraphNode w, GraphNode v) {
		// do one walk from w to its root to collect the nodes...
		final List<GraphNode> ancestors = new ArrayList<>(); 
		GraphNode parent = (GraphNode) w.getParent();
		while (parent != null) {
			ancestors.add(parent);
			parent = (GraphNode) parent.getParent();
		}
		
		//... and then release the locks in reverse order
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			GraphNode graphNode = ancestors.get(i);
			unlock(graphNode);
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
}
