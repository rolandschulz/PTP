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

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.Block;

public class MPIBlock extends Block {

	/** Used variables in this block */
	protected List<String> use_;

	/** Defined variables in this block */
	protected List<String> def_;

	/** In, Out, Gen and Kill for each variable */
	protected Hashtable<String, List<IBlock>> in_;
	protected Hashtable<String, List<IBlock>> out_;
	protected Hashtable<String, List<IBlock>> gen_;
	protected Hashtable<String, List<IBlock>> kill_;

	/** Reaching definitions (flow dependence) */
	protected Hashtable<String/* var */, List<IBlock>> duPred_;
	/** Reaching uses */
	protected Hashtable<String/* var */, List<IBlock>> duSucc_;

	/** Dominator Frontier */
	protected List<IBlock> df_;

	/**
	 * This records the condition block from which different paths
	 * are generated. Similar as Gated SSA. Note that there may or may
	 * not be a \phi node in "this" block, depends on whether there is
	 * variable definition along the paths from cond_ to this block.
	 */
	protected List<IBlock> condBlock_;
	// protected IBlock condBlock_;
	protected List<IBlock> joinBlocks_;

	/**
	 * A set of variables introduced by \Phi functions. Such variables
	 * are regarded as being both used and defined in this block
	 */
	protected List<String> phiVar_;
	/**
	 * A set variables that are used and also introduced by placing a
	 * \phi function, that is, there is a data dependence from the
	 * \phi function to the originally used variable.
	 */
	protected List<String> usedPhiVar_;
	/** This block contains a \phi node ? */
	protected boolean phi;

	/** list of multi-valued variables (in this block? BRT) */
	protected List<String> mvVar_;
	protected List<String> oldMVvar_;

	private boolean mv;

	boolean sliced;
	/** For \phi node placement */
	int hasAlready;
	int work;

	boolean withBreak = false;
	boolean withContinue = false;

	public MPIBlock() {
		super();
		init();
	}

	public MPIBlock(IASTNode content, IASTStatement parent, int type) {
		super(content, parent, type);
		init();
	}

	public MPIBlock(IASTExpression expr, IASTStatement parent) {
		super(expr, parent);
		init();
	}

	public MPIBlock(IASTStatement stmt) {
		super(stmt);
		init();
	}

	public MPIBlock(IASTName label) {
		super(label);
		init();
	}

	private void init() {

		use_ = new ArrayList<String>();
		def_ = new ArrayList<String>();

		in_ = new Hashtable<String, List<IBlock>>();
		out_ = new Hashtable<String, List<IBlock>>();
		gen_ = new Hashtable<String, List<IBlock>>();
		kill_ = new Hashtable<String, List<IBlock>>();

		duPred_ = new Hashtable<String, List<IBlock>>();
		duSucc_ = new Hashtable<String, List<IBlock>>();

		df_ = new ArrayList<IBlock>();
		condBlock_ = new ArrayList<IBlock>();
		// condBlock_ = null;
		joinBlocks_ = new ArrayList<IBlock>();
		phi = false;
		phiVar_ = new ArrayList<String>();
		usedPhiVar_ = new ArrayList<String>();

		mvVar_ = new ArrayList<String>();
		oldMVvar_ = new ArrayList<String>();

		sliced = false;
		mv = false;
	}

	public void setCond(List<IBlock> b) {
		condBlock_ = b;
	}

	public List<IBlock> getCond() {
		return condBlock_;
	}

	public List<IBlock> getJoin() {
		return joinBlocks_;
	}

	public List<String> getUse() {
		return use_;
	}

	public void setUse(List<String> set) {
		use_ = set;
	}

	public List<String> getDef() {
		return def_;
	}

	public void setDef(List<String> set) {
		def_ = set;
	}

	public void setIn(Hashtable<String, List<IBlock>> in) {
		in_ = in;
	}

	public void setOut(Hashtable<String, List<IBlock>> out) {
		out_ = out;
	}

	public Hashtable<String, List<IBlock>> getIn() {
		return in_;
	}

	public Hashtable<String, List<IBlock>> getOut() {
		return out_;
	}

	public void setGen(Hashtable<String, List<IBlock>> gen) {
		gen_ = gen;
	}

	public void setKill(Hashtable<String, List<IBlock>> kill) {
		kill_ = kill;
	}

	public Hashtable<String, List<IBlock>> getGen() {
		return gen_;
	}

	public Hashtable<String, List<IBlock>> getKill() {
		return kill_;
	}

	public void setDUPred(Hashtable<String, List<IBlock>> pred) {
		duPred_ = pred;
	}

	public void setDUSucc(Hashtable<String, List<IBlock>> succ) {
		duSucc_ = succ;
	}

