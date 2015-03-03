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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GraphNode implements Comparable<GraphNode> {

	public enum Visited {
		UN, PRE, POST
	};

	private final Set<GraphNode> successors = new TreeSet<GraphNode>();
	private final Set<GraphNode> contracted = new HashSet<GraphNode>();
	private final String id;

	private Visited visited = Visited.UN;

	public GraphNode(final String anId) {
		this.id = anId;
	}
	
	public String getId() {
		return id;
	}

	public Visited getVisited() {
		return visited;
	}

	public void setVisited(Visited visited) {
		this.visited = visited;
	}

	public void addSuccessor(GraphNode aGraphNode) {
		successors.add(aGraphNode);
	}

	/**
	 * The contraction of a pair of vertices v_i and v_j of a graph produces a
	 * graph in which the two nodes v_1 and v_2 are replaced with a single node
	 * v such that v is adjacent to the union of the nodes to which v_1 and v_2
	 * were originally adjacent.
	 * 
	 * @param aNode
	 */
	public void contract(final GraphNode aNode, final List<GraphNode> graph) {
		// Do not contract the node if it's the same instance. Otherwise we
		// destroy the node logically.
		if (this == aNode) {
			return;
		}
		
		// Instead of creating a new GraphNode, this becomes technically the new
		// node. aNode is removed from the graph and will eventually be garbage
		// collected.
		
		// Union of all successors
		aNode.successors.remove(this);
		this.successors.addAll(aNode.successors);
		
		// aNode is now no longer connected in the graph. To allow garbage
		// collection to do its job, also clear successors.
		aNode.successors.clear();
		
		contracted.addAll(aNode.contracted);
		aNode.contracted.clear();
		contracted.add(aNode);
		
		// Replace all occurrences of aNode with this
		//TODO This is obviously terrible
		for (GraphNode graphNode : graph) {
			if(graphNode.successors.remove(aNode)) {
				graphNode.successors.add(this);
			}
		}
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

	public Set<GraphNode> getContracted() {
		if (contracted.isEmpty()) {
			// The current vertex has *not* been contracted
			return contracted;
		}
		contracted.add(this);
		return contracted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphNode other = (GraphNode) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public int compareTo(GraphNode o) {
		return id.compareTo(o.id);
	}
}
