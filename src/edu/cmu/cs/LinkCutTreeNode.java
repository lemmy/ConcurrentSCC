package edu.cmu.cs;

import java.util.HashSet;
import java.util.Set;

public class LinkCutTreeNode {

	int s, my_s, on/*, id*/;

	boolean flip, my_flip;

	LinkCutTreeNode left, right;
	protected LinkCutTreeNode preferred;
	protected final int id;
	
	protected Set<LinkCutTreeNode> children = new HashSet<LinkCutTreeNode>();
	
	LinkCutTreeNode() {
		this(-1);
	}
	
	protected LinkCutTreeNode(int id) {
		this.id = id;
	}
//	
//	Node(int c, int i) {
////		id = i;
////		s = my_s = c;
////		on = 0;
//		l = r = p = null;
////		flip = my_flip = false;
//	}

	boolean isroot() {
		return preferred == null || (preferred.left != this && preferred.right != this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (id == -1) {
			return super.toString();
		}
		return "LinkCutTreeNode [id=" + id + "]";
	}

	/*
	 * If this node is flipped, we unflip it, and push the change down the tree,
	 * so that it represents the same thing.
	 */
	void normalize() {
		if (flip) {
			flip = false;
			on = s - on;
			my_flip = !my_flip;
			if (left != null) {
				left.flip = !left.flip;
			}
			if (right != null) {
				right.flip = !right.flip;
			}
		}
	}

	/*
	 * The tree structure has changed in the vicinity of this node (for example,
	 * if this node is linked to a different left child in a rotation). This
	 * function fixes up the data fields in the node to maintain invariants.
	 */
	void update() {
		s = my_s;
		on = (my_flip) ? my_s : 0;
		if (left != null) {
			s += left.s;
			if (left.flip) {
				on += left.s - left.on;
			} else {
				on += left.on;
			}
		}
		if (right != null) {
			s += right.s;
			if (right.flip) {
				on += right.s - right.on;
			} else {
				on += right.on;
			}
		}
	}

	public void addChild(LinkCutTreeNode p) {
		children.add(p);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LinkCutTreeNode other = (LinkCutTreeNode) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
