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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

/**
 * Abstraction of TLC's NodePtrTable.
 */
public class Graph {

	public static final int NO_ARC = -1;

	private class Record {
		private final GraphNode node;
		private final List<Integer> arcs;
//		private final Lock lock;
		
		private Record(GraphNode node, List<Integer> arcs, Lock nodeLock) {
			assert node != null && arcs != null /*&& nodeLock != null*/;
			this.node = node;
			this.arcs = arcs;
//			this.lock = nodeLock;
		}
		
//		
//		/**
//		 * @return the lock
//		 */
//		public Lock getLock() {
//			return lock;
//		}
//
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Record [node=" + node + ", arcs=" + arcs + "]";
		}
	}
	
	private final Map<Integer, Record> nodePtrTable;
	
	private final Set<Record> allReplaced = new HashSet<>();

//	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	public Graph() {
		this.nodePtrTable = new HashMap<Integer, Record>();
	}
	
	/**
	 * @return The initial nodes?!
	 */
	public Collection<GraphNode> getStartNodes() {
		final List<GraphNode> start = new ArrayList<GraphNode>(this.nodePtrTable.size());
		for (Record graphNode : nodePtrTable.values()) {
			start.add(graphNode.node);
		}
		return start;
	}
	

	public void addArc(int nodeId, int arcId) {
		assert this.nodePtrTable.containsKey(nodeId);
		Record record = this.nodePtrTable.get(nodeId);
		record.arcs.add(arcId);
	}

	// Convenience method for unit tests (see AbstractGraph#addNode)
	public void addNode(GraphNode node, Integer... successors) {
		assert !this.nodePtrTable.containsKey(node.getId());

		// Create the entry in the nodePtrTable
		final List<Integer> s = new LinkedList<Integer>();
		for (Integer integer : successors) {
			s.add(integer);
		}
		
		node.setGraph(this);
		
		Record r = new Record(node, s, /*new ReentrantLock()*/ null);
		this.nodePtrTable.put(node.getId(), r);
	}

	public GraphNode get(int id) {
		Record record = this.nodePtrTable.get(id);
		if (record == null) {
			record = new Record(new GraphNode(id, this), new LinkedList<Integer>(), /*new ReentrantLock()*/ null);
			this.nodePtrTable.put(id, record);
		}
		return record.node;
	}
	
	public boolean checkPostCondition(int totalNodes) {
		// All arcs of all nodes have to be traversed, all nodes have to be
		// post-visited.
		if (totalNodes != new HashSet<Record>(this.nodePtrTable.values()).size() + this.allReplaced.size()) {
			return false;
		}
		for (Record record : this.nodePtrTable.values()) {
			if (record.node.isNot(Visited.POST)) {
				return false;
			}
			Collection<Integer> arcs = record.arcs;
			if (!arcs.isEmpty()) {
				return false;
			}
		}
		for (Record record : this.allReplaced) {
			if (record.node.isNot(Visited.POST)) {
				return false;
			}
			Collection<Integer> arcs = record.arcs;
			if (!arcs.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/* (outgoing) arcs */

	public int getUntraversedArc(GraphNode node) {
		final Record record = this.nodePtrTable.get(node.getId());
		// 'node' has been contracted already. Thus return no untraversed arcs.
		if (isContracted(record, node)) {
			return NO_ARC;
		}
		if (record.arcs.isEmpty()) {
			return NO_ARC;
		}
		return record.arcs.remove(0);
	}

	public boolean hasUntraversedArc(GraphNode node) {
		final Record record = this.nodePtrTable.get(node.getId());
		// If this node has been replaced by one into which it was contracted,
		// there will obviously be arcs in the set. Thus, check if it's indeed
		// replaced.
		if (isContracted(record, node)) {
			return false;
		}
		return !record.arcs.isEmpty();
	}

	private boolean isContracted(Record record, GraphNode node) {
		int id = record.node.getId();
		if (id != node.getId()) {
			assert node.is(Visited.POST);
			return true;
		}
		return false;
	}

	public Collection<Integer> getArcs(GraphNode node) {
		Record record = this.nodePtrTable.get(node.getId());
		return record.arcs;
	}
	
	public Collection<Integer> getUntraversedArcs(GraphNode node) {
		final Record record = this.nodePtrTable.get(node.getId());
		// 'node' has been contracted already. Thus return no untraversed arcs.
		if (record.node.getId() != node.getId()) {
			return new ArrayList<Integer>();
		}
		return new HashSet<>(record.arcs);
	}
	
	/* contraction */

	public void contract(final GraphNode parent, final GraphNode child) {
		final Record dstRecord = this.nodePtrTable.get(parent.getId());
		assert dstRecord.node == parent;
		assert dstRecord != null;
		
		// Globally Replace src with dst
		final Record replaced = this.nodePtrTable.replace(child.getId(), dstRecord);
		assert replaced != dstRecord;
		
		allReplaced.add(replaced);
		
		// add all outgoing arcs to dstRecord
		dstRecord.arcs.addAll(replaced.arcs);
		replaced.arcs.clear();
		
		// Replace lock of dst with lock of src
//		final Lock lock = replaced.getLock();
//		assert lock != null;
//		
//		// lock the lock first to get its monitor...
//		logger.fine(() -> String.format("Trying unlock of node (%s) due to contraction.", src));
//		lock.lock();
//		
//		// ...now we have the monitor and are free to unlock the lock
//		lock.unlock();
//		logger.fine(() -> String.format("Unlocked node (%s) due to contraction.", src));
		
		
		//TODO Probably have to unlock w's tree too
		
		assert this.nodePtrTable.get(child.getId()).node == parent;
	}
	
	/* Graph Locking */

	public boolean tryLock(GraphNode node) {
//		assert nodePtrTable.get(node.getId()) != null;
//		if (nodePtrTable.get(node.getId()).getLock().tryLock()) {
//			logger.fine(() -> String.format("%s: Locked node (%s)", node.getId(), node));
			return true;
//		} else {
//			logger.fine(() -> String.format("%s: Failed to acquire lock on node (%s)", node.getId(), node));
//			return false;
//		}
	}
	
	public void unlock(GraphNode node) {
//		assert nodePtrTable.get(node.getId()) != null;
//		final Lock nodeLock = nodePtrTable.get(node.getId()).getLock();
//		nodeLock.unlock();
//		logger.fine(() -> String.format("%s: Unlocked node %s", node.getId(), node));
	}

	/* Link cut tree locking */
	
	public boolean tryLockTrees(GraphNode w, GraphNode v) {
//		assert nodePtrTable.get(w.getId()) != null;
//		if (nodePtrTable.get(w.getId()).lock.tryLock()) {
//			logger.fine(() -> String.format("%s: Locked v (%s) and w (%s)", v.getId(), v, w));
//			// Acquired w, lets try to lock both trees
//			//TODO
			return true;
//		} else {
//			logger.fine(() -> String.format("%s: Locked v (%s), failed acquire w (%s)", v.getId(), v, w));
//			return false;
//		}
	}

	public void unlockTrees(GraphNode w, GraphNode v) {
//		assert nodePtrTable.get(w.getId()) != null;
//		//TODO
//		nodePtrTable.get(w.getId()).getLock().unlock();
//		logger.fine(() -> String.format("%s: Unlocked tree node %s", w.getId(), w));
	}
}
