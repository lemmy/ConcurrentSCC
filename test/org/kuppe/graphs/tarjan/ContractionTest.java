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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class ContractionTest {
	
	private final ExecutorService noopExecutor = new NoopExecutorService();

	@Test
	public void testInvalidVisitedStateChildContraction() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 1,2);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 1,2);
		
		one.setParent(two);
		try {
			two.contract(new HashMap<GraphNode, Set<GraphNode>>(0), graph, one);
		} catch (AssertionError e) {
			return;
		}
		Assert.fail("A node has to be PRE-visited if its contracted into its root (or assertions \"-ea\" disabled)");
	}
	
	@Test
	public void testInvalidContraction() {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2,4,5);
		
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 2);
		
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 1);
		
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,4);
		
		final GraphNode five = new GraphNode(5);
		graph.addNode(five, 3);
		
		// The SCC in F
		five.setParent(three);
		one.setParent(four);
		// one is root at this
		three.setParent(one);

		// Cannot set two as parent of one, four is its parent already. 
		try {
			one.setParent(two);
		} catch (RuntimeException e) {
			Assert.assertEquals("non-root link", e.getMessage());
			return;
		}
		Assert.fail("Linked non-root node.");
	}
	
	@Test
	public void testContractionsInC() {
		final Graph graph = new Graph();

		final GraphNode one = new GraphNode(1);
		graph.addNode(one,3);
		final GraphNode two = new GraphNode(2);
		graph.addNode(two,3);
		final GraphNode three = new GraphNode(3);
		graph.addNode(three,4,5);
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,1);
		final GraphNode five = new GraphNode(5);
		graph.addNode(five,2);
		
		// One possible permutation of child>parent relationships in the tree
		one.setParent(three);
		one.set(Visited.PRE);
		five.setParent(two);
		five.set(Visited.PRE);
		three.setParent(four);
		three.set(Visited.PRE);
		two.setParent(three);
		two.set(Visited.PRE);
		
		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);
		four.contract(sccs, graph, one);
		Assert.assertTrue(one.is(Visited.POST));
		Assert.assertTrue(three.is(Visited.POST));
		
		four.contract(sccs, graph, five);
		Assert.assertTrue(five.is(Visited.POST));
		Assert.assertTrue(two.is(Visited.POST));

		final Set<GraphNode> expected = new HashSet<GraphNode>();
		expected.add(one);
		expected.add(two);
		expected.add(three);
		expected.add(four);
		expected.add(five);
		Set<GraphNode> actual = sccs.get(four);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testContractionsInF() throws Exception {
		final Graph graph = new Graph();

		// a star with one loop
		final GraphNode one = new GraphNode(1);
		graph.addNode(one, 2,4,5);
		
		final GraphNode two = new GraphNode(2);
		graph.addNode(two, 2);
		
		final GraphNode three = new GraphNode(3);
		graph.addNode(three, 1);
		
		final GraphNode four = new GraphNode(4);
		graph.addNode(four,4);
		
		final GraphNode five = new GraphNode(5);
		graph.addNode(five, 3);

		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);
		
		// The SCC in F
		five.setParent(three);
		five.set(Visited.PRE);
		one.setParent(four);
		one.set(Visited.PRE);
		// one is root at this
		three.setParent(one);
		three.set(Visited.PRE);
		// one is not at root at this point

		Assert.assertTrue(!one.isRoot());
		
		// Roots: (2), (4)
		// Untraversed arcs: 1:5
		//
		// Once (4) gets explored, it is found that it has no children and its
		// child (1) must be cut loose.
		four.set(Visited.PRE);
		new SCCWorker(noopExecutor, graph, sccs, four).call();
		Assert.assertTrue(four.is(Visited.POST));
		Assert.assertTrue(one.isRoot());

		// Now one should be free again and contract its children into an SCC
		final SCCWorker sccWorker = new SCCWorker(noopExecutor, graph, sccs, one);
		sccWorker.call();
		
		final Set<GraphNode> expected = new HashSet<GraphNode>();
		expected.add(one);
		expected.add(three);
		expected.add(five);
		Assert.assertEquals(expected, sccs.get(one));
	}
	
	private static class NoopExecutorService implements ExecutorService {

		@Override
		public void execute(Runnable command) {
		}

		@Override
		public void shutdown() {
		}

		@Override
		public List<Runnable> shutdownNow() {
			return new ArrayList<>();
		}

		@Override
		public boolean isShutdown() {
			return true;
		}

		@Override
		public boolean isTerminated() {
			return true;
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			return true;
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			try {
				task.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			return null;
		}

		@Override
		public Future<?> submit(Runnable task) {
			return null;
		}

		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
			return null;
		}

		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException {
			return null;
		}

		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			return null;
		}

		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return null;
		}
	}
}
