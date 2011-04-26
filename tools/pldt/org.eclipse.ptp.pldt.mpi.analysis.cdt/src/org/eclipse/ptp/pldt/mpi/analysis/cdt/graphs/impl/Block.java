/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;

/**
 * 
 * @author Yuan Zhang
 * 
 */
public class Block implements IBlock {
	protected List<IBlock> succs_;
	protected List<IBlock> preds_;
	protected IBlock topNext_ = null;
	protected IBlock botNext_ = null;
	protected List<IBlock> DOM_ = null;
	protected List<IBlock> PDOM_ = null;

	protected static int counter = 0;
	protected int id;

	/**
	 * "type" tells the type of the content in this block.
	 * A block with expr_type/stmt_type/label_type stores an
	 * expression/statement/labelname, respectively. A block
	 * with continue_join_type is an empty block in a loop
	 * joining regular flows and flows from continue statements.
	 * A block with exit_join_type is an empty block joining
	 * (1) two branches of an if statement (2) all leaving
	 * edges (through break or default statements) of a
	 * switch statement (3) the regular exit from a loop and
	 * irregular exits from break statements of this loop.
	 */
	protected int type;
	public static final int expr_type = 1;
	public static final int stmt_type = 2;
	public static final int label_type = 3;
	public static final int continue_join_type = 4;
	public static final int exit_join_type = 5;

	protected IASTNode content_;

	/**
	 * The parent of a block storing a statement is the statement itself;
	 * The parent of a block with a predicate expression is the corresponding
	 * if/loop/switch statement;
	 * The parent of a block with a label is the label statement;
	 * The parent of a block with continue_join_type or exit_join_type
	 * is the corresponding if/loop/switch statement;
	 */
	protected IASTStatement parent_;

	protected Hashtable<String, Object> attrs_;

	int color; // white = 0, gray = 1, black = 2

	public Block() {
		id = counter++;
		type = 0;
		content_ = null;
		parent_ = null;
		blockInit();
	}

	public Block(IASTNode content, IASTStatement parent, int type) {
		this();
		content_ = content;
		parent_ = parent;
		this.type = type;
		blockInit();
	}

	/** Short-cut constructor for stmt_type block */
	public Block(IASTStatement stmt) {
		this(stmt, stmt, stmt_type);
	}

	/** Short-cut constructor for expr_type block */
	public Block(IASTExpression expr, IASTStatement parent) {
		this(expr, parent, expr_type);
	}

	/** Short-cut constructor for label_type block */
	public Block(IASTName label) {
		this(label, null, label_type);
	}

	protected void blockInit() {
		succs_ = new ArrayList<IBlock>();
		preds_ = new ArrayList<IBlock>();
		DOM_ = new ArrayList<IBlock>();
		PDOM_ = new ArrayList<IBlock>();
		attrs_ = new Hashtable<String, Object>();
	}

	public int getID() {
		return id;
	}

	public IASTNode getContent() {
		return content_;
	}

	public IASTStatement getParent() {
		return parent_;
	}

	public int getType() {
		return type;
	}

	public boolean search(IASTNode content, IASTStatement parent, int type) {
		if (this.type != type)
			return false;
		if (type == stmt_type) {
			if (content == content_)
				return true;
			else
				return false;
		}
		else if (type == label_type) {
			if (content instanceof IASTName) {
				IASTName name = (IASTName) content;
				IASTName labelName = (IASTName) content_;
				if (name.toString().equals(labelName.toString()))
					return true;
				else
					return false;
			}
			else
				return false;
		}
		else if (type == expr_type) {
			if (content != null && content == content_)
				return true;
			if (content == null && content_ == null && parent_ == parent)
				return true;
			else
				return false;
		}
		else if (type == continue_join_type || type == exit_join_type) {
			if (content != null)
				return false;
			if (parent == parent_)
				return true;
			else
				return false;
		}
		else
			return false;
	}

	public boolean search(IASTExpression expr, IASTStatement parent) {
		return search(expr, parent, expr_type);
	}

	public boolean search(IASTStatement stmt) {
		return search(stmt, stmt, stmt_type);
	}

	public boolean search(IASTName label) {
		return search(label, null, label_type);
	}

	public IBlock topNext() {
		return topNext_;
	}

	public IBlock getTopNext() {
		return topNext_;
	}

	public void setTopNext(IBlock b) {
		topNext_ = b;
	}

	public IBlock botNext() {
		return botNext_;
	}

	public IBlock getBotNext() {
		return botNext_;
	}

	public void setBotNext(IBlock b) {
		botNext_ = b;
	}

	public List<IBlock> getPreds() {
		return preds_;
	}

	public List<IBlock> getSuccs() {
		return succs_;
	}

	public List<IBlock> getDOM() {
		return DOM_;
	}

	public void setDOM(List<IBlock> set) {
		DOM_ = set;
	}

	public List<IBlock> getPDOM() {
		return PDOM_;
	}

	public void setPDOM(List<IBlock> set) {
		PDOM_ = set;
	}

	public void setAttr(String name, Object attr) {
		attrs_.put(name, attr);
	}

	public Object getAttr(String name) {
		return attrs_.get(name);
	}

	/**
	 * Print IBlock information, include id, content (type & raw signature), and successors
	 */
	public void print() {
		System.out.println(toString());
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Block " + id + ": "); //$NON-NLS-1$ //$NON-NLS-2$
		IASTNode content = getContent();
		if (content != null) {
			String type = content.toString(); // a.b.c.Name@abcd
			type = type.substring(type.lastIndexOf('.') + 1); // Name@abcd
			type = type.substring(0, type.indexOf('@'));
			buf.append("  " + type + "  " + content.getRawSignature() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else {
			buf.append("  Empty block"); //$NON-NLS-1$
			buf.append("\n"); //$NON-NLS-1$
		}
		buf.append("  flows to: "); //$NON-NLS-1$
		for (Iterator<IBlock> i = succs_.iterator(); i.hasNext();) {
			buf.append(i.next().getID() + ", "); //$NON-NLS-1$
		}
		buf.append(" \n"); //$NON-NLS-1$
		/*
		 * System.out.print("Dominator: ");
		 * for(Iterator i = DOM_.iterator(); i.hasNext();){
		 * Block dom = (Block)i.next();
		 * System.out.print(dom.getID() + ", ");
		 * }
		 * System.out.println(" ");
		 */
		return buf.toString();
	}
}
