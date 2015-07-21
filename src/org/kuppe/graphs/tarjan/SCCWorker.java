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
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;

public class SCCWorker implements Runnable {
	
	private static final Counter vLock = ConcurrentFastSCC.metrics.counter(MetricRegistry.name(SCCWorker.class, "v-lock-success"));
	private static final Counter vLockFail = ConcurrentFastSCC.metrics.counter(MetricRegistry.name(SCCWorker.class, "v-lock-failed"));

	private static final Counter wLock = ConcurrentFastSCC.metrics.counter(MetricRegistry.name(SCCWorker.class, "w-lock-success"));
	private static final Counter wLockFail = ConcurrentFastSCC.metrics.counter(MetricRegistry.name(SCCWorker.class, "w-lock-failed"));
	
	@SuppressWarnings("unused") // It's used because registered as a metric
	private static final LockRatioGauge vRatio = ConcurrentFastSCC.metrics.register(SCCWorker.class.getName() + ".v-lock-ratio", new LockRatioGauge(vLock, vLockFail));
	@SuppressWarnings("unused") // It's used because registered as a metric
	private static final LockRatioGauge wRatio = ConcurrentFastSCC.metrics.register(SCCWorker.class.getName() + ".w-lock-ratio", new LockRatioGauge(wLock, wLockFail));
	
	private static final Logger logger = Logger.getLogger("org.kuppe.graphs.tarjan");

	private final ExecutorService executor;
	private final Map<GraphNode, GraphNode> sccs;
	private final Graph graph;
	private GraphNode v;

	public SCCWorker(final ExecutorService executor, final Graph graph, Map<GraphNode, GraphNode> sccs,
			final GraphNode root) {
		this.executor = executor;
		this.graph = graph;
		this.sccs = sccs;
		this.v = root;
	}

	public void run() {
		try {
			// Skip POST-visited v (POST will never transition back to UN,
			// whereas isRoot() can change between now and when the lock is
			// acquired in the next line.
			if (v.is(Visited.POST)) {
				// Note: Not checking if all children are free'ed or if the node
				// has unprocessed arcs. It could be interleaved with contraction.

				logger.fine(() -> String.format("%s: Skipping (unlocked) post-visted v %s", getId(), v));
				// my job is already done
				return;
			}

			// ...but if v becomes a root again, a dedicate job for v is
			// scheduled which is guaranteed to happen before this isRoot check.
			if (!v.isRoot()) {
				logger.fine(() -> String.format("%s: Skipping (unlocked) non-root v %s", getId(), v));
				// my job is already done
				return;
			}
			
			// Get lock of v
			if (v.tryLock()) {
				vLock.inc();
				// Skip POST-visited v
				if (v.is(Visited.POST)) {
					// If POST, there must not be any children. 
					assert!v.hasChildren();
					// All arcs must be traversed
					assert!v.hasArcs();

					logger.fine(() -> String.format("%s: Skipping post-visited v %s", getId(), v));
					v.unlock();
					return;
				}

				// Skip non-root v
				if (!v.isRoot()) {
					logger.fine(() -> String.format("%s: Skipping non-root v %s", getId(), v));
					v.unlock();
					return; // A new worker will be scheduled by our tree
									// root. We are a child right now.
				}

				/*
				 * Then traverse the next outgoing untraversed arc;
				 */
				int arc = Graph.NO_ARC;
				NEXT_ARC : while ((arc = v.getArc()) != Graph.NO_ARC) {
					// To traverse an arc (v, w), if w is postvisited do
					// nothing.
					final GraphNode w = graph.get(arc);

					// During contraction, out-arcs of contracted nodes are
					// merged creating duplicates. As we don't want to pay the
					// price to discard duplicates during contraction, check if
					// w is already POST-visited OUTSIDE of lock acquisition.
					// However, as it turns out, this cannot be done because it
					// opens the door to a dirty read when interleaved with w
					// being contracted. (We read the contracted w node at 
					// graph.get(arc) above instead of the node w got contracted
					// into).
//					if (w.is(Visited.POST)) {
//						v.removeArc(arc);
//						continue NEXT_ARC;
//					}
					
					GraphNode root = null;
					if ((root = graph.tryLockTrees(w)) != null) {
						wLock.inc();
						v.removeArc(arc);
						
						if (w.equals(v)) {
							// TODO self-loop, might check stuttering here
							logger.fine(() -> String.format("%s: Check self-loop on v (%s)", getId(), v));
							
							// do nothing and don't unlock w. It would unlock v too.
							continue NEXT_ARC;
						}

						// w happens to be done, just release the lock and move
						// onto the next arc
						if (w.is(Visited.POST)) {
							// v # w (due to previous check)
							graph.unlockTrees(w, root);
							continue NEXT_ARC;
						}

						// Otherwise...
						if (!root.equals(v)) { // No need to findroot(v). v is by definition a root, thus compare w's root with v => O(n) * 1
							// If w is in a different tree than v, make w the
							// parent of v and mark w previsited if it is
							// unvisited.
							v.setParent(w);
							logger.info(() -> String.format("%s: ### w (%s) PARENT OF v (%s)", getId(), w.getId(),
									v.getId()));

							// We've potentially just created a new root
							final GraphNode vOld = v;
							this.v = w;

							/*
							 * Since v is now a child, it is not (for the
							 * moment) eligible for further processing. The
							 * thread can switch to an arbitrary root, or it can
							 * check to see if the root of the tree containing v
							 * and w is idle, and switch to this root if so.
							 */
							if (w.isRoot()) {
								vOld.unlock();
								continue NEXT_ARC;
							}
							graph.unlockTrees(w, root);
							vOld.unlock();
							return;
						} else if (!w.equals(v)) {
							/*
							 * The other possibility is that w is in the same
							 * tree as v. If v = w, do nothing. (Self-loops can
							 * be created by contractions.)
							 */
							/*
							 * If v # w, contract all the ancestors of w into a
							 * single vertex, which is a root. It may be
							 * convenient and efficient to view this contraction
							 * as contracting all the vertices into v, since v
							 * is already a root.
							 * 
							 * Continue processing this root. (The new root
							 * inherits all the outgoing un-traversed arcs from
							 * the set of contracted vertices).
							 */

							// Put SCC in a global set of sccs
							logger.fine(
									() -> String.format("%s: Trying to contracted w (%s) into v (%s)", getId(), w, v));
							v.contract(sccs, graph, w);
							logger.info(() -> String.format("%s: +++ Contracted w (%s) into v (%s)", getId(), w, v));

							// This is when an SCC has been found in v.
							// TODO SCCs might not be maximal SCCs.
							// TODO Release any lock we own and work on a copy?
							// After all, we don't want to block the concurrent
							// fast SCC search.
							if (v.checkSCC()) {
								continue NEXT_ARC;
							} else {
								// All other threads can stop, we've found a
								// violation.
								executor.shutdownNow();
								// TODO Throw something better here
								throw new RuntimeException("SCC violates liveness");
							}
						}
					} else {
						// Failed to acquire w lock, try later again but release
						// v's lock first. It's possible we failed to acquire
						// w's lock because of a cyclic lock graph.
						wLockFail.inc();
						v.unlock();
						executor.execute(this);
						return;
					}
				}
				// No arcs left, become post-visited and free childs
				freeChilds();
				v.unlock();
				return;
			} else {
				vLockFail.inc();
				// Cannot acquire v lock, try later
				executor.execute(this);
				return;
			}
		} catch (Exception | Error e) {
			logger.severe(() -> String.format("%s: Exception: %s", SCCWorker.this.getId(), e.getMessage()));
			e.printStackTrace();
			throw e;
		}
	}

