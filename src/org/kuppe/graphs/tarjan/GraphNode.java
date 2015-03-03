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

import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class GraphNode implements Comparable<GraphNode> {

	public enum Visited {
		UN, PRE, POST
	};

	private final Set<GraphNode> successors = new TreeSet<GraphNode>();
	private final Set<GraphNode> predecessors = new TreeSet<GraphNode>();
	private final Stack<GraphNode> contracted = new Stack<GraphNode>();
	private final String id;

	private Visited visited = Visited.UN;

	public GraphNode(final String anId) {
		this.id = anId;
	}

	public Visited getVisited() {
		return visited;
	}

	public void setVisited(Visited visited) {
		this.visited = visited;
	}

	public void addEdge(final GraphNode aGraphNode) {
		successors.add(aGraphNode);
		aGraphNode.predecessors.add(this);
	}

	/**
	 * The contraction of a pair of vertices v_i and v_j of a graph produces a
	 * graph in which the two nodes v_1 and v_2 are replaced with a single node
	 * v such that v is adjacent to the union of the nodes to which v_1 and v_2
	 * were originally adjacent.
	 * 
	 * @param aNode
	 */
	public void contract(final GraphNode aNode) {
		// A node cannot be contracted with itself.
		if (this == aNode) {
			return;
		}
		// No need to contract the same two nodes twice.
		if (contracted.contains(aNode)) {
			return;
		}
		
		// Replace aNode with this in aNode's predecessors. But only if aNode's predecessor
		// indeed has aNode as a successor. aNode is *not* successor, if the predecessor
		// itself has already been contracted.
		for (final GraphNode aPredecessor : aNode.predecessors) {
			if (aPredecessor.successors.remove(aNode)) {
				aPredecessor.successors.add(this);
			}
		}
		aNode.predecessors.clear();
		
		// Union of all successors
		aNode.successors.remove(this);
		this.successors.addAll(aNode.successors);
		aNode.successors.clear();

		// Union of contracted but don't add duplicates
		for (GraphNode graphNode : aNode.contracted) {
			if (!contracted.contains(graphNode)) {
				contracted.add(graphNode);
			}
		}
		contracted.add(aNode);
		aNode.contracted.clear();
	}

	@Override
	public String toString() {
		return "GraphNode [anId=" + id + ", visited=" + visited + "]";
	}
	
	// Deterministically
	public GraphNode getUnvisitedSuccessor(final Set<GraphNode> explored) {
		final Set<GraphNode> temp = new TreeSet<GraphNode>(this.successors);
		temp.removeAll(explored);
		if (temp.isEmpty()) {
			return null;
		} else {
			return temp.toArray(new GraphNode[temp.size()])[0];
		}
	}

	public Stack<GraphNode> getContracted() {
		if (contracted.isEmpty()) {
			// The current vertex has *not* been contracted
			return contracted;
		}
		contracted.add(this);
		return contracted;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(GraphNode o) {
		return id.compareTo(o.id);
	}
}
