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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.kuppe.graphs.tarjan.GraphNode.Visited;

public class SequentialFastSCC {

//	public static void main(String[] args) {
//		final List<GraphNode> roots = new ArrayList<GraphNode>();
//
//		final GraphNode one = new GraphNode("1");
//		roots.add(one);
//		final GraphNode two = new GraphNode("2");
//		roots.add(two);
//		final GraphNode three = new GraphNode("3");
//		roots.add(three);
//		final GraphNode four = new GraphNode("4");
//		roots.add(four);
//
//		one.addSuccessor(two);
//		one.addSuccessor(one);
//
//		two.addSuccessor(one);
//		two.addSuccessor(three);
//
//		three.addSuccessor(four);
//
//		four.addSuccessor(three);
//
//		final SequentialFastSCC sequentialFastSCC = new SequentialFastSCC();
//		sequentialFastSCC.searchSCCs(roots);
//	}

	// private final LinkedList<GraphNode> path = new LinkedList<GraphNode>();
	private final Stack<GraphNode> path = new Stack<GraphNode>();

	public Set<Set<GraphNode>> searchSCCs(final List<GraphNode> roots) {
		final Iterator<GraphNode> itr = roots.iterator();
		while (itr.hasNext()) {
			final GraphNode next = itr.next();
			if (next.getVisited() != Visited.POST) {
				path.push(next);
				dfs(0, roots);
			}
		}

		// At this point all elements in roots are post-visited
		for (GraphNode graphNode : roots) {
			assert graphNode.getVisited() == Visited.POST;
		}
		
		// Print nodes with contracted graph nodes.
		// These are the strongly connected components?!
		final Set<Set<GraphNode>> result = new HashSet<Set<GraphNode>>();
		for (GraphNode graphNode : roots) {
			final Set<GraphNode> scc = graphNode.getContracted();
			if (!scc.isEmpty()) {
				result.add(scc);
			}
		}
		return result;
	}

	private void dfs(final int level, final List<GraphNode> graph) {
		if (path.isEmpty()) {
//			System.out.println("Ending dfs due to empty path");
			return;
		}
		final GraphNode node = path.peek();
		// Do not search a POST-visited node
		if (node.getVisited() == Visited.POST) {
//			System.out.println(String.format("Ending dfs due to post-visited node %s", node));
			return;
		}

		System.out.println(String.format("dfs(%s) on %s and path %s", level, node, path));

		// The general step is to traverse the next arc out of the last vertex
		// on the path:
		// final ListIterator<GraphNode> iterator = node.iterator();
		final Set<GraphNode> exploredArcs = new HashSet<GraphNode>();
		GraphNode successor;
		while ((successor = node.getUnvisitedSuccessor(exploredArcs)) != null) {
			exploredArcs.add(successor);
			// final GraphNode successor = iterator.next();
//			System.out.println(String.format("Successor of %s is %s", node, successor));
			// If this arc leads to a new vertex:
			if (successor.getVisited() == Visited.UN) {
				// The path is extended by one vertex
				path.push(successor);
				// The new vertex becomes previsited
				successor.setVisited(Visited.PRE);
				// The search continues from the new vertex.
				dfs(level + 1, graph);
				// If the arc leads to a "previsited" vertex (a vertex on the
				// search path):
			} else if (successor.getVisited() == Visited.PRE && !path.isEmpty()) {
				// A cycle has been found: This cycle is contracted into the
				// last vertex on the search path (forming a strongly connected
				// subgraph but not necessarily a maximal one) and the search
				// continues.
				final GraphNode last = path.peek();

				// Contract both GraphNodes into last GraphNode instance
				last.contract(successor, graph);

				// Contract both GraphNodes on the path into last
				contractPath(last, successor);

//				System.out.println(String.format("Contracting %s into %s", successor, last));
				// If the arc leads to a "postvisited" vertex:
//			} else if (successor.getVisited() == Visited.POST) {
//				// Each postvisited vertex is a complete strong component.
//				System.err.println(String.format("SCC found: %s", successor));
//				// The search moves on to the next arc out of the current
//				// vertex.
			}
		}

		// When there are no more arcs to traverse out of the current vertex:

		// The current vertex becomes postvisited (it is a finished component)
		node.setVisited(Visited.POST);

		// The previous vertex on the search path becomes the current vertex.
		// final int idx = path.lastIndexOf(node);
		// if (idx > 0) {
		// final GraphNode predecessor = path.get(idx - 1);
		// if (!path.isEmpty()) {
		// dfs(path.pop());
		// }
		dfs(level + 1, graph);
	}

	private void contractPath(final GraphNode last, final GraphNode successor) {
		// Replace all occurrences of successor in path with last
		Collections.replaceAll(path, successor, last);
		// Contract (combine) two to n identical elements
		final Iterator<GraphNode> itr = path.iterator();
		GraphNode pre = null;
		while (itr.hasNext()) {
			final GraphNode n = itr.next();
			if (pre == n) {
				itr.remove();
			}
			pre = n;
		}
	}
}
