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
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class ConcurrentFastSCC {
	
	public Set<Stack<GraphNode>> searchSCCs(final List<GraphNode> initNodes) {
		final Set<Stack<GraphNode>> sccs = new HashSet<Stack<GraphNode>>();
		
		// TODO 25 is obviously an arbitrary number, but keep queue length bounded
		// so that it doesn't grow indefinitely.
		// TODO remove duplicates from queue after contractions to minimize space consumption.
		final BlockingQueue<GraphNode> roots = new ArrayBlockingQueue<>(25);
		roots.addAll(initNodes);
		
		//TODO Name threads inside executor to aid debugging.
		// see http://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		
		final Future<Boolean> future = executor.submit(new Callable<Boolean>() {
			/* (non-Javadoc)
			 * @see java.util.concurrent.Callable#call()
			 */
			public Boolean call() throws Exception {
				// Nothing to be done anymore
				if (roots.isEmpty()) {
					return true;
				}
				
				// Pick an idle/unvisited vertex
				final GraphNode v = roots.take();
				
				if (v.is(Visited.POST)) {
					executor.submit(this);
					return true;
				}

				// root possibly has been contracted by now. Skip it and move to
				// the next one.
//				if (v.is(Visited.PRE)) {
//					executor.submit(this);
//					return true;
//				}
				
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
					if (arc.isTraversed()) {
						continue;
					}
					arc.setTraversed();
					
					final GraphNode w = arc.getTo();
					
					// To traverse an arc (v, w), if w is postvisited do nothing.
					if (w.is(Visited.POST)) {
						continue;
					}
					
					// Otherwise...
					if (!w.isInSameTree(v)) {
						// If w is in a different tree than v, make w the
						// parent of v and mark w previsited if it is unvisited.
						v.setParent(w);
						
						if (w.is(Visited.UN)) {
							w.set(Visited.PRE);
						}
						
						// We've potentially just created a new root
						roots.add(w);
						
						/*
						 * Since v is now a child, it is not (for the moment)
						 * eligible for further processing. The thread can
						 * switch to an arbitrary root, or it can check to see
						 * if the root of the tree containing v and w is idle,
						 * and switch to this root if so.
						 */
						executor.submit(this);
						return true;
					} else {
						/*
						 * The other possibility is that w is in the same tree
						 * as v. If v = w, do nothing. (Self-loops can be
						 * created by contractions.)
						 */
						if (!w.equals(v)) {
							/*
							 * If v # w, contract all the ancestors of w into a
							 * single vertex, which is a root.
							 * It may be convenient and efficient to view this
							 * contraction as contracting all the vertices into
							 * v, since v is already a root.
							 * 
							 * Continue processing this root. (The new root
							 * inherits all the outgoing un-traversed arcs from
							 * the set of contracted vertices).
							 */
							
							// Put SCC in a global set of sccs
							sccs.add(v.contract(w));
							
							// This is when an SCC has been found in v.
							// TODO SCCs might not be maximal SCCs.
							if(v.checkSCC()) {
								// v is a (contracted) root and thus eligable
								// for further processing.
								roots.add(v);
								
								executor.submit(this);
								return true;
							} else {
								// All other threads can stop, we've found a
								// violation.
								executor.shutdownNow();
								//TODO Throw something better here
								throw new RuntimeException("SCC violates liveness");
							}
						} else {
							// do nothing
							System.out.println("nop");
						}
					}
					
				}
				
				// All arcs of v must be traversed by now 
				v.getSuccessor().forEach( (arc) -> {assert arc.isTraversed();} );
				
				/*
				 * if there is no such arc: 
				 * a) mark the root postvisited
				 */
				v.set(Visited.POST);

				/*
				 * b) and make all its children idle roots (POST visited
				 * children are by definition not roots).
				 * 
				 * TODO Read Bob's errata note
				 */
				for (Arc arc : v.getSuccessor()) {
					final GraphNode to = arc.getTo();
					if (to.isNot(Visited.POST)) {
						roots.add(to);
					}
				}
				
				executor.submit(this);
				return true;
			}
		});
		
		
		try {
//			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
			executor.shutdown();
			assert roots.isEmpty();
			
			future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return sccs;
	}
}
