/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.Block;

public class MPISSA {
	protected ICallGraph cg_;
	protected MPICallGraphNode currentFunc_;
	protected Hashtable<String, List<IBlock>> defTable_;
	protected IControlFlowGraph cfg_;

	public MPISSA(ICallGraph cg) {
		cg_ = cg;
	}

	public void run() {
		for (ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()) {
			MPICallGraphNode node = (MPICallGraphNode) n;
			// System.out.println(node.getFuncName());
			if (!node.marked)
				continue;
			currentFunc_ = node;
			cfg_ = node.getCFG();
			defTable_ = node.getDefTable();
			domFrontier();
			placePHI();
			ExitBlockPhi ebp = new ExitBlockPhi();
			ebp.run();
		}
	}

	public void domFrontier() {
		for (IBlock bx = cfg_.getEntry(); bx != null; bx = bx.topNext()) {
			MPIBlock x = (MPIBlock) bx;
			List<IBlock> DF = new ArrayList<IBlock>();
			for (IBlock by = cfg_.getEntry(); by != null; by = by.topNext()) {
				MPIBlock y = (MPIBlock) by;
				for (Iterator<IBlock> i = y.getPreds().iterator(); i.hasNext();) {
					MPIBlock pred = (MPIBlock) i.next();
					if (pred.getDOM().contains(x) && !(y.getDOM().contains(x) && x != y)) {
						if (!DF.contains(y))
							DF.add(y);
					}
				}
			}
			x.setDF(DF);
		}
	}

	public void placePHI() {
		int iterCount = 0;
		for (IBlock b = cfg_.getEntry(); b != null; b = b.topNext()) {
			MPIBlock block = (MPIBlock) b;
			block.hasAlready = 0;
			block.work = 0;
		}
		LinkedList<IBlock> W = new LinkedList<IBlock>();
		for (Enumeration<String> e = defTable_.keys(); e.hasMoreElements();) {
			String var = e.nextElement();
			iterCount++;
			List<IBlock> defs = defTable_.get(var);
			for (Iterator<IBlock> i = defs.iterator(); i.hasNext();) {
				MPIBlock defblock = (MPIBlock) i.next();
				defblock.work = iterCount;
				W.add(defblock);
			}
			while (!W.isEmpty()) {
				MPIBlock next = (MPIBlock) W.remove();
				for (Iterator<IBlock> i = next.getDF().iterator(); i.hasNext();) {
					MPIBlock df = (MPIBlock) i.next();
					if (df.hasAlready < iterCount) {
						if (df.getCond() == null && df != cfg_.getExit()) {
							// no associated condition?
							System.out.println("Error: phi Node has no condition!" + //$NON-NLS-1$
									currentFunc_.getFuncName() + " Block " + df.getID()); //$NON-NLS-1$
							// return;
						}
						df.setPhi();
						if (!df.getDef().contains(var))
							df.getDef().add(var);
						// if(!df.getUse().contains(var)) df.getUse().add(var);
						if (df.getUse().contains(var) && !df.getUsedPhiVar().contains(var))
							df.getUsedPhiVar().add(var);
						if (!df.getPhiVar().contains(var))
							df.getPhiVar().add(var);
						if (!defs.contains(df))
							defs.add(df);
						updateDefTable(df, var);
						// System.out.println("Block " + df.getID() + " has Phi for " + var);
						df.hasAlready = iterCount;
						if (df.work < iterCount) {
							df.work = iterCount;
							W.add(df);
						}
					}
				}
			}
		}
		for (IBlock b = cfg_.getEntry(); b != null; b = b.topNext()) {
			MPIBlock block = (MPIBlock) b;
			if (block.hasPhi())
				for (Iterator<String> i = block.getPhiVar().iterator(); i.hasNext();) {
					String var = i.next();
					if (!block.getUse().contains(var))
						block.getUse().add(var);
				}
		}
	}

	protected void updateDefTable(IBlock block, String var) {
		List<IBlock> list = defTable_.get(var);
		if (list == null) {
			System.out.print("Error in SSA!"); //$NON-NLS-1$
			return;
		}
		if (!list.contains(block))
			list.add(block);
	}

	class ExitBlockPhi extends ASTVisitor {

		public void run() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			currentFunc_.getFuncDef().accept(this);
		}

		public int visit(IASTStatement stmt) {
			MPIBlock condblock = null;
			if (stmt instanceof IASTDoStatement) {
				IASTDoStatement doS = (IASTDoStatement) stmt;
				condblock = (MPIBlock) cfg_.getBlock(doS.getCondition(), stmt);
			}
			else if (stmt instanceof IASTForStatement) {
				IASTForStatement forS = (IASTForStatement) stmt;
				condblock = (MPIBlock) cfg_.getBlock(forS.getConditionExpression(), stmt);
			}
			else if (stmt instanceof IASTWhileStatement) {
				IASTWhileStatement whileS = (IASTWhileStatement) stmt;
				condblock = (MPIBlock) cfg_.getBlock(whileS.getCondition(), stmt);
			}
			else {
				return PROCESS_CONTINUE;
			}

			MPIBlock exitblock = null;
			for (Iterator<IBlock> i = condblock.getSuccs().iterator(); i.hasNext();) {
				MPIBlock succ = (MPIBlock) i.next();
				if (succ.getType() == Block.exit_join_type)
					exitblock = succ;
			}

			exitblock.setPhi();
			for (Iterator<String> i = condblock.getPhiVar().iterator(); i.hasNext();) {
				String phivar = i.next();
				if (!exitblock.getDef().contains(phivar))
					exitblock.getDef().add(phivar);
				if (!exitblock.getUse().contains(phivar))
					exitblock.getUse().add(phivar);
				if (!exitblock.getPhiVar().contains(phivar))
					exitblock.getPhiVar().add(phivar);
				updateDefTable(exitblock, phivar);
			}

			return PROCESS_CONTINUE;
		}
	}

}
