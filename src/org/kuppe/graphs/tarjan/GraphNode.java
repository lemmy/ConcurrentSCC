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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.cmu.cs.LinkCut;
import edu.cmu.cs.LinkCutTreeNode;

public class GraphNode extends LinkCutTreeNode {

	public enum Visited {
		// This also constitutes an order (see ordinal)
		UN, POST;
	};
	
	private Graph graph;
	public volatile Visited visited = Visited.UN;

	public GraphNode(final int anId) {
		super(anId);
	}

	public GraphNode(int id, Graph graph) {
		this(id);
		this.graph = graph;
	}

	void setGraph(Graph graph) {
		this.graph = graph;
	}

	public boolean is(Visited v) {
		return visited == v;
	}
	
	public boolean isNot(Visited v) {
		return !is(v);
	}

	public void set(Visited visited) {
		// Only state changes from UN > PRE > POST are allowed
		assert this.visited.ordinal() <= visited.ordinal();
		
		// When a GraphNode transitions into post, all its arcs have to be
		// explored.
		assert visited != Visited.POST || !graph.hasUntraversedArc(this);

		this.visited = visited;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isRoot()) {
			return "GN [id=" + id
					+ ", visited=" + visited
					+ ", ROOT"
					+ "]";
		}
		return "GN [id=" + id
				+ ", visited=" + visited
				+ ", CHILD"
				+ "]";
	}

	public void setParent(GraphNode parent) {
		assert this.isNot(Visited.POST);
		LinkCut.link(this, parent);
	}

	public boolean isInSameTree(GraphNode other) {
		return LinkCut.root(this) == LinkCut.root(other);
	}
		
	public void contract(final Map<GraphNode, Set<GraphNode>> sccs, final Graph graph, final GraphNode graphNode) {
		// We have to be a root in the tree...
		assert this.isRoot();
		// ...and the other has to be in our tree
		assert LinkCut.root(graphNode) == this;

		// Get the subset SCCs (if any) which has been contracted into this
		// before.
		Set<GraphNode> scc = sccs.get(this);
		if (scc == null) {
			// No previous scc for this node
			scc = getNewScc();
			scc.add(this);
			sccs.put(this, scc);
		}

		
		// Traverse GraphNode's tree up to the root which is us/this.
		GraphNode parent = graphNode;
		while (parent != this) {
			// Merge the others subset scc into this new one and remove it
			// from the set of sccs.
			final Set<GraphNode> parentsSubset = sccs.remove(parent);
			if (parentsSubset != null) {
				// Correct all 'id to node' mappings for the previous contracted
				// nodes. Otherwise, if an arc is later explored
				// going to one node in parentsSubset "t", it will be skipped as
				// "t" is post-visited. It has to be pre-visited though, which
				// is this' visited state after contraction.
				for (GraphNode s : parentsSubset) {
					// s' mapping will be updated down below
					if (s != parent) {
						graph.contract(this, s);
					}
				}
				scc.addAll(parentsSubset);
			} else {
				scc.add(parent);
			}

			assert parent.isNot(Visited.POST);
			// This should be the only place where visited is accessed directly
			// (except to its setter). It is done, to skip the pre-condition,
			// that all of parents outgoing arcs are traversed. Here we merge
			// all unprocessed outgoing arcs into this node.
			parent.visited = Visited.POST;

			// Logically replace parent with this GraphNode in the Graph.
			graph.contract(this, parent);
			// parent's arcs should have been contracted into this now.
			assert !graph.hasUntraversedArc(parent);
			
			// Before unlink/cut, remember parent's parent
			GraphNode parentsParent = (GraphNode) LinkCut.parent(parent);

			LinkCut.cut(parent);
			assert parent.isRoot();
			
			// Take copy of parent.children. parent.children is modified by
			// LinkCut.cut/LinkCut.link which results in a
			// ConcurrentModificationException otherwise.
			final Set<LinkCutTreeNode> children = new HashSet<>(parent.children);
			
			// Unlink/Cut children of parent from parent and link them to
			// us/this node.
			// E.g. for test B when {2,1} form a contraction and tree being:
			// {2,1} < 3 exploring the arc {2,3} has to trigger compaction
			// of 3 into 2. But when only cut is done without linking to
			// this, the previous compaction will have cut 3 loose already.
			for (LinkCutTreeNode child : children) {
				// This while loop is walking a path from a child to its root
				// and it obviously has to skip linking the nodes on the path to
				// the root again. This for loop here is so that childs *not on
				// the path* are linked to the root.
				if (!scc.contains(child)) {
					LinkCut.cut(child);
					LinkCut.link(child, this);
				}
			}
			// Now that the children of parent *who are not on this path* are
			// linked to the root (this), clear the last remaining child (if
			// any) which is the one on the path
			assert parent.children.isEmpty() || parent.children.size() == 1;
			parent.children.clear();
			//TODO should be safe to null parent.children
			
			// Continue with parent's parent.
			parent = parentsParent;
		}

		// We remain a root in the tree.
		assert this.isRoot();
		// Must not be POST-visited now
		assert this.isNot(Visited.POST);
	}
	
	private Set<GraphNode> getNewScc() {
		// TODO Do we need a stack here? A set will do to check liveness as by
		// definition of SCC, each vertex can reach every other one.
		final Set<GraphNode> scc = new TreeSet<GraphNode>(new Comparator<GraphNode>() {
			public int compare(GraphNode o1, GraphNode o2) {
				// want a stable order of the nodes inside the SCC for the moment
				// (to better test if the correct SCC has been found)
				return Integer.compare(o1.id, o2.id);
			}
		});
		return scc;
	}

	public boolean checkSCC() {
		//TODO check liveness here
		return true;
	}

	public int getId() {
		return id;
	}

	public boolean isRoot() {
		return LinkCut.root(this) == this;
	}

	/**
	 * Cuts off the direct tree children. 
	 */
	public Set<GraphNode> cutChildren() {
		// CUT EACH CHILD FROM ITS PARENT
		final Set<GraphNode> children = new HashSet<GraphNode>();
		for (LinkCutTreeNode linkCutTreeNode : LinkCut.directChildren(this, new HashSet<LinkCutTreeNode>())) {
			GraphNode child = (GraphNode) linkCutTreeNode;
			if (child.isNot(Visited.POST)) {
				children.add(child);
			}
			LinkCut.cut(child);
		}
		return children;
	}

	public Set<LinkCutTreeNode> getChildren() {
		return this.children;
	}
}
