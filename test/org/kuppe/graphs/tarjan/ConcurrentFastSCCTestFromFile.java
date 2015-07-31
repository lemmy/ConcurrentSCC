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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ConcurrentFastSCCTestFromFile extends AbstractConcurrentFastSCCTest {

	@Test
	@Ignore
	public void testTLCVoteProofN4() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCVoteProofN4");
		readBinFile(graph, "/data/voteproofN4.bin");
		Assert.assertEquals(693930, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(0, sccs.size());
	}

	@Test
	public void testTLCBakeryN3() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCbakeryN3");
		readBinFile(graph, "/data/BakeryN3.bin");
		Assert.assertEquals(2212736, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(324590, sccs.size());
	}

	@Test
	@Ignore
	public void testTLCN10() throws IOException {
		// Manually copy the 2gb file to the test directory. It's too large for git to handle.
		final Graph graph = new Graph("testTLCN10");
		readFile(graph, "/data/tlcn10.txt");
		Assert.assertEquals(10508304, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(2302, sccs.size());
	}

	@Test
	public void testTLCN9() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCN9");
		readBinFile(graph, "/data/tlcn09.bin");
		Assert.assertEquals(2369550, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(1150, sccs.size());
	}

	@Test
	public void testTLCN8() throws IOException {
		final Graph graph = new Graph("testTLCN8");
		readFile(graph, "/data/tlc.txt");
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(574, sccs.size());
		
		final Set<Set<Integer>> convertedSCCs = convertToInts(sccs);
		
		// Read the file with the correct SCCs
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream("/data/tlcsccs.txt");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				final Set<Integer> hashSet = new HashSet<Integer>();
				final String[] ints = line.trim().split("\\s+");
				for (String string : ints) {
					hashSet.add(Integer.parseInt(string));
				}
				Assert.assertTrue(convertedSCCs.contains(hashSet));
			}
		}
	}
	
	@Test
	@Ignore
	public void testTLCN8JustInits() throws IOException {
		final Graph graph = new Graph("testTLCN8");
		readFile(graph, "/data/tlc.txt");
		readInits(graph, "/data/tlcinits.txt");
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(574, sccs.size());
		
		final Set<Set<Integer>> convertedSCCs = convertToInts(sccs);
		
		// Read the file with the correct SCCs
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream("/data/tlcsccs.txt");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				final Set<Integer> hashSet = new HashSet<Integer>();
				final String[] ints = line.trim().split("\\s+");
				for (String string : ints) {
					hashSet.add(Integer.parseInt(string));
				}
				Assert.assertTrue(convertedSCCs.contains(hashSet));
			}
		}
	}
	
	/*
	 * tinyDG.txt has 13 nodes, 22 arcs
	 * 
	 * 3 (non-trivial) components:
	 * {0 2 3 4 5}, {9 10 11 12}, {6 8} 
	 */
	@Test
	public void testTiny() throws IOException {
		final Graph graph = new Graph("testTiny");
		readFile(graph, "/data/tinyDG.txt");
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(printSCCs(sccs), 3, sccs.size());
		
		testTinySCCs(graph, sccs);
	}
	
	@Test
	public void testTinyLoop() throws IOException {
		final Graph graph = new Graph("testTinyLoop");
		readFile(graph, "/data/tinyDG.txt");
		
		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);

		final NoopExecutorService executor = new NoopExecutorService();
		final Iterator<GraphNode> iterator = graph.iterator();
		while (iterator.hasNext()) {
			new SCCWorker(executor, graph, sccs, iterator.next()).run();
		}
		
		final Set<Set<GraphNode>> result = new HashSet<>(sccs.size());
		for (GraphNode graphNode : sccs.values()) {
			result.add(graphNode.getSCC());
		}
		testTinySCCs(graph, result);
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
		final Graph graph = new Graph("testMedium");
		readFile(graph, "/data/mediumDG.txt");

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		testMediumSCCs(graph, sccs);
	}
	
	@Test
	public void testMediumWithThreeInitsOnly() throws IOException {
		final Graph graph = new Graph("testMediumWithInits");
		readFile(graph, "/data/mediumDG.txt");
		graph.setInit(1); // Source node
		graph.setInit(10); // Source node
		graph.setInit(20); // One node of the smaller SCC which is isolated WRT incoming arcs.

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		testMediumSCCs(graph, sccs);
	}
	
	@Test
	public void testMediumLoop() throws IOException {
		final Graph graph = new Graph("testMediumLoop");
		readFile(graph, "/data/mediumDG.txt");
		
		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);

		final NoopExecutorService executor = new NoopExecutorService();
		final Iterator<GraphNode> iterator = graph.iterator();
		while (iterator.hasNext()) {
			new SCCWorker(executor, graph, sccs, iterator.next()).run();
		}
		final Set<Set<GraphNode>> result = new HashSet<>(sccs.size());
		for (GraphNode graphNode : sccs.values()) {
			result.add(graphNode.getSCC());
		}
		testMediumSCCs(graph, result);
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
	@Test
	public void testLarge() throws IOException {
		final Graph graph = new Graph("testLarge");
		readFile(graph, "/data/largeDG.txt");

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(25, sccs.size());
		
		final Set<Set<Integer>> convertedSCCs = convertToInts(sccs);
		
		// Read the file with the correct SCCs
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream("/data/largeDGsccs.txt");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				final Set<Integer> hashSet = new HashSet<Integer>();
				final String[] ints = line.trim().split("\\s+");
				for (String string : ints) {
					hashSet.add(Integer.parseInt(string));
				}
				Assert.assertTrue(convertedSCCs.contains(hashSet));
			}
		}
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

	// This implementation makes heavy use of multi-threading and is considerably faster on multi-core machines.
	private static void readBinFile(Graph graph, String filename) throws IOException, InterruptedException {
		// Read the inputstream into a byte buffer all at once. This provides good performance because
		// we don't call read() repeatedly.
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream(filename);
		final DataInputStream dis = new DataInputStream(in);
		final byte[] buf = new byte[dis.available()];
		dis.readFully(buf);
		dis.close();
		
		// Create an executor with as many threads as available on the system.
		final int nThreads = Runtime.getRuntime().availableProcessors();
		final ExecutorService executor = Executors.newFixedThreadPool(nThreads);

		// Wrap the byte buffer as an int buffer.
		final IntBuffer intBuf = ByteBuffer.wrap(buf).asIntBuffer();

		// Partition the work among the threads 
		final int partition = intBuf.capacity() / nThreads;
		
		for (int i = 0; i < nThreads; i++) {
			final int start= i * partition;
			// The thread responsible for the last partition will read until the
			// end of the buffer in case partition has a remainder.
			final int end = i == (nThreads -1) ? intBuf.capacity() : (i + 1) * partition;

			executor.execute(new Runnable() {
				@Override
				public void run() {
					int s = start;
					if (s > 0 && intBuf.get(s-1) != Integer.MIN_VALUE) {
						// Records (node and its out arcs) in the buffer are of
						// variable length and terminated by an end marker
						// (MIN_VALUE).
						// Advance/align to the first end marker in this
						// partition unless we are at the beginning of a p. or
						// we are right at the beginning of a record. The reader
						// of the previous partition will read into our
						// partition if necessary.
						while ((intBuf.get(s++) != Integer.MIN_VALUE)) {
							; // no-op
						}
					}
					// TODO Implement collecting n graphnodes into an
					// intermediate collection to append the collection to graph
					// => less contention on graph#put.
					while (s < end) {
						// Create a new node instance
						final int nodeId = intBuf.get(s++);
						final GraphNode graphNode = new GraphNode(nodeId);

						// Read the node's arcs
						int arcId;
						final LinkedList<Integer> arcs = new LinkedList<Integer>();
						while ((arcId = intBuf.get(s++)) != Integer.MIN_VALUE) {
							arcs.add(arcId);
						}
						graphNode.setArcs(arcs);
						
						// Add the node to the graph
						graph.put(nodeId, graphNode);
					}
				}
			});
		}
		
		// Wait for the threads to do their work.
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}

	private static void readFile(Graph graph, String filename) throws IOException {
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream(filename);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				final String[] split = line.trim().split("\\s+");
				final int nodeId = Integer.parseInt(split[0]);
				final int arcId = Integer.parseInt(split[1]);

				// set up the from node
				if (graph.hasNode(nodeId)) {
					graph.addArc(nodeId, arcId);
				} else {
					graph.addNode(new GraphNode(nodeId), arcId);
				}
				
				// Also create the to node (arcId) in case it's not explicitly
				// created in a test graph.
				if (!graph.hasNode(arcId)) {
					graph.addNode(new GraphNode(arcId));
				}
			}
		}
	}
	
	private static void readInits(Graph graph, String filename) throws IOException {
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream(filename);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				final int nodeId = Integer.parseInt(line.trim());
				graph.setInit(nodeId);
			}
		}
	}
}
