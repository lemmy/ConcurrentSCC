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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Abstraction of TLC's NodePtrTable.
 */
public class Graph {
	
	private class Record {
		private final GraphNode node;
		private final Collection<Arc> arcs;
		private final Lock lock;
		
		private Record(GraphNode node, Collection<Arc> arcs, Lock nodeLock) {
			assert node != null && arcs != null && nodeLock != null;
			this.node = node;
			this.arcs = arcs;
			this.lock = nodeLock;
		}
		
		/**
		 * @return the lock
		 */
		public Lock getLock() {
			return lock;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Record [node=" + node + ", arcs=" + arcs + "]";
		}
	}
	
	private final Map<Integer, Record> nodePtrTable;

//	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	public Graph() {
		this.nodePtrTable = new HashMap<Integer, Record>();
	}
	
	/**
	 * @return The initial nodes?!
	 */
	public Collection<GraphNode> getStartNodes() {
		Set<GraphNode> start = new HashSet<GraphNode>(this.nodePtrTable.size());
		nodePtrTable.values().forEach((value) -> {start.add(value.node);});
		return start;
	}

	// Convenience method for unit tests (see AbstractGraph#addNode)
	public void addNode(GraphNode node, Integer... successors) {
		assert !this.nodePtrTable.containsKey(node);

		// Create the entry in the nodePtrTable
		final List<Arc> s = new ArrayList<Arc>();
		for (Integer integer : successors) {
			s.add(new Arc(integer));
		}
		
		Record r = new Record(node, s, new ReentrantLock());
		this.nodePtrTable.put(node.getId(), r);
	}

	public GraphNode get(int id) {
		return this.nodePtrTable.get(id).node;
	}

	/* (outgoing) arcs */

	public Arc getUntraversedArc(GraphNode node) {
		Iterator<Arc> arcs = this.nodePtrTable.get(node.getId()).arcs.iterator();
		while (arcs.hasNext()) {
			Arc next = arcs.next();
			if (!next.isTraversed()) {
				return next;
			}
		}
		return null;
	}

	public boolean hasUntraversedArc(GraphNode v) {
		return getUntraversedArc(v) != null;
	}

	public Collection<Arc> getArcs(GraphNode node) {
		return this.nodePtrTable.get(node.getId()).arcs;
	}
	
	public Collection<Arc> getUntraversedArcs(GraphNode node) {
		final Collection<Arc> arcs = this.nodePtrTable.get(node.getId()).arcs;
		return arcs.stream().filter((arc) -> !arc.isTraversed()).collect(Collectors.toSet());
	}
	
	/* contraction */

	public void contract(GraphNode dst, GraphNode src) {
		final Record dstRecord = this.nodePtrTable.get(dst.getId());
		assert dstRecord.node == dst;
		assert dstRecord != null;
		
		// Globally Replace src with dst
		final Record replaced = this.nodePtrTable.replace(src.getId(), dstRecord);
		assert replaced.node == src;
		assert replaced != dstRecord;
		
		// add all outgoing arcs to dstRecord
		dstRecord.arcs.addAll(replaced.arcs);
		
		// Replace lock of dst with lock of src
		final Lock lock = replaced.getLock();
		assert lock != null;
		
		// lock the lock first to get its monitor...
		System.out.println(String.format("Trying unlock of node (%s) due to contraction.", src));
		lock.lock();
		
		// ...now we have the monitor and are free to unlock the lock
		lock.unlock();
		System.out.println(String.format("Unlocked node (%s) due to contraction.", src));
		
		
		//TODO Probably have to unlock w's tree too
	}
	
	/* Graph Locking */

	public boolean tryLock(GraphNode node) {
		assert nodePtrTable.get(node.getId()) != null;
		if (nodePtrTable.get(node.getId()).getLock().tryLock()) {
			System.out.println(String.format("%s: Locked node (%s)", node.getId(), node));
			return true;
		} else {
			System.out.println(String.format("%s: Failed to acquire lock on node (%s)", node.getId(), node));
			return false;
		}
	}
	
	public void unlock(GraphNode node) {
		assert nodePtrTable.get(node.getId()) != null;
		final Lock nodeLock = nodePtrTable.get(node.getId()).getLock();
		nodeLock.unlock();
		System.out.println(String.format("%s: Unlocked node %s", node.getId(), node));
	}

	/* Link cut tree locking */
	
	public boolean tryLockTrees(GraphNode w, GraphNode v) {
		assert nodePtrTable.get(w.getId()) != null;
		if (nodePtrTable.get(w.getId()).lock.tryLock()) {
			System.out.println(String.format("%s: Locked v (%s) and w (%s)", v.getId(), v, w));
			// Acquired w, lets try to lock both trees
			//TODO
			return true;
		} else {
			System.out.println(String.format("%s: Locked v (%s), failed acquire w (%s)", v.getId(), v, w));
			return false;
		}
	}

	public void unlockTrees(GraphNode w, GraphNode v) {
		assert nodePtrTable.get(w.getId()) != null;
		//TODO
		nodePtrTable.get(w.getId()).getLock().unlock();
		System.out.println(String.format("%s: Unlocked tree node %s", w.getId(), w));
	}
}
