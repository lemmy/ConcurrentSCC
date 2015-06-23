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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class GraphNode {

	public enum Visited {
		// This also constitutes an order (see ordinal)
		UN, PRE, POST;
	};

	// TODO Do we need a stack here? A set will do to check liveness as by
	// definition of SCC, each vertex can reach every other one.
	private final Set<GraphNode> contractedInto = new TreeSet<GraphNode>(new Comparator<GraphNode>() {
		public int compare(GraphNode o1, GraphNode o2) {
			// want a stable order of the nodes inside the SCC for the moment
			// (to better test if the correct SCC has been found)
			return Integer.compare(o1.id, o2.id);
		}
	});
	private final Set<Arc> successors = new HashSet<Arc>();
	
	private final int id;
	
	private Visited visited = Visited.UN;
	private GraphNode parent;
	public GraphNode contracted = this;

	public GraphNode(final int anId) {
		this.id = anId;
	}

	public boolean is(Visited v) {
		return contracted.visited == v;
	}
	
	public boolean isNot(Visited v) {
		return !is(v);
	}

	public void set(Visited visited) {
		// Only state changes from UN > PRE > POST are allowed
		assert contracted.visited.ordinal() <= visited.ordinal();
		contracted.visited = visited;
	}

	public void addEdge(final GraphNode aGraphNode) {
		contracted.successors.add(new Arc(this, aGraphNode));
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
		return contracted.successors;
		}

	public void setParent(GraphNode parent) {
		contracted.parent = parent;
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
		
		// TODO Do this for this and other and compare the root for equality. An
		// optimization is to compare the element on the path. If other is on
		// the path of this (or vice-versa), both are obviously in the same tree
		
		

		// Recursively traverse all ancestors to the root
		if (contracted.equals(other)) {
			return true;
		}
		if (contracted.parent == null) {
			return false;
		}
		if (contracted.parent.isInSameTree(other)) {
			return true;
		}
		return root(contracted).equals(root(other));
	}

	private GraphNode root(GraphNode node) {
		GraphNode parent = node.parent;
		while (parent != null) {
			parent = parent.parent;
		}
		return parent;
	}

	public void contract(Set<Set<GraphNode>> sccs, GraphNode graphNode) {
		// Must add ourself to the SCC
		contractedInto.add(this);
		
		// "merge" successor's outgoing arcs into the current set of arcs.
		// Do the same merge for all of successor's parents
		//TODO merge w into v too or does it remain a root?

		GraphNode parent = graphNode;
		while (parent != null) {
			// Special care needs to be taken if we are about to merge ourself.
			// Since we will represent the new contracted node, our visited
			// state must *not* change.
			//TODO Visited state of new node UN or PRE? 
			if (parent != this) {
				
				if (!parent.contractedInto.isEmpty()) {
					// If parent is also contracted, we have to merge it's
					// contraction into us. It is an SCC, but we have found a larger
					// one that contains the smaller.
					sccs.remove(parent.contractedInto);
					contractedInto.addAll(parent.contractedInto);
				}
				// Add parent to stack of contracted (SCC)
				contractedInto.add(parent);

				// Merge parent's untraversed outgoing arcs into ours
				for (Arc arc : parent.getSuccessor()) {
				if (!arc.isTraversed() && !this.successors.contains(arc)) {
						this.successors.add(arc);
					}
				}
				
				// Exclude parent from further processing
				parent.visited = Visited.POST;
				parent.contracted = this;
			}
			
			// Continue with parent's parent (up to the root which is indicated
			// by null)
			parent = parent.parent;
		}
		
		// Add to SCCs
		sccs.add(contractedInto);
	}

	public boolean checkSCC() {
		assert !contracted.contractedInto.isEmpty();
		//TODO check liveness here
		return true;
	}
}
