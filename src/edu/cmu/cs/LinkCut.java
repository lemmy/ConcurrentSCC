package edu.cmu.cs;

import java.util.Collection;

/**
 * Taken from http://www.cs.cmu.edu/~avrim/451f12/lectures/lect1009-linkcut.txt
 * 
 * https://youtu.be/cyx63EI_jbo (MIT opencourseware) has an introduction to LC trees.
 */
public class LinkCut {
	// rotR and rotL are also known as zig and zag
	static void rotR(LinkCutTreeNode p) {
		LinkCutTreeNode q = p.preferred;
		LinkCutTreeNode r = q.preferred;
		if ((q.left = p.right) != null) {
			q.left.preferred = q;
		}
		p.right = q;
		q.preferred = p;
		if ((p.preferred = r) != null) {
			if (r.left == q) {
				r.left = p;
			} else if (r.right == q) {
				r.right = p;
			}
		}
	}

	static void rotL(LinkCutTreeNode p) {
		LinkCutTreeNode q = p.preferred;
		LinkCutTreeNode r = q.preferred;
		if ((q.right = p.left) != null) {
			q.right.preferred = q;
		}
		p.left = q;
		q.preferred = p;
		if ((p.preferred = r) != null) {
			if (r.left == q) {
				r.left = p;
			} else if (r.right == q) {
				r.right = p;
			}
		}
	}

	static void splay(LinkCutTreeNode p) {
		while (!p.isroot()) {
			LinkCutTreeNode q = p.preferred;
			if (q.isroot()) {
				if (q.left == p) {
					rotR(p);
				} else {
					rotL(p);
				}
			} else {
				LinkCutTreeNode r = q.preferred;
				if (r.left == q) {
					if (q.left == p) {
						rotR(q);
						rotR(p);
					} else {
						rotL(p);
						rotR(p);
					}
				} else {
					if (q.right == p) {
						rotL(q);
						rotL(p);
					} else {
						rotR(p);
						rotL(p);
					}
				}
			}
		}
	}

	/*
	 * This makes node q the root of the virtual tree, and also q is the
	 * leftmost node in its splay tree
	 * 
	 * similar to 'access' in tango tree
	 * 
	 * Technically it rearranges the splay tree so that the right most node is
	 * the root. The node at the root has no preferred parent.
	 */
	static void expose(LinkCutTreeNode q) {
		LinkCutTreeNode r = null;
		for (LinkCutTreeNode p = q; p != null; p = p.preferred) {
			splay(p);
			p.left = r;
			r = p;
		}
		;
		splay(q);
	}

	/**
	 * Assuming p and q are nodes in different trees and that p is a root of its
	 * tree, this links p to q
	 */
	public static void link(LinkCutTreeNode p, LinkCutTreeNode q) {
		// Added by mku to prevent livelock.
		if (p == q) {
			throw new RuntimeException("Trying to link identical nodes");
		}
		expose(p);
		if (p.right != null) {
			throw new RuntimeException("non-root link");
		}
		p.preferred = q;
		p.isRoot = false;
		
		// Hack: Store child of represented tree to reconstruct the set of
		// children (even unpreferred paths) later (see children(..)).
		q.addChild(p);
	}

	/**
	 * this returns the id of the node that is the root of the tree containing p
	 */
	public static LinkCutTreeNode root(LinkCutTreeNode p) {
		expose(p);
		while (p.right != null) {
			p = p.right;
		}
		splay(p);
		// Changed by mku to return node instead of node's id.
		return p;
	}

	/**
	 * p is not a tree root. Delete the edge from p to its parent, thus
	 * separating the tree in two.
	 * 
	 * Added by mku
	 */
	public static void cut(LinkCutTreeNode p) {
		expose(p);
		if(p.right != null) {
			p.right.children.remove(p);
			p.right.preferred = null;
			p.right = null;
		}
		p.isRoot = true;
	}
	
	/**
	 * Returns the parents/ancestors of the given node. If into is insertion
	 * order preserving, the elements will be ordered by the order of
	 * the represented tree).
	 * 
	 * Added by mku
	 */
	public static Collection<LinkCutTreeNode> parents(LinkCutTreeNode p, Collection<LinkCutTreeNode> into) {
		expose(p);
		// Traverse the right hand subtree in in-order
		inOrder(p.right, into);
		// Last, add this node as the smallest element (when Collection order,
		// ie. stack)
		into.add(p);
		
		return into;
	}
	
	/**
	 * Added by mku
	 */
	private static void inOrder(LinkCutTreeNode p, Collection<LinkCutTreeNode> path) {
		if (p == null) {
			return;
		}
		//TODO Replace recursion with iteration
		if (p.right != null) {
			inOrder(p.right, path);
		}
		path.add(p);
		if (p.left != null) {
			inOrder(p.left, path);
		}
	}

	/**
	 * Added by mku
	 */
	public static Collection<LinkCutTreeNode> children(LinkCutTreeNode p, Collection<LinkCutTreeNode> into) {
		into.add(p);
		for (LinkCutTreeNode child : p.children) {
			children(child, into);
		}
		return into;
	}

	public static Collection<LinkCutTreeNode> directChildren(LinkCutTreeNode p, Collection<LinkCutTreeNode> into) {
		into.addAll(p.children);
		return into;
	}

	/**
	 * Returns p's parent/ancestor or null if p is a root of the represented/real tree.
	 * 
	 * Added by mku
	 */
	public static LinkCutTreeNode parent(LinkCutTreeNode p) {
		expose(p);
		// Parent is right's left-most child.
		LinkCutTreeNode right = p.right;
		if (right == null) {
			// Has no parent
			return null;
		}
		while (right.left != null) {
			right = right.left;
		}
		return right;
	}
}
