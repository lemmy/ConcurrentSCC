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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ConcurrentFastSCC {
	
	
	
	public Set<Set<GraphNode>> searchSCCs(final List<GraphNode> initNodes) {
		
		//TODO Name threads inside executor to aid debugging.
		// see http://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
		final ForkJoinPool executor = new ForkJoinPool(1);

		final Set<Set<GraphNode>> sccs = Collections.newSetFromMap(new ConcurrentHashMap(0));

		for (GraphNode graphNode : initNodes) {
			executor.submit(new SCCWorker(executor, sccs, graphNode));
			executor.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		
		executor.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		executor.shutdown();
		
		return sccs;
	}
		
//	private Set<Stack<GraphNode>> doIt(Collection<Callable<Void>> tasks) {
//		
//		// TODO 25 is obviously an arbitrary number, but keep queue length bounded
//		// so that it doesn't grow indefinitely.
//		// TODO remove duplicates from queue after contractions to minimize space consumption.
//		final BlockingQueue<GraphNode> roots = new ArrayBlockingQueue<>(25, false, initNodes);
//		
//		executor.submit(new Callable<Void>() {
//
//			public Void call() throws InterruptedException {
//			}
//		});
//		
//		
//		try {
//			executor.shutdown();
//			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
//			assert roots.isEmpty();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		return sccs;
//	}
}
