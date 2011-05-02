/**********************************************************************
 * Copyright (c) 2006,2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory;

import java.util.Hashtable;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.ptp.pldt.common.util.Utility;

/**
 * Interval tree, mapping statement (position, length)->statement
 * 
 * @author pazel
 * 
 */
public class StatementMap
{
	protected Member root_ = null;
	protected Hashtable iastnodeToMember_ = new Hashtable();

	public StatementMap()
	{

	}

	public boolean add(IASTNode node)
	{
		if (node == null)
			return false;

		Member m = new Member(node);
		RBInsert(m);

		iastnodeToMember_.put(node, m);

		return true;
	}

	/**
	 * getLocation - get offset/length relative to containing file
	 * 
	 * @param node
	 *            - IASTNode
	 * @return Location
	 */
	public Location getLocation(IASTNode node)
	{
		Utility.Location l = Utility.getLocation(node); // Use common function
		return new Location(l.low_, l.high_);
		/*
		 * ASTNode astnode = (node instanceof ASTNode ? (ASTNode)node : null);
		 * if (astnode==null) return null;
		 * 
		 * IASTFileLocation ifl = node.getFileLocation();
		 * // offset calculation is tricky - we used the following since it seems to cover the most cases
		 * int offset = 0;
		 * int length = 0;
		 * if (ifl!=null) {
		 * offset = ifl.getNodeOffset();
		 * length = ifl.getNodeLength();
		 * }
		 * else { // this happens in "omp sections", apparently due to pragmas splitting the region
		 * IASTNodeLocation [] locs = node.getNodeLocations();
		 * if (locs==null || locs.length==0) return null;
		 * offset = locs[0].getNodeOffset();
		 * length = astnode.getLength();
		 * }
		 * return new Location(offset, offset+length-1);
		 */
	}

	public void remove(IASTNode node)
	{
		Member m = (Member) iastnodeToMember_.get(node);
		if (m == null)
			return;

		RBDelete(m);

		iastnodeToMember_.remove(node);
	}

	public IASTNode find(int offset, int length)
	{
		return findInterval(offset, offset + length - 1);
	}

	public IASTNode findInterval(int offset, int endset)
	{
		Member x = findIntervalMember(offset, endset);
		if (x != null)
			return x.node_;
		return null;
	}

	protected Member findIntervalMember(int offset, int endset)
	{
		Member x = root_;
		while (x != null && !overlap(x.low_, x.high_, offset, endset)) {
			if (x.left_ != null && x.left_.max_ >= offset)
				x = x.left_;
			else
				x = x.right_;
		}
		return x;
	}

	protected boolean overlap(int offset1, int endset1, int offset2, int endset2)
	{
		if (endset1 < offset2 || endset2 < offset1)
			return false;
		return true;
	}

	// NOTE: do not use until we understand
	// 1) if null should be replaced by a NIL object
	// 2) where/how to do the max update corrections
	protected void RBDelete(Member z)
	{
		Member y = null, x = null;

		y = ((z.left_ == null || z.right_ == null) ? z : treeSuccessor(z));
		x = (y.left_ != null ? y.left_ : y.right_);
		x.parent_ = y.parent_;
		if (y.parent_ == null)
			root_ = x;
		else {
			if (y == y.parent_.left_)
				y.parent_.left_ = x;
			else
				y.parent_.right_ = x;
		}
		if (y != z)
			z.copyFrom(y);
		if (y.color_ == Member.BLACK)
			RBDeleteFixup(x);
	}

	protected void RBDeleteFixup(Member x)
	{
		Member w = null;
		while (x != root_ && x.color_ == Member.BLACK) {
			if (x == x.parent_.left_) {
				w = x.parent_.right_;
				if (w.color_ == Member.RED) {
					w.color_ = Member.BLACK;
					x.parent_.color_ = Member.RED;
					x.parent_.leftRotate();
					w = x.parent_.right_;
				}
				if (w.left_.color_ == Member.BLACK && w.right_.color_ == Member.BLACK) {
					w.color_ = Member.RED;
					x = x.parent_;
				}
				else {
					if (w.right_.color_ == Member.BLACK) {
						w.left_.color_ = Member.BLACK;
						w.color_ = Member.RED;
						w.rightRotate();
						w = x.parent_.right_;
					}
					w.color_ = x.parent_.color_;
					x.parent_.color_ = Member.BLACK;
					w.right_.color_ = Member.BLACK;
					x.parent_.leftRotate();
					x = root_;
				}
			}
			else {
				// same with left/right exchanged
				w = x.parent_.left_;
				if (w.color_ == Member.RED) {
					w.color_ = Member.BLACK;
					x.parent_.color_ = Member.RED;
					x.parent_.rightRotate();
					w = x.parent_.left_;
				}
				if (w.right_.color_ == Member.BLACK && w.left_.color_ == Member.BLACK) {
					w.color_ = Member.RED;
					x = x.parent_;
				}
				else {
					if (w.left_.color_ == Member.BLACK) {
						w.right_.color_ = Member.BLACK;
						w.color_ = Member.RED;
						w.leftRotate();
						w = x.parent_.left_;
					}
					w.color_ = x.parent_.color_;
					x.parent_.color_ = Member.BLACK;
					w.left_.color_ = Member.BLACK;
					x.parent_.rightRotate();
					x = root_;
				}
			}
		}
		x.color_ = Member.BLACK;
	}

