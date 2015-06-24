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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class SCCWorker implements Callable<Void> {

	private static final Lock GLOBAL_LOCK = new ReentrantLock(true);
//	private static final Random rnd = new Random(1541980);
	
	private final ExecutorService executor;
	private final Map<GraphNode, Set<GraphNode>> sccs;
	private final Graph graph;
	private final int id;
	private GraphNode v;

	public SCCWorker(int id, final ExecutorService executor, final Graph graph, Map<GraphNode, Set<GraphNode>> sccs, final GraphNode root) {
		this.id = id;
		this.executor = executor;
		this.graph = graph;
		this.sccs = sccs;
		this.v = root;
	}
	
	public Void call() throws Exception {
		GLOBAL_LOCK.lock();
		System.out.println("----------- " + this.getId()+ " ----------------");
		try {
			if (v.is(Visited.POST)) {
				System.out.println(String.format("%s: Skipping post-visted v %s", getId(), v));
				// my job is already done
				return null;
			}
			if (graph.tryLock(this, v)) {
				if (v.is(Visited.POST) || !v.isRoot()) {
					System.out.println(String.format("%s: Skipping v %s", getId(), v));
					graph.unlock(this, v);
					return null;
				}

				/*
				 * Then traverse the next outgoing untraversed arc;
				 */
				Arc arc = graph.getUntraversedArc(v);
				if (arc != null) {
					// To traverse an arc (v, w), if w is postvisited do nothing.
					final GraphNode w = graph.get(arc.getTo());
					
					if (w.equals(v)) {
						arc.setTraversed();
						//TODO self-loop, might check stuttering here

						// do nothing
						graph.unlock(this, v);
						executor.submit(this);
						return null;
					}
					
					if (graph.tryLockTrees(this, w, v)) {
						// We now have v and w locked, lets start doing work with it
						// Mark the vertex visited (PRE)
						v.set(Visited.PRE);

						
						// w happens to be done, just release the lock and move
						// onto the next arc
						if (w.is(Visited.POST)) {
							arc.setTraversed();
							graph.unlock(this, v);
							return null;
						}
						
						// Otherwise...
						if (!w.isInSameTree(v)) {
							// If w is in a different tree than v, make w the
							// parent of v and mark w previsited if it is unvisited.
							v.setParent(w);
							System.out.println(String.format("%s: ### w (%s) PARENT OF v (%s)", id, w, v));

							w.set(Visited.PRE);

							arc.setTraversed();

							// We've potentially just created a new root
							final GraphNode vOld = v;
							this.v = w;
							
							boolean isRoot = w.isRoot();
							
							graph.unlockTrees(this, w, v);
							graph.unlock(this, vOld);
							/*
							 * Since v is now a child, it is not (for the moment) eligible
							 * for further processing. The thread can switch to an arbitrary
							 * root, or it can check to see if the root of the tree
							 * containing v and w is idle, and switch to this root if so.
							 */
							if (isRoot) {
								executor.submit(this);
								return null;
							}
						} else if (!w.equals(v)) {
								arc.setTraversed();
								/*
								 * The other possibility is that w is in the same tree as v. If
								 * v = w, do nothing. (Self-loops can be created by
								 * contractions.)
								 */
								/*
								 * If v # w, contract all the ancestors of w into a single
								 * vertex, which is a root. It may be convenient and
								 * efficient to view this contraction as contracting all the
								 * vertices into v, since v is already a root.
								 * 
								 * Continue processing this root. (The new root inherits all
								 * the outgoing un-traversed arcs from the set of contracted
								 * vertices).
								 */

								// Put SCC in a global set of sccs
								v.contract(sccs, graph, w);
								System.out.println(String.format("%s: +++ Contracted w (%s) into v (%s)", id, w, v));

								// This is when an SCC has been found in v.
								// TODO SCCs might not be maximal SCCs.
								// TODO Release any lock we own and work on a copy?
								// After all, we don't want to block the concurrent fast
								// SCC search.
								if (v.checkSCC()) {
									boolean hasUntraversedArcs = graph.hasUntraversedArc(v);
									if (!hasUntraversedArcs) {
										v.set(Visited.POST);
									}
									
									// v is a (contracted) root and thus eligible
									// for further processing.	this.v = v;
									// No need to unlock w, has happened during contraction
									graph.unlock(this, v);
									
									executor.submit(this);
									return null;
								} else {
									// All other threads can stop, we've found a
									// violation.
									executor.shutdownNow();
									// TODO Throw something better here
									throw new RuntimeException("SCC violates liveness");
								}
							}
						}
//					} else {
//						// We couldn't acquire lock on w, try again later.
////							Thread.sleep(rnd.nextInt(50) + 1L);
//						executor.submit(this);
//						return null;
//					}
				} else {
					// No untraversed arcs left
					assert v.is(Visited.PRE);
					
					/*
					 * if there is no such arc: a) mark the root postvisited
					 */
					v.set(Visited.POST);

					/*
					 * b) and make all its children idle roots (POST visited children
					 * are by definition not roots).
					 * 
					 * TODO Bob's errata note:
					 * I overlooked one thing in my high-level description of the
					 * proposed algorithm: when a root runs out of outgoing arcs to be
					 * traversed and it is marked as post visited, deleting it breaks
					 * its tree into s number of new trees, one per child. This means
					 * that one cannot use the disjoint-set data structure to keep track
					 * of trees, since sets must be broken up as well as combined. There
					 * are efficient data structures to solve this more-complicated
					 * problem, notably Euler tour trees, which represent a tree by an
					 * Euler tour stored in a binary search tree. The time for a query
					 * is O(logn), as is the time to add an arc (a link) or break an arc
					 * (a cut).
					 */
					//TODO This seems incorrect. There won't be any untraversed arcs left.
					while ((arc = graph.getUntraversedArc(v)) != null) {
						final GraphNode to = graph.get(arc.getTo());
						if (to.isNot(Visited.POST)) {
							executor.submit(new SCCWorker(-1, executor, graph, sccs, to));
						}
					}
					graph.unlock(this, v);
				}
			} else {
				// Cannot acquire lock, try later
//				Thread.sleep(rnd.nextInt(50) + 1L);
				executor.submit(this);
				return null;
			}
			return null;
		} finally {
			GLOBAL_LOCK.unlock();
		}
	}

	public int getId() {
		return id;
	}
}
