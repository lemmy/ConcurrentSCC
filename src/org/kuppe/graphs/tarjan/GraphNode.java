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
import java.util.Set;
import java.util.Stack;

public class GraphNode {

	public enum Visited {
		// This also constitutes an order (see ordinal)
		UN, PRE, POST;
	};

	// TODO Do we need a stack here? A set will do to check liveness as by
	// definition of SCC, each vertex can reach every other one.
	private final Stack<GraphNode> contractedInto = new Stack<GraphNode>();
	private final Set<Arc> successors = new HashSet<Arc>();
	
	private final String id;
	
	private Visited visited = Visited.UN;
	private GraphNode parent;
	private boolean contracted = false;

	public GraphNode(final String anId) {
		this.id = anId;
	}
	
	public GraphNode(final int anId) {
		this(Integer.toString(anId));
	}

	public boolean is(Visited v) {
		return visited == v;
	}
	
	public boolean isNot(Visited v) {
		return !is(v);
	}

	public void set(Visited visited) {
		assert !contracted;
		// Only state changes from UN > PRE > POST are allowed
		assert this.visited.ordinal() <= visited.ordinal();
		this.visited = visited;
	}

	public void addEdge(final GraphNode aGraphNode) {
		successors.add(new Arc(this, aGraphNode));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GraphNode [id=" + id
				+ ", visited=" + visited
				+ "]";
	}

	public Set<Arc> getSuccessor() {
		assert !contracted;
		return successors;
	}

	public void setParent(GraphNode parent) {
		assert !contracted;
		this.parent = parent;
	}

	/**
	 * Two nodes are said to be in the same tree, iff there is a path from this
	 * {@link GraphNode} to the other via the ancestors. In other words, this is
	 * an ancestor of other.
	 * <p>
	 * The implementation is O(n). It traverses the ancestor tree up to the root
	 * where GraphNode#parent is null).
	 * <p>
	 * TODO This should use a more efficient data structure, like an Euler-tour-tree.
	 * 
	 * @param other
	 * @return true iff there exists a path from this to other
	 */
	public boolean isInSameTree(GraphNode other) {
		// Assert this node hasn't been contracted into another node
		assert !contracted;
		
		// TODO Do this for this and other and compare the root for equality. An
		// optimization is to compare the element on the path. If other is on
		// the path of this (or vice-versa), both are obviously in the same tree

		// Recursively traverse all ancestors to the root
		if (this.equals(other)) {
			return true;
		}
		if (parent == null) {
			return false;
		}
		return parent.isInSameTree(other);
	}

	public Stack<GraphNode> contract(GraphNode graphNode) {
		// "merge" successor's outgoing arcs into the current set of arcs.
		// Do the same merge for all of successor's parents
		//TODO merge w into v too or does it remain a root?

		GraphNode parent = graphNode;
		while (parent != null) {
			
			// Add parent to stack of contracted (SCC)
			contractedInto.push(parent);
			
			// Special care needs to be taken if we are about to merge ourself.
			// Since we will represent the new contracted node, our visited
			// state must *not* change.
			//TODO Visited state of new node UN or PRE? 
			if (parent != this) {

				// Merge parent's untraversed outgoing arcs into ours
				for (Arc arc : parent.getSuccessor()) {
					if (!arc.isTraversed()) {
						this.successors.add(arc);
					}
				}
				
				// Exclude parent from further processing
				parent.visited = Visited.POST;
				parent.contracted = true;
			}
			
			// Continue with parent's parent (up to the root which is indicated
			// by null)
			parent = parent.parent;
		}
		
		return contractedInto;
	}
//	
//	private void mergeUnvisistedArcs(final Collection<GraphNode> successors, final GraphNode successor) {
//		Set<GraphNode> successorSuccessors = successor.getSuccessor();
//		for (GraphNode graphNode : successorSuccessors) {
//			if (graphNode.isNot(Visited.POST)) {
//				successors.add(graphNode);
//			}
//		}
//	}

	public boolean checkSCC() {
		assert !contractedInto.isEmpty();
		//TODO check liveness here
		return true;
	}
}
