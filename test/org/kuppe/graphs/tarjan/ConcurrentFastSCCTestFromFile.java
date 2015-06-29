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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Collections2;

public class ConcurrentFastSCCTestFromFile extends AbstractConcurrentFastSCCTest {

	/*
	 * tinyDG.txt has 13 nodes, 22 arcs
	 * 
	 * 3 (non-trivial) components:
	 * {0 2 3 4 5}, {9 10 11 12}, {6 8} 
	 */
	@Test
	public void testTiny() throws IOException {
		final Graph graph = new Graph();
		readFile(graph, "tinyDG.txt");
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition(13));
		Assert.assertEquals(printSCCs(sccs), 3, sccs.size());
		
		testTinySCCs(graph, sccs);
	}
	
	@Test
	public void testTinyLoop() throws IOException {
		final Graph graph = new Graph();
		readFile(graph, "tinyDG.txt");
		
		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);

		final Collection<GraphNode> nodes = graph.getStartNodes();
		final NoopExecutorService executor = new NoopExecutorService();
		while (!graph.checkPostCondition(13)) {
			for (GraphNode graphNode : nodes) {
				new SCCWorker(executor, graph, sccs, graphNode).call();
			}
		}
		testTinySCCs(graph, new HashSet<Set<GraphNode>>(sccs.values()));
	}	
	
//	@Test
	public void testTinyLoopAllPermutations() throws IOException {
		final Graph graph = new Graph();
		readFile(graph, "tinyDG.txt");
		
		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);

		final NoopExecutorService executor = new NoopExecutorService();

		int i = 0;
		final Collection<List<GraphNode>> permutations = Collections2.permutations(graph.getStartNodes());
		for (Collection<GraphNode> permutation : permutations) {
			if (i++ % 1000000 == 0)
				System.out.println(String.format("Checking %s permutation of %s of start nodes", i, permutations.size()));
			while (!graph.checkPostCondition(13)) {
				for (GraphNode graphNode : permutation) {
					new SCCWorker(executor, graph, sccs, graphNode).call();
				}
			}
			testTinySCCs(graph, new HashSet<Set<GraphNode>>(sccs.values()));
		}
	}	
	
	private void testTinySCCs(final Graph graph, final Set<Set<GraphNode>> sccs) {
		Assert.assertEquals(printSCCs(sccs), 3, sccs.size());
	
		final Set<Set<Integer>> converted = convertToInts(sccs);

		// Now compare ints
		// Now compare ints
		final Set<Set<Integer>> expected = new HashSet<Set<Integer>>();
		Set<Integer> anSCC = new HashSet<Integer>();
		anSCC.add(0);
		anSCC.add(2);
		anSCC.add(3);
		anSCC.add(4);
		anSCC.add(5);
		expected.add(anSCC);

		anSCC = new HashSet<Integer>();
		anSCC.add(9);
		anSCC.add(10);
		anSCC.add(11);
		anSCC.add(12);
		expected.add(anSCC);
		
		anSCC = new HashSet<Integer>();
		anSCC.add(6);
		anSCC.add(8);
		expected.add(anSCC);
		
		Assert.assertEquals(printSCCs(sccs), expected, converted);
	}
	
	/*
	 * mediumDG.txt has 49 nodes (no node #21) and 147 (136 without dupes) arcs
	 * 
	 * 2 (non-trivial) components
	 * {2 5 6 8 9 11 12 13 15 16 18 19 22 23 25 26 28 29 30 31 32 33 34 35 37 38 39 40 42 43 44 46 47 48 49},
	 * {3 4 17 20 24 27 36}
	 */
	@Test
	public void testMedium() throws IOException {
		final Graph graph = new Graph();
		readFile(graph, "mediumDG.txt");

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		testMediumSCCs(graph, sccs);
	}
	
	@Test
	public void testMediumLoop() throws IOException {
		final Graph graph = new Graph();
		readFile(graph, "mediumDG.txt");
		
		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);

		final Collection<GraphNode> nodes = graph.getStartNodes();
		final NoopExecutorService executor = new NoopExecutorService();
		while (!graph.checkPostCondition(50)) {
			for (GraphNode graphNode : nodes) {
				new SCCWorker(executor, graph, sccs, graphNode).call();
			}
		}
		testMediumSCCs(graph, new HashSet<Set<GraphNode>>(sccs.values()));
	}

	private void testMediumSCCs(final Graph graph, final Set<Set<GraphNode>> sccs) {
		Assert.assertTrue(graph.checkPostCondition(50));
		Assert.assertEquals(printSCCs(sccs), 2, sccs.size());
	
		final Set<Set<Integer>> converted = convertToInts(sccs);

		// Now compare ints
		final Set<Set<Integer>> expected = new HashSet<Set<Integer>>();
		Set<Integer> anSCC = new TreeSet<Integer>();
		anSCC.add(3);
		anSCC.add(4);
		anSCC.add(17);
		anSCC.add(20);
		anSCC.add(24);
		anSCC.add(27);
		anSCC.add(36);
		expected.add(anSCC);

		anSCC = new TreeSet<Integer>();
		anSCC.add(2);
		anSCC.add(5);
		anSCC.add(6);
		anSCC.add(8);
		anSCC.add(9);
		anSCC.add(11);
		anSCC.add(12);
		anSCC.add(13);
		anSCC.add(15);
		anSCC.add(16);
		anSCC.add(18);
		anSCC.add(19);
		anSCC.add(22);
		anSCC.add(23);
		anSCC.add(25);
		anSCC.add(26);
		anSCC.add(28);
		anSCC.add(29);
		anSCC.add(30);
		anSCC.add(31);
		anSCC.add(32);
		anSCC.add(33);
		anSCC.add(34);
		anSCC.add(35);
		anSCC.add(37);
		anSCC.add(38);
		anSCC.add(39);
		anSCC.add(40);
		anSCC.add(42);
		anSCC.add(43);
		anSCC.add(44);
		anSCC.add(46);
		anSCC.add(47);
		anSCC.add(48);
		anSCC.add(49);
		expected.add(anSCC);
		
		Assert.assertEquals(printSCCs(sccs), expected, converted);
	}
	
	/*
	 * largeDG.txt has 25 components, 1.000.000 nodes and 7.500.000 arcs
	 */
	@Test
	public void testLarge() throws IOException {
		final Graph graph = new Graph();
		readFile(graph, "largeDG.txt");

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition(1000000));
		Assert.assertEquals(25, sccs.size());
	}	
	
