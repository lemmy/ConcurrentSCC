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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ConcurrentFastSCCTestFromFile {

	@Test
	public void test() throws IOException {
		final Graph graph = new Graph();

		/*
		 * tinyDG.txt has 3 components
		 * largeDG.txt has 25 components
		 */
		
		final InputStream in = this.getClass().getResourceAsStream("tinyDG.txt");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				String[] split = line.trim().split("\\s+");
				int nodeId = Integer.parseInt(split[0]);
				int arcId = Integer.parseInt(split[1]);
				
				graph.get(nodeId);
				graph.addArc(nodeId, arcId);
			}
		}

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertEquals(3, sccs.size());
	}
}
