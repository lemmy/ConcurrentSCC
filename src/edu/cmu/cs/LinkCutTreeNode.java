package edu.cmu.cs;

import java.util.HashSet;
import java.util.Set;

public class LinkCutTreeNode {

	LinkCutTreeNode left, right;
	protected LinkCutTreeNode preferred;
	protected final int id;
	protected final Set<LinkCutTreeNode> children = new HashSet<LinkCutTreeNode>();
	
	// For unit tests only
	LinkCutTreeNode() {
		this(-1);
	}
	
	protected LinkCutTreeNode(int id) {
		this.id = id;
	}

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

	public void addChild(LinkCutTreeNode p) {
		assert p != this;
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
