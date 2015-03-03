package org.kuppe.graphs.tarjan;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GraphNode implements Comparable<GraphNode> {

	public enum Visited {
		UN, PRE, POST
	};

	private final Set<GraphNode> successors = new TreeSet<GraphNode>();
	private final Set<GraphNode> predecessors = new HashSet<GraphNode>();
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
		aGraphNode.predecessors.add(this);
	}

//	public ListIterator<GraphNode> iterator() {
//		return successors.listIterator();
//	}

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
//		for (GraphNode succ : aNode.successors) {
//			if (!successors.contains(succ)) {
//				iterator.add(succ);
//			}
//		}
		
		// Remove aNode from all its predecessors which basically causes it to
		// disappear from the graph
		aNode.predecessors.clear();
		
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
			if (graphNode.predecessors.remove(aNode)) {
				graphNode.predecessors.add(this);
			}
		}
	}

	@Override
	public String toString() {
		return "GraphNode [anId=" + id + ", visited=" + visited + "]";
	}
//
//	public int succSize() {
//		return this.successors.size();
//	}
	
	public GraphNode getUnvisitedSuccessor(final Set<GraphNode> explored) {
		return getUnvisitedSuccessorD(explored);
	}

	// Non-deterministically (Change this.successors to HashSet too)
//	private GraphNode getUnvisitedSuccessorND(final Set<GraphNode> explored) {
//		final Set<GraphNode> temp = new HashSet<GraphNode>(this.successors);
//		temp.removeAll(explored);
//		if (temp.isEmpty()) {
//			return null;
//		} else {
//			return temp.toArray(new GraphNode[temp.size()])[0];
//		}
//	}

	// Deterministically
	private GraphNode getUnvisitedSuccessorD(final Set<GraphNode> explored) {
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