//	@Test
	public void testLargeLoop() throws IOException {
		final Graph graph = new Graph();
		readFile(graph, "largeDG.txt");

		final Map<GraphNode, Set<GraphNode>> mapOfSccs = new HashMap<GraphNode, Set<GraphNode>>(0);

		final Collection<GraphNode> nodes = graph.getStartNodes();
		final NoopExecutorService executor = new NoopExecutorService();
		while (!graph.checkPostCondition(1000000)) {
			for (GraphNode graphNode : nodes) {
				new SCCWorker(executor, graph, mapOfSccs, graphNode).call();
			}
		}

		// convert
		final Set<Set<GraphNode>> sccs = new HashSet<Set<GraphNode>>(mapOfSccs.values());
		
		Assert.assertTrue(graph.checkPostCondition(1000000));
		Assert.assertEquals(printSCCs(sccs), 25, sccs.size());
	}	

	// Convert the set of sets of GraphNodes into a set of sets of ints
	private Set<Set<Integer>> convertToInts(final Set<Set<GraphNode>> sccs) {
		final Set<Set<Integer>> converted = new HashSet<Set<Integer>>();
		for (Set<GraphNode> set : sccs) {
			Set<Integer> anSCC = new TreeSet<Integer>();
			for (GraphNode graphNode : set) {
				anSCC.add(graphNode.getId());
			}
			converted.add(anSCC);
		}
		return converted;
	}
	
	private static void readFile(Graph graph, String filename) throws IOException {
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream(filename);
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				String[] split = line.trim().split("\\s+");
				int nodeId = Integer.parseInt(split[0]);
				int arcId = Integer.parseInt(split[1]);

				graph.get(nodeId);
				graph.addArc(nodeId, arcId);
			}
		}
	}
}