	public Hashtable<String, List<IBlock>> getDUPred() {
		return duPred_;
	}

	public Hashtable<String, List<IBlock>> getDUSucc() {
		return duSucc_;
	}

	public void setDF(List<IBlock> list) {
		df_ = list;
	}

	public List<IBlock> getDF() {
		return df_;
	}

	public void setPhi() {
		phi = true;
	}

	public boolean hasPhi() {
		return phi;
	}

	public List<String> getPhiVar() {
		return phiVar_;
	}

	public List<String> getUsedPhiVar() {
		return usedPhiVar_;
	}

	public List<String> getMVvar() {
		return mvVar_;
	}

	public void setMVvar(List<String> list) {
		mvVar_ = list;
	}

	public List<String> getOldMVvar() {
		return oldMVvar_;
	}

	public void setOldMVvar(List<String> list) {
		oldMVvar_ = list;
	}

	// private static int countMV=0; // BRT for debugging
	public void setMV(boolean val) {
		// countMV++;
		//System.out.println("setMV: "+val+"   "+countMV+" blockID: "+getID()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		mv = val;
	}

	public boolean getMV() {
		return mv;
	}

	public void print() {
		super.print();

		System.out.print("joins to: "); //$NON-NLS-1$
		for (Iterator<IBlock> i = joinBlocks_.iterator(); i.hasNext();) {
			System.out.print(i.next().getID() + ", "); //$NON-NLS-1$
		}
		System.out.println(" "); //$NON-NLS-1$

		System.out.print(" use = "); //$NON-NLS-1$
		for (Iterator<String> i = use_.iterator(); i.hasNext();) {
			System.out.print(i.next() + ", "); //$NON-NLS-1$
		}
		System.out.print(" def = "); //$NON-NLS-1$
		for (Iterator<String> i = def_.iterator(); i.hasNext();) {
			System.out.print(i.next() + ", "); //$NON-NLS-1$
		}
		System.out.println(" "); //$NON-NLS-1$

		System.out.println("Flow dependence: "); //$NON-NLS-1$
		for (Enumeration<String> e = duSucc_.keys(); e.hasMoreElements();) {
			String var = e.nextElement();
			List<IBlock> list = duSucc_.get(var);
			if (list.isEmpty())
				continue;
			System.out.print(var + " to: "); //$NON-NLS-1$
			for (Iterator<IBlock> i = list.iterator(); i.hasNext();) {
				System.out.print(i.next().getID() + ", "); //$NON-NLS-1$
			}
			System.out.println(" "); //$NON-NLS-1$
		}
		System.out.println(" "); //$NON-NLS-1$

		if (mv)
			System.out.println("------ Multi-valued ------"); //$NON-NLS-1$
		else
			System.out.println("------ Single-valued ------"); //$NON-NLS-1$
		System.out.println(" "); //$NON-NLS-1$
		System.out.println(" "); //$NON-NLS-1$
	}

	/** reset the block ID counter */
	public static void clean() {
		counter = 0;
	}

	public String toString() {
		super.print();
		StringBuffer buf = new StringBuffer();

		buf.append("joins to: "); //$NON-NLS-1$
		for (Iterator<IBlock> i = joinBlocks_.iterator(); i.hasNext();) {
			buf.append(i.next().getID() + ", "); //$NON-NLS-1$
		}
		buf.append(" \n"); //$NON-NLS-1$

		buf.append(" use = "); //$NON-NLS-1$
		for (Iterator<String> i = use_.iterator(); i.hasNext();) {
			buf.append(i.next() + ", "); //$NON-NLS-1$
		}
		buf.append(" def = "); //$NON-NLS-1$
		for (Iterator<String> i = def_.iterator(); i.hasNext();) {
			buf.append(i.next() + ", "); //$NON-NLS-1$
		}
		buf.append(" \n"); //$NON-NLS-1$

		buf.append("Flow dependence: \n"); //$NON-NLS-1$
		for (Enumeration<String> e = duSucc_.keys(); e.hasMoreElements();) {
			String var = e.nextElement();
			List<IBlock> list = duSucc_.get(var);
			if (list.isEmpty())
				continue;
			buf.append(var + " to: "); //$NON-NLS-1$
			for (Iterator<IBlock> i = list.iterator(); i.hasNext();) {
				buf.append(i.next().getID() + ", "); //$NON-NLS-1$
			}
			buf.append(" \n"); //$NON-NLS-1$
		}
		buf.append(" \n"); //$NON-NLS-1$

		if (mv)
			buf.append("------ Multi-valued ------\n"); //$NON-NLS-1$
		else
			buf.append("------ Single-valued ------\n"); //$NON-NLS-1$
		return buf.toString();
	}
}
