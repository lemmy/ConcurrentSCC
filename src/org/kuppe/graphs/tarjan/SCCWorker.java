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
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class SCCWorker implements Callable<Void> {

	private final ExecutorService executor;
	private final Set<Set<GraphNode>> sccs;

	private GraphNode v;

	public SCCWorker(final ExecutorService executor, Set<Set<GraphNode>> sccs, final GraphNode root) {
		this.executor = executor;
		this.sccs = sccs;
		this.v = root;
	}
	
	public Void call() throws Exception {
		synchronized (v.contracted) {
			if (v.is(Visited.POST)) {
				return null;
			}

			// Mark the vertex visited (PRE)
			v.set(Visited.PRE);

			/*
			 * Then traverse the next outgoing untraversed arc;
			 */
			final Iterator<Arc> itr = v.getSuccessor().iterator();
			while (itr.hasNext()) {

				/*
				 * ..traverse the next outgoing untraversed arc
				 */
				final Arc arc = itr.next();
				if (arc.setTraversed()) {
					continue;
				}

				// To traverse an arc (v, w), if w is postvisited do nothing.
				final GraphNode w = arc.getTo();
				if (w.is(Visited.POST)) {
					continue;
				}

				// Otherwise...
				if (!w.isInSameTree(v)) {
					// If w is in a different tree than v, make w the
					// parent of v and mark w previsited if it is unvisited.
					v.setParent(w);

					w.set(Visited.PRE);

					// We've potentially just created a new root
					this.v = w;

					/*
					 * Since v is now a child, it is not (for the moment) eligible
					 * for further processing. The thread can switch to an arbitrary
					 * root, or it can check to see if the root of the tree
					 * containing v and w is idle, and switch to this root if so.
					 */
					executor.submit(this);
					return null;
				} else {
					/*
					 * The other possibility is that w is in the same tree as v. If
					 * v = w, do nothing. (Self-loops can be created by
					 * contractions.)
					 */
					if (!w.equals(v)) {
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
						v.contract(sccs, w);

						// This is when an SCC has been found in v.
						// TODO SCCs might not be maximal SCCs.
						// TODO Release any lock we own and work on a copy?
						// After all, we don't want to block the concurrent fast
						// SCC search.
						if (v.checkSCC()) {
							// v is a (contracted) root and thus eligible
							// for further processing.	this.v = v;

							executor.submit(this);
							return null;
						} else {
							// All other threads can stop, we've found a
							// violation.
							executor.shutdownNow();
							// TODO Throw something better here
							throw new RuntimeException("SCC violates liveness");
						}
					} else {
						// do nothing
						//TODO self-loop, might check stuttering here 
					}
				}

			}

			// All arcs of v must be traversed by now
			v.getSuccessor().forEach((arc) -> {
				assert arc.isTraversed();
			});

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
			for (Arc arc : v.getSuccessor()) {
				final GraphNode to = arc.getTo();
				if (to.isNot(Visited.POST)) {
					executor.submit(new SCCWorker(executor, sccs, to));
				}
			}
		}
		return null;
	}
}
