package edu.cmu.cs;

import java.util.HashSet;
import java.util.Set;

public class LinkCutTreeNode {

	LinkCutTreeNode left, right;
	protected LinkCutTreeNode preferred;
	protected final int id;
	//TODO set of children can be replaced by "ternarization" if needed.
	protected final Set<LinkCutTreeNode> children = new HashSet<LinkCutTreeNode>();
	
	// For unit tests only
	LinkCutTreeNode() {
		this(-1);
	}
	
	protected LinkCutTreeNode(int id) {
		this.id = id;
	}

	/**
	 * @return true iff root of its splay tree, not the root of the LC tree.
	 */
	boolean isroot() {
		return preferred == null || (preferred.left != this && preferred.right != this);
	}

	void addChild(LinkCutTreeNode p) {
		assert p != this;
		children.add(p);
	}
	
	/**
	 * @return true iff root of the LC tree.
	 */
	public boolean isRoot() {
		// TODO Could add a flag that indicates if this is a root or not. It
		// would be flipped when the node is linked or cut.
		return getRoot() == this;
	}
	
	/**
	 * @param p
	 *            The alleged child of this node.
	 * @return true iff the given {@link LinkCutTreeNode} p is a child of this root.
	 */
	public boolean isRootTo(LinkCutTreeNode p) {
		 return p.getRoot() == this;
	}
	
	public LinkCutTreeNode getRoot() {
		return LinkCut.root(this);
	}
	
	public void link(LinkCutTreeNode parent) {
		LinkCut.link(this, parent);
	}
	
	public void cut() {
		LinkCut.cut(this);
	}
	
	public LinkCutTreeNode getParent() {
		return LinkCut.parent(this);
	}

	public void reLinkChildren(final LinkCutTreeNode newRoot, final Set<? extends LinkCutTreeNode> excludes) {
		// Take copy of children. children is modified by
		// LinkCut.cut/LinkCut.link which results in a
		// ConcurrentModificationException otherwise.
		final Set<LinkCutTreeNode> copy = new HashSet<>(children);
		
		// Unlink/Cut children of this from this and link them to
		// the newRoot node.
		// E.g. for test B when {2,1} form a contraction and tree being:
		// {2,1} < 3 exploring the arc {2,3} has to trigger compaction
		// of 3 into 2. But when only cut is done without linking to
		// this, the previous compaction will have cut 3 loose already.
		for (LinkCutTreeNode child : copy) {
			// This while loop is walking a path from a child to its root
			// and it obviously has to skip linking the nodes on the path to
			// the root again. This for loop here is so that childs *not on
			// the path* are linked to the root.
			if (!excludes.contains(child)) {
				LinkCut.cut(child);
				LinkCut.link(child, newRoot);
			}
		}
		// Now that the children of this *who are not on this path* are
		// linked to the newRoot, clear the last remaining child (if
		// any) which is the one on the path
		assert children.isEmpty() || children.size() == 1;
		children.clear();
	}
	
	public Set<LinkCutTreeNode> getChildren() {
		return new HashSet<>(this.children);
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}
}