	protected void RBInsert(Member x)
	{
		Member y = null;
		treeInsert(x);
		x.updateMax();
		x.color_ = Member.RED;
		while (x != root_ && x.parent_.color_ == Member.RED) {
			if (x.parent_ == x.parent_.parent_.left_) {
				y = x.parent_.parent_.left_;
				if (y.color_ == Member.RED) {
					x.parent_.color_ = Member.BLACK;
					y.color_ = Member.BLACK;
					x.parent_.parent_.color_ = Member.RED;
					x = x.parent_.parent_;
				}
				else {
					if (x == x.parent_.right_) {
						x = x.parent_;
						x.leftRotate();
					}
					x.parent_.color_ = Member.BLACK;
					x.parent_.parent_.color_ = Member.RED;
					x.parent_.parent_.rightRotate();
				}
			}
			else {
				y = x.parent_.parent_.right_;
				if (y.color_ == Member.RED) {
					x.parent_.color_ = Member.BLACK;
					y.color_ = Member.BLACK;
					x.parent_.parent_.color_ = Member.RED;
					x = x.parent_.parent_;
				}
				else {
					if (x == x.parent_.left_) {
						x = x.parent_;
						x.rightRotate();
					}
					x.parent_.color_ = Member.BLACK;
					x.parent_.parent_.color_ = Member.RED;
					x.parent_.parent_.leftRotate();
				}
			}
		} // end while
		root_.color_ = Member.BLACK;
	}

	protected void treeInsert(Member z)
	{
		Member y = null;
		Member x = root_;

		while (x != null) {
			y = x;
			x = (z.low_ < x.low_ ? x.left_ : x.right_);
		}
		z.parent_ = y;
		if (y == null)
			root_ = z;
		else {
			if (z.low_ < y.low_)
				y.left_ = z;
			else
				y.right_ = z;
		}
	}

	protected Member treeSuccessor(Member x)
	{
		if (x.right_ != null)
			return treeMinimum(x.right_);
		Member y = x.parent_;
		while (y != null && x == y.right_) {
			x = y;
			y = y.parent_;
		}
		return y;
	}

	protected Member treeMaximum(Member x)
	{
		while (x.right_ != null)
			x = x.right_;
		return x;
	}

	protected Member treeMinimum(Member x)
	{
		while (x.left_ != null)
			x = x.left_;
		return x;
	}

	// for testing
	protected void add(int offset, int endset)
	{
		Member m = new Member(offset, endset);
		RBInsert(m);
	}

	// -------------------------------------------------------------------------
	// Member
	// -------------------------------------------------------------------------
	public class Member
	{
		protected int low_ = 0;
		protected int high_ = 0;
		protected int max_ = 0;
		protected int color_ = 0;
		protected IASTNode node_ = null;
		protected Member left_ = null;
		protected Member right_ = null;
		protected Member parent_ = null;

		public static final int RED = 0;
		public static final int BLACK = 1;

		public Member(int low, int high)
		{
			low_ = low;
			high_ = high;
		}

		public Member(Location l, IASTNode node)
		{
			low_ = l.low_;
			high_ = l.high_;
			node_ = node;
		}

		public Member(IASTNode node)
		{
			this(getLocation(node), node);
		}

		public void copyFrom(Member y)
		{
			low_ = y.low_;
			high_ = y.high_;
			node_ = y.node_; // is this right?
			// TODO: compute max
		}

		public void leftRotate()
		{
			Member y = this.right_; // assumed to be non-null;
			this.right_ = y.left_;
			if (y.left_ != null)
				y.left_.parent_ = this;
			y.parent_ = this.parent_;
			if (this.parent_ == null)
				root_ = y;
			else {
				if (this == this.parent_.left_)
					this.parent_.left_ = y;
				else
					this.parent_.right_ = y;
			}
			y.left_ = this;
			this.parent_ = y;

			// Set the max's - order is important
			this.updateMax();
			y.updateMax();
		}

		public void rightRotate()
		{
			Member x = this.left_; // assumed to be non-null;
			this.left_ = x.right_;
			if (x.right_ != null)
				x.right_.parent_ = this;
			x.parent_ = this.parent_;
			if (this.parent_ == null)
				root_ = x;
			else {
				if (this == this.parent_.left_)
					this.parent_.left_ = x;
				else
					this.parent_.right_ = x;
			}
			x.right_ = this;
			this.parent_ = x;

			// Set the max's - order is important
			this.updateMax();
			x.updateMax();
		}

		protected void updateMax()
		{
			int l = (left_ != null ? left_.max_ : 0);
			int r = (right_ != null ? right_.max_ : 0);
			max_ = Math.max(high_, Math.max(l, r));
		}

	}

	// -------------------------------------------------------------------------
	// Member
	// -------------------------------------------------------------------------
	protected static class Location
	{
		public int low_ = 0;
		public int high_ = 0;

		public Location(int low, int high)
		{
			low_ = low;
			high_ = high;
		}
	}

	public static void main(String[] args)
	{
		StatementMap sm = new StatementMap();

		sm.add(12, 15);
		sm.add(1, 6);
		sm.add(14, 24);

		Member m = sm.findIntervalMember(12, 12);
		if (m == null)
			System.out.println("could not find 12");
		else {
			System.out.println("found in " + m.low_ + ":" + m.high_);
		}

		return;
	}

}
