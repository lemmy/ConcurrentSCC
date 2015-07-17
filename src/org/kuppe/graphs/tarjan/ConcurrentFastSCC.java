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

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ConcurrentFastSCC {
	
	public Set<Set<GraphNode>> searchSCCs(final Graph graph) {
		// reset counters
		SCCWorker.V_LOCK_FAIL.set(0);
		SCCWorker.V_LOCK_SUCC.set(0);
		SCCWorker.W_LOCK_FAIL.set(0);
		SCCWorker.W_LOCK_SUCC.set(0);
		Graph.AVERAGE_FAIL_CNT.set(1);
		Graph.AVERAGE_FAIL_LENGTH.set(1);
		Graph.AVERAGE_FINDROOT_CNT.set(1);
		Graph.AVERAGE_FINDROOT_LENGTH.set(1);
		GraphNode.AVERAGE_FIX_AMOUNT.set(1);
		GraphNode.AVERAGE_FIX_CNT.set(1);
		GraphNode.CONTRACTION_LENGTH.set(1);
		GraphNode.CONTRACTIONS.set(1);
		GraphNode.PARENTING.set(0);
		
		// TODO Name threads inside executor to aid debugging.
		// see
		// http://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
		final int availableProcessors = Runtime.getRuntime().availableProcessors();
		final ForkJoinPool executor = new ForkJoinPool();

		// The map of sccs passed around by SCCWorkers
		final Map<GraphNode, GraphNode> sccs = new ConcurrentHashMap<GraphNode, GraphNode>();
		
		// Take timestamp of when actual work started
		final long start = System.currentTimeMillis();
		
		final List<AppendableIterator<GraphNode>> iterators = graph.partition(availableProcessors);
		for (AppendableIterator<GraphNode> iterator : iterators) {
			executor.execute(new SCCWorker(executor, graph, iterator, sccs));
		}

		// Wait until no SCCWorker is running and no SCCWorker is queued.
		executor.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		executor.shutdown();

		// Print runtime statistics
		if (graph.getName() != null) {
			System.out.println("=============" + graph.getName() + "============");
			System.out.println("Failed v locks: " + DecimalFormat.getInstance().format(SCCWorker.V_LOCK_FAIL.get()));
			System.out.println("Succss v locks: " + DecimalFormat.getInstance().format(SCCWorker.V_LOCK_SUCC.get()));
			System.out.println("Failed w locks: " + DecimalFormat.getInstance().format(SCCWorker.W_LOCK_FAIL.get()));
			System.out.println("Succss w locks: " + DecimalFormat.getInstance().format(SCCWorker.W_LOCK_SUCC.get()));
			System.out.println("Avg. succ length: " + DecimalFormat.getInstance()
					.format(Graph.AVERAGE_FINDROOT_LENGTH.get() / Graph.AVERAGE_FINDROOT_CNT.get()));
			System.out.println("Avg. fail length: " + DecimalFormat.getInstance()
					.format(Graph.AVERAGE_FAIL_LENGTH.get() / Graph.AVERAGE_FAIL_CNT.get()));
			System.out.println("Avg. dangling: " + DecimalFormat.getInstance()
					.format(GraphNode.AVERAGE_FIX_AMOUNT.get() / GraphNode.AVERAGE_FIX_CNT.get()));
			System.out.println(
					"Number of contractions: " + DecimalFormat.getInstance().format(GraphNode.CONTRACTIONS.get()));
			System.out.println("Avg. contraction length: " + DecimalFormat.getInstance()
					.format(GraphNode.CONTRACTION_LENGTH.get() / GraphNode.CONTRACTIONS.get()));
			System.out.println("Total Parenting: " + DecimalFormat.getInstance()
			.format(GraphNode.PARENTING.get()));
			System.out.println("Runtime: " + (System.currentTimeMillis() - start) / 1000 + " sec");
		}

		// Convert the result from a map with key being the parent in a tree of
		// the forest to just a set of SCCs. The parent is irrelevant from the
		// SCC POV and internal to the concurrent fast SCC algorithm.
		final Set<Set<GraphNode>> result = new HashSet<>(sccs.size());
		for (GraphNode graphNode : sccs.values()) {
			result.add(graphNode.getSCC());
		}
		return result;
	}
}