package org.kuppe.graphs.tarjan;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstraction of TLC's NodePtrTable.
 */
public class Graph {
	
	private static class Record {
		GraphNode node;
		Set<Arc> arcs;
	}
	
	private final Map<Integer, Record> nodePtrTable;

	// Have on lock per GraphNode. Later this has to change when the number of
	// GraphNode growth. Then, hash the GraphNode to a Lock.
	private final Map<GraphNode, Lock> lockTable;

//	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	public Graph() {
		this.nodePtrTable = new HashMap<Integer, Record>();
		this.lockTable = new HashMap<GraphNode, Lock>();
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
		assert !this.nodePtrTable.containsKey(node) && !this.lockTable.containsKey(node);

		// Create the entry in the nodePtrTable
		final Set<Arc> s = new HashSet<Arc>();
		for (Integer integer : successors) {
			s.add(new Arc(integer));
		}
		
		Record r = new Record();
		r.node = node;
		r.arcs = s;
		this.nodePtrTable.put(node.getId(), r);
		
		// Create the corresponding lock object
		this.lockTable.put(node, new ReentrantLock());
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

	public Set<Arc> getArcs(GraphNode node) {
		return this.nodePtrTable.get(node.getId()).arcs;
	}
	
	/* contraction */

	public void contract(GraphNode dst, GraphNode src) {
		final Record dstRecord = this.nodePtrTable.get(dst.getId());
		assert dstRecord != null;
		
		// Globally Replace src with dst
		final Record replaced = this.nodePtrTable.replace(src.getId(), dstRecord);
		assert replaced != dstRecord;
		
		// all all outgoing arcs to dstRecord
		dstRecord.arcs.addAll(replaced.arcs);
		
		// Replace lock of dst with lock of src
		final Lock lock = this.lockTable.replace(dst, this.lockTable.get(src));
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

	public boolean tryLock(SCCWorker sccWorker, GraphNode node) {
		if (lockTable.get(node).tryLock()) {
			System.out.println(String.format("%s: Locked node (%s)", sccWorker.getId(), node));
			return true;
		} else {
			System.out.println(String.format("%s: Failed to acquire lock on node (%s)", sccWorker.getId(), node));
			return false;
		}
	}
	
	public void unlock(SCCWorker sccWorker, GraphNode node) {
		lockTable.get(node).unlock();
		System.out.println(String.format("%s: Unlocked node %s", sccWorker.getId(), node));
	}

	/* Link cut tree locking */
	
	public boolean tryLockTrees(SCCWorker sccWorker, GraphNode w, GraphNode v) {
		if (lockTable.get(w).tryLock()) {
			System.out.println(String.format("%s: Locked v (%s) and w (%s)", sccWorker.getId(), v, w));
			// Acquired w, lets try to lock both trees
			//TODO
			return true;
		} else {
			System.out.println(String.format("%s: Locked v (%s), failed acquire w (%s)", sccWorker.getId(), v, w));
			return false;
		}
	}

	public void unlockTrees(SCCWorker sccWorker, GraphNode w, GraphNode v) {
		//TODO
		lockTable.get(w).unlock();
		System.out.println(String.format("%s: Unlocked tree node %s", sccWorker.getId(), w));
	}
}