	private void freeChilds() {
		logger.fine(() -> String.format("%s: Freeing children of v.", getId(), v.getId()));

		// No untraversed arcs left (UN) or no arcs at all (UN).
		assert v.isNot(Visited.POST);

		// All arcs must be traversed
		assert!v.hasArcs();

		/*
		 * if there is no such arc: a) mark the root postvisited
		 */
		v.set(Visited.POST);

		// Cut its remaining direct tree childs loose. This
		// is i.e. necessary for Graph A, when:
		//
		// Tree is (with 3&4 contracted):
		// (3 < 4) < 2 < 1
		// and the untraversed arcs are
		// {{1,1},{2,1}}.
		//
		// At this point, 2 must become a root so that its
		// last untraversed arc {2,1} gets explored and node
		// 2 and 1 contracted.

		/*
		 * b) and make all its children idle roots (POST visited children are by
		 * definition not roots).
		 * 
		 * TODO Bob's errata note: I overlooked one thing in my high-level
		 * description of the proposed algorithm: when a root runs out of
		 * outgoing arcs to be traversed and it is marked as post visited,
		 * deleting it breaks its tree into s number of new trees, one per
		 * child. This means that one cannot use the disjoint-set data structure
		 * to keep track of trees, since sets must be broken up as well as
		 * combined. There are efficient data structures to solve this
		 * more-complicated problem, notably Euler tour trees, which represent a
		 * tree by an Euler tour stored in a binary search tree. The time for a
		 * query is O(logn), as is the time to add an arc (a link) or break an
		 * arc (a cut).
		 */
		// Cut all children of v in the tree (not graph which are
		// its arcs). This essentially converts them
		// into roots which in turn makes them eligible for further
		// processing.
		//
		// The assumption is that we can just collect the children
		// and null all their pointers. The pointer from v to the
		// will be irrelevant, as v's tree will be garbage
		// collected.
		//TODO parallelize
		final Iterator<NaiveTreeNode> iterator = v.iterator();
		while(iterator.hasNext()) {
			final GraphNode child = (GraphNode) iterator.next();
			child.cut();
			// Now that the child is free, it's a root again.
			logger.info(() -> String.format("%s: Free'ed child (%s)", getId(), child));
			executor.execute(new SCCWorker(executor, graph, sccs, child));
		}

		// All our children must in fact be cut loose
		assert!v.hasChildren();
	}

	public int getId() {
		return v.getId();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(getId());
	}

	public static class LockRatioGauge extends RatioGauge {
		private Counter success;
		private Counter fail;

		public LockRatioGauge(Counter success, Counter fail) {
			this.success = success;
			this.fail = fail;
		}

		@Override
		protected Ratio getRatio() {
			return Ratio.of(success.getCount(), fail.getCount());
		}

	}
}
