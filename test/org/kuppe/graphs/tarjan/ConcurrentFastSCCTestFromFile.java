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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(printSCCs(sccs), 3, sccs.size());
		
		final Set<Set<Integer>> converted = convertToInts(sccs);
		
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
		doTestMedium("mediumDG.txt");
	}
	
	@Test
	public void testMediumNoDupes() throws IOException {
		doTestMedium("mediumDGnodupes.txt");
	}
	
	private void doTestMedium(String filename) throws IOException {
		final Graph graph = new Graph();
		readFile(graph, filename);
		testResultFileRead(graph);

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		testMediumSCCs(graph, sccs);
		System.out.println(printSCCs(sccs));
	}
	
	@Test
	public void testMediumLoop() throws IOException {
		doTestMediumLoop("mediumDG.txt");
	}
	
	@Test
	public void testMediumLoopNoDupes() throws IOException {
		doTestMediumLoop("mediumDGnodupes.txt");
	}
	
	private void doTestMediumLoop(String filename) throws IOException {
		final Graph graph = new Graph();
		readFile(graph, filename);
		testResultFileRead(graph);
		
		final Map<GraphNode, Set<GraphNode>> sccs = new HashMap<GraphNode, Set<GraphNode>>(0);

		final NoopExecutorService executor = new NoopExecutorService();
		final Collection<GraphNode> nodes = graph.getStartNodes();
		while (!graph.checkPostCondition()) {
			for (GraphNode graphNode : nodes) {
				new SCCWorker(executor, graph, sccs, graphNode).call();
			}
		}
		testMediumSCCs(graph, new HashSet<Set<GraphNode>>(sccs.values()));
	}

	private void testResultFileRead(final Graph graph) {
		//Check the graph has correctly been read.
		int sum = 0;
		final Collection<GraphNode> startNodes = graph.getStartNodes();
		for (GraphNode graphNode : startNodes) {
			sum += graph.getUntraversedArcs(graphNode).size();
		}
		Assert.assertEquals(136, sum);
		Assert.assertEquals(49, startNodes.size());
	}

	private void testMediumSCCs(final Graph graph, final Set<Set<GraphNode>> sccs) {
		Assert.assertTrue(graph.checkPostCondition());
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
//	@Test
//	public void testLarge() throws IOException {
//		final Graph graph = new Graph();
//		readFile(graph, "largeDG.txt");
//
//		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
//  	Assert.assertTrue(graph.checkPostCondition());
//		Assert.assertEquals(25, sccs.size());
//	}
	

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
		
//		final Set<Integer> irrelevant = new HashSet<>();
//		
//		// sources
//		irrelevant.add(10);
//		irrelevant.add(0);
//		irrelevant.add(7);
//		irrelevant.add(41);
//
//		irrelevant.add(1);
//		irrelevant.add(45);
//		irrelevant.add(14);
//
//		// second scc
//		irrelevant.add(20);
//		irrelevant.add(3);
//		irrelevant.add(36);
//		irrelevant.add(17);
//		irrelevant.add(24);
//		irrelevant.add(4);
//		irrelevant.add(27);
//		
//		// sink
//		irrelevant.add(21);
//		
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream(filename);
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			int prev = -1;
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				String[] split = line.trim().split("\\s+");
				int nodeId = Integer.parseInt(split[0]);
				int arcId = Integer.parseInt(split[1]);
//				
//				if (irrelevant.contains(nodeId)) {
//					continue;
//				}
//				
//				// print for TLC
//				if (nodeId != prev) {
//					System.out.print("},{");
//					prev = nodeId;
//				}
//				System.out.print("," + (arcId + 1));
//				
				// skip irrelevant nodes
				graph.get(nodeId);
				graph.addArc(nodeId, arcId);
			}
		}
	}
}
