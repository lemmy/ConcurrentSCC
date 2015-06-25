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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import edu.cmu.cs.LinkCut;
import edu.cmu.cs.LinkCutTreeNode;

public class GraphNode extends LinkCutTreeNode {

	public enum Visited {
		// This also constitutes an order (see ordinal)
		UN, PRE, POST;
	};
	
	private volatile Visited visited = Visited.UN;

	private GraphNode representedTreeParent;
	
	public GraphNode(final int anId) {
		super(anId);
	}

	public boolean is(Visited v) {
		return visited == v;
	}
	
	public boolean isNot(Visited v) {
		return !is(v);
	}

	public void set(Visited visited) {
		// Only state changes from UN > PRE > POST are allowed
		assert visited.ordinal() <= visited.ordinal();
		this.visited = visited;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (representedTreeParent != null) {
			return "GN [id=" + id
					+ ", visited=" + visited
					+ ", parent=" + representedTreeParent
					+ "]";
		}
		return "GN [id=" + id
				+ ", visited=" + visited
				+ ", ROOT"
				+ "]";
	}

	public void setParent(GraphNode parent) {
		representedTreeParent = parent;
		LinkCut.link(this, parent);
	}

	public boolean isInSameTree(GraphNode other) {
		return LinkCut.root(this) == LinkCut.root(other);
	}
		
	public void contract(final Map<GraphNode, Set<GraphNode>> sccs, final Graph graph, final GraphNode graphNode) {
		try {
			assert isRoot();
			
			assert LinkCut.root(graphNode) == this;
			
			final Set<GraphNode> scc = getNewScc();
			
			// Traverse the tree up to the root
			GraphNode parent = graphNode;
			while(parent != this) {
				// Merge the others subset scc into this new one and remove it from
				// the set of sccs.
				final Set<GraphNode> subset = sccs.remove(parent);
				if (subset != null) {
					scc.addAll(subset);
				} else {
					scc.add(parent);
				}
				
				// Mark parent done
				parent.visited = Visited.POST;
				
				// Logically replace parent with this GraphNode in the Graph.
				graph.contract(this, parent);
				
				// Before unlink/cut, remember parent's parent
				GraphNode parentsParent = (GraphNode) LinkCut.parent(parent);
				assert parentsParent == parent.representedTreeParent;
//				GraphNode parentsParent = parent.representedTreeParent;
				
				// Unlink parent from its tree (this might not be necessary as the
				// whole tree including the root will be logically removed from the
				// forest.
				
				// Don't unlink parent even though it's contracted. It potentially has
				// children that would loose their tree membership otherwise.
//			LinkCut.cut(parent);

				
				parent.children.forEach((child) -> {LinkCut.cut(child);});
				
				// Continue with parent's parent.
				parent = parentsParent;
			}
			
			scc.add(this);
			
			// Get the subset SCCs (if any) which has been contracted into this before.
			final Set<GraphNode> subset = sccs.get(this);
			if (subset != null) {
				scc.addAll(subset);
			}
			
			sccs.put(this, scc);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public Integer getId() {
		return id;
	}

	public boolean isRoot() {
		final LinkCutTreeNode root = LinkCut.root(this);
		return root == this;
	}

	public Set<GraphNode> getChildren() {
		// TODO Cannot implement this method for link/cut tree and thus just
		// return an empty set to avoid NPEs.
		Collection<LinkCutTreeNode> lctnChildren = LinkCut.directChildren(this, new HashSet<LinkCutTreeNode>());
		// HACK: Convert lctnChildren to correct type BUT ALSO CUT EACH CHILD FROM ITS PARENT
		Set<GraphNode> children = new HashSet<GraphNode>(lctnChildren.size());
		for (LinkCutTreeNode linkCutTreeNode : lctnChildren) {
			GraphNode child = (GraphNode) linkCutTreeNode;
			if (child.isNot(Visited.POST)) {
				children.add(child);
			}
			LinkCut.cut(child);
		}
		return children;
	}
	
	public void applyToDirectTreeChildren(Function<LinkCutTreeNode, Void> func) {
		LinkCut.applyDirectChildren(this, func);
	}
}
