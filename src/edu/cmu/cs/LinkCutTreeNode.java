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

	public void addChild(LinkCutTreeNode p) {
		assert p != this;
		children.add(p);
	}
}
