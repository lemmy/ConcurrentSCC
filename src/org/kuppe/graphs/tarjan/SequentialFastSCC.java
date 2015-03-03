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
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class SequentialFastSCC {

	private final Stack<GraphNode> path = new Stack<GraphNode>();

	public Set<Stack<GraphNode>> searchSCCs(final List<GraphNode> roots) {
		final Iterator<GraphNode> itr = roots.iterator();
		while (itr.hasNext()) {
			final GraphNode next = itr.next();
			if (next.getVisited() != Visited.POST) {
				path.push(next);
				dfs();
			}
		}
		
		// Print nodes with contracted graph nodes!
		final Set<Stack<GraphNode>> result = new HashSet<Stack<GraphNode>>();
		for (GraphNode graphNode : roots) {
			final Stack<GraphNode> scc = graphNode.getContracted();
			if (!scc.isEmpty()) {
				result.add(scc);
			}
		}
		return result;
	}

	private void dfs() {
		final GraphNode node = path.peek();
		// Do not search a POST-visited node
		if (node.getVisited() == Visited.POST) {
			return;
		}

		// The general step is to traverse the next arc out of the last vertex
		// on the path:
		final Set<GraphNode> exploredArcs = new HashSet<GraphNode>();
		GraphNode successor;
		while ((successor = node.getUnvisitedSuccessor(exploredArcs)) != null) {
			exploredArcs.add(successor);
			// If this arc leads to a new vertex:
			if (successor.getVisited() == Visited.UN) {
				// The path is extended by one vertex
				path.push(successor);
				// The new vertex becomes previsited
				successor.setVisited(Visited.PRE);
				// The search continues from the new vertex.
				dfs();
			// If the arc leads to a "previsited" vertex (a vertex on the
			// search path):
			} else if (successor.getVisited() == Visited.PRE) {
				// A cycle has been found: This cycle is contracted into the
				// last vertex on the search path (forming a strongly connected
				// subgraph but not necessarily a maximal one) and the search
				// continues.
				final GraphNode last = path.peek();

				// Contract both GraphNodes into last GraphNode instance
				last.contract(successor);

				// Contract both GraphNodes on the path into last
				contractPath(last, successor);

			// If the arc leads to a "postvisited" vertex:
//			} else if (successor.getVisited() == Visited.POST) {
//				// Each postvisited vertex is a complete strong component.
//				System.out.println(String.format("SCC found: %s", successor));
//				// The search moves on to the next arc out of the current
//				// vertex.
			}
		}

		// When there are no more arcs to traverse out of the current vertex:

		// The current vertex becomes postvisited (it is a finished component)
		node.setVisited(Visited.POST);

		dfs();
	}

	private void contractPath(final GraphNode last, final GraphNode successor) {
		// Contract (combine) two to n identical elements
		final ListIterator<GraphNode> itr = path.listIterator();
		GraphNode pre = null;
		while (itr.hasNext()) {
			final GraphNode n = itr.next();
			if (n == successor) {
				if (pre == n) {
					itr.remove();
				} else {
					itr.remove();
					itr.add(last);
				}
			}
			pre = n;
		}
	}
}
