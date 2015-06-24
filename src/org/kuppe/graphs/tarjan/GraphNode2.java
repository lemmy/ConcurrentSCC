///*******************************************************************************
// * Copyright (c) 2015 Microsoft Research. All rights reserved. 
// *
// * The MIT License (MIT)
// * 
// * Permission is hereby granted, free of charge, to any person obtaining a copy 
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
// * of the Software, and to permit persons to whom the Software is furnished to do
// * so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software. 
// * 
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
// * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// *
// * Contributors:
// *   Markus Alexander Kuppe - initial API and implementation
// ******************************************************************************/
//
//package org.kuppe.graphs.tarjan;
//
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.TreeSet;
//
//import edu.cmu.cs.LinkCut;
//import edu.cmu.cs.LinkCutTreeNode;
//
//public class GraphNode2 extends LinkCutTreeNode {
//
//	/**
//	 * The set of visited states a GraphNode can have. 
//	 */
//	public enum Visited {
//		// This also constitutes an order (see ordinal)
//		UN, PRE, POST;
//	};
//
//	// TODO Do we need a stack here? A set will do to check liveness as by
//	// definition of SCC, each vertex can reach every other one.
//	/**
//	 * 
//	 */
//	private final Set<GraphNode2> scc = new TreeSet<GraphNode2>(new Comparator<GraphNode2>() {
//		public int compare(GraphNode2 o1, GraphNode2 o2) {
//			// want a stable order of the nodes inside the SCC for the moment
//			// (to better test if the correct SCC has been found)
//			return Integer.compare(o1.id, o2.id);
//		}
//	});
//	
//	/**
//	 * The set of successors in the *graph*. This is orthogonal to the
//	 * LinkCutTree.
//	 */
//	private final Set<Arc> successors = new HashSet<Arc>();
//
//	/**
//	 * The nodes id. No to nodes must have the same id.
//	 */
//	private final int id;
//
//	/**
//	 * The visited state of this node
//	 */
//	private Visited visited = Visited.UN;
//	
//	/**
//	 * If this node has been contracted into another one, this field will keep a
//	 * reference to the one this has been contracted into.
//	 */
//	public GraphNode2 contracted = this;
//
//	public GraphNode2(final int anId) {
//		this.id = anId;
//	}
//
//	public boolean is(Visited v) {
//		return contracted.visited == v;
//	}
//
//	public boolean isNot(Visited v) {
//		return !is(v);
//	}
//
//	public void set(Visited visited) {
//		// Only state changes from UN > PRE > POST are allowed
//		assert contracted.visited.ordinal() <= visited.ordinal();
//		contracted.visited = visited;
//	}
//
//	/**
//	 * Create an arc between this node an the given {@link GraphNode2} in the
//	 * graph.
//	 * 
//	 * @param aGraphNode
//	 */
//	public void createArc(final GraphNode2 aGraphNode) {
//		contracted.successors.add(new Arc(this, aGraphNode));
//	}
//
//	/**
//	 * @return The the set of graph arcs
//	 */
//	public Set<Arc> getSuccessor() {
//		return contracted.successors;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		return "GraphNode [id=" + id
//				+ ", visited=" + visited
//				+ "]";
//	}
//
//	public void setParent(GraphNode2 parent) {
//		LinkCut.link(this, parent);
//	}
//
//	public boolean isInSameTree(GraphNode2 other) {
//		return LinkCut.root(this) == LinkCut.root(other);
//	}
//
//	public void contract(Set<Set<GraphNode2>> sccs, GraphNode2 graphNode) {
//		scc.add(this);
//
//		// "merge" successor's outgoing arcs into the current set of arcs.
//		// Do the same merge for all of successor's parents
//		// TODO merge w into v too or does it remain a root?
//
//		GraphNode2 parent = graphNode;
//		while (parent != null && parent != this) {
//			if (!parent.scc.isEmpty()) {
//				// If parent is also contracted, we have to merge it's
//				// contraction into us. It is an SCC, but we have found a larger
//				// one that contains the smaller.
//				sccs.remove(parent.scc);
//				scc.addAll(parent.scc);
//			} else {
//				// Add parent to stack of contracted (SCC)
//				scc.add(parent);
//			}
//
//			// Merge parent's untraversed outgoing arcs into ours
//			for (Arc arc : parent.getSuccessor()) {
//				if (!arc.isTraversed() && !this.successors.contains(arc)) {
//					this.successors.add(arc);
//				}
//			}
//
//			// Exclude parent from further processing
//			parent.visited = Visited.POST;
//			
//			// Continue with parent's parent (up to the root which is indicated
//			// by null)
//			GraphNode2 newParent = (GraphNode2) parent.p;
//
//			// Redirect any invocations on contracted node to us. Other
//			// GraphNode instances still have arcs pointing to parent.
//			parent.contracted = this;
//
//			parent = newParent;
//		}
//
//		// TODO Visited state of new contracted node (this) UN or PRE?
//		this.visited = Visited.UN;
//
//		// TODO If the addition into 'sccs' is done outside of the critical
//		// section,
//		// one thread might wait for a very long time. It then adds an 'scc'
//		// into 'sccs' that has been contracted into a larger one already?!
//		// However, that would imply, that only real SCCs are ever inserted
//		// which doesn't seem to be the case.
//
//		// Add to SCCs
//		sccs.add(scc);
//	}
//
//	public boolean checkSCC() {
//		assert!contracted.scc.isEmpty();
//		// TODO check liveness here
//		return true;
//	}
//}
