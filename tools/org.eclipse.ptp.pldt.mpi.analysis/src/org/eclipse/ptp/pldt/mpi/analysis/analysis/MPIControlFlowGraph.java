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
import java.util.List;
import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.Block;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.ControlFlowGraph;

public class MPIControlFlowGraph extends ControlFlowGraph {

	/** a set of return statement flowing to the exit block */
	protected List<IBlock> returnBlocks_;
	private static final boolean traceOn = false;

	public MPIControlFlowGraph(IASTStatement funcBody) {
		super(funcBody);
	}

	public List<IBlock> getReturnBlocks() {
		return returnBlocks_;
	}

	protected void collectBlocks() {
		entry_ = new MPIBlock();
		exit_ = new MPIBlock();
		MPIBlockCollector mbc = new MPIBlockCollector();
		mbc.run();
	}

	class MPIBlockCollector extends ASTVisitor {
		public void run() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			prog_.accept(this);
		}

		public int visit(IASTStatement stmt)
		{
			MPIBlock block;
			if (stmt instanceof IASTBreakStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTCaseStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTCompoundStatement) {
			}
			else if (stmt instanceof IASTContinueStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTDeclarationStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTDefaultStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTDoStatement) {
				IASTDoStatement doStmt = (IASTDoStatement) stmt;
				block = new MPIBlock(doStmt.getCondition(), stmt);
				addBlock(block);
				IBlock exitjoin = new MPIBlock(null, stmt, Block.exit_join_type);
				addBlock(exitjoin);
				IBlock continuejoin = new MPIBlock(null, stmt, Block.continue_join_type);
				addBlock(continuejoin);
			}
			else if (stmt instanceof IASTExpressionStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTForStatement) {
				IASTForStatement forStmt = (IASTForStatement) stmt;
				/* The initialization is a statement, and will be added later */
				block = new MPIBlock(forStmt.getConditionExpression(), stmt);
				addBlock(block);
				if (forStmt.getIterationExpression() != null) {
					block = new MPIBlock(forStmt.getIterationExpression(), stmt);
					addBlock(block);
				}
				IBlock continuejoin = new MPIBlock(null, stmt, Block.continue_join_type);
				addBlock(continuejoin);
				IBlock exitjoin = new MPIBlock(null, stmt, Block.exit_join_type);
				addBlock(exitjoin);
			}
			else if (stmt instanceof IASTGotoStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTIfStatement) {
				IASTIfStatement ifStmt = (IASTIfStatement) stmt;
				block = new MPIBlock(ifStmt.getConditionExpression(), stmt);
				addBlock(block);
				IBlock join = new MPIBlock(null, stmt, Block.exit_join_type);
				addBlock(join);
			}
			else if (stmt instanceof IASTLabelStatement) {
				IASTLabelStatement labelS = (IASTLabelStatement) stmt;
				block = new MPIBlock(labelS.getName(), stmt, Block.label_type);
				addBlock(block);
			}
			else if (stmt instanceof IASTNullStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTProblemStatement) {
				block = new MPIBlock(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTReturnStatement) {
				IASTReturnStatement rtStmt = (IASTReturnStatement) stmt;
				block = new MPIBlock(rtStmt.getReturnValue(), stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTSwitchStatement) {
				IASTSwitchStatement swStmt = (IASTSwitchStatement) stmt;
				block = new MPIBlock(swStmt.getControllerExpression(), stmt);
				addBlock(block);
				IBlock join = new MPIBlock(null, stmt, Block.exit_join_type);
				addBlock(join);
			}
			else if (stmt instanceof IASTWhileStatement) {
				IASTWhileStatement whStmt = (IASTWhileStatement) stmt;
				block = new MPIBlock(whStmt.getCondition(), stmt);
				addBlock(block);
				IBlock join = new MPIBlock(null, stmt, Block.continue_join_type);
				addBlock(join);
				IBlock exitjoin = new MPIBlock(null, stmt, Block.exit_join_type);
				addBlock(exitjoin);
			}
			return PROCESS_CONTINUE;
		}
	}

	protected void otherOPs() {
		returnBlocks_ = new ArrayList<IBlock>();
		PhiFlowBuilder phi = new PhiFlowBuilder();
		phi.run();
		BreakContinueChecker bcc = new BreakContinueChecker();
		bcc.run();
	}

	class PhiFlowBuilder extends FlowBuilder {

		public void run() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			this.shouldVisitTranslationUnit = true;
			this.shouldVisitExpressions = true;
			prog_.accept(this);
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTBreakStatement) {
				if (traceOn)
					System.out.println("BreakStatement"); //$NON-NLS-1$
				IASTNode parent = stmt.getParent();
				List<IASTIfStatement> branches = new ArrayList<IASTIfStatement>();
				while (true) {
					if (parent instanceof IASTDoStatement ||
							parent instanceof IASTForStatement ||
							parent instanceof IASTWhileStatement) {
						MPIBlock exitjoin = (MPIBlock) getBlock(null,
								(IASTStatement) parent, Block.exit_join_type);
						for (Iterator<IASTIfStatement> i = branches.iterator(); i.hasNext();) {
							IASTIfStatement ifstmt = i.next();
							MPIBlock ifcondblock = (MPIBlock) getBlock(ifstmt.getConditionExpression(), ifstmt);
							PhiEdge(ifcondblock, exitjoin);
						}
						MPIBlock loopcondblock = null;
						if (parent instanceof IASTDoStatement) {
							IASTDoStatement doS = (IASTDoStatement) parent;
							loopcondblock = (MPIBlock) getBlock(doS.getCondition(), doS);
						} else if (parent instanceof IASTForStatement) {
							IASTForStatement forS = (IASTForStatement) parent;
							loopcondblock = (MPIBlock) getBlock(forS.getConditionExpression(), forS);
						} else if (parent instanceof IASTWhileStatement) {
							IASTWhileStatement whileS = (IASTWhileStatement) parent;
							loopcondblock = (MPIBlock) getBlock(whileS.getCondition(), whileS);
						}
						PhiEdge(loopcondblock, exitjoin);
						break;
					}
					else if (parent instanceof IASTSwitchStatement) {
						MPIBlock exitjoin = (MPIBlock) getBlock(null, (IASTStatement) parent,
								Block.exit_join_type);
						IASTSwitchStatement swStmt = (IASTSwitchStatement) parent;
						MPIBlock swcond = (MPIBlock) getBlock(swStmt.getControllerExpression(),
								(IASTStatement) parent);
						PhiEdge(swcond, exitjoin);
						break;
					}
					else if (parent instanceof IASTIfStatement) {
						branches.add((IASTIfStatement) parent);
						parent = parent.getParent();
					}
					else {
						parent = parent.getParent();
					}
				}
			}
			else if (stmt instanceof IASTCaseStatement ||
					stmt instanceof IASTDefaultStatement) {
				if (traceOn)
					System.out.println("CaseStatement or DefaultStatement"); //$NON-NLS-1$
				IASTNode parent = stmt.getParent();
				while (true) {
					if (parent instanceof IASTSwitchStatement)
						break;
					else
						parent = parent.getParent();
				}
				IASTSwitchStatement swStmt = (IASTSwitchStatement) parent;
				MPIBlock swblock = (MPIBlock) getBlock(swStmt.getControllerExpression(),
						(IASTStatement) parent);
				MPIBlock caseblock = (MPIBlock) getBlock(stmt);
				if (caseblock.getPreds().size() > 1) {
					/* Previous case without break joins here */
					PhiEdge(swblock, caseblock);
				}
			}
			else if (stmt instanceof IASTContinueStatement) {
				if (traceOn)
					System.out.println("ContinueStatement"); //$NON-NLS-1$
				IASTNode parent = stmt.getParent();
				List<IASTIfStatement> branches = new ArrayList<IASTIfStatement>();
				while (true) {
					if (parent instanceof IASTDoStatement ||
							parent instanceof IASTForStatement ||
							parent instanceof IASTWhileStatement) {
						MPIBlock continuejoin = (MPIBlock) getBlock(null,
								(IASTStatement) parent, Block.continue_join_type);
						for (Iterator<IASTIfStatement> i = branches.iterator(); i.hasNext();) {
							IASTIfStatement ifS = i.next();
							MPIBlock ifcondblock = (MPIBlock) getBlock(
									ifS.getConditionExpression(), ifS);
							PhiEdge(ifcondblock, continuejoin);
						}
						break;
					}
					else if (parent instanceof IASTIfStatement) {
						branches.add((IASTIfStatement) parent);
						parent = parent.getParent();
					}
					else {
						parent = parent.getParent();
					}
				}
			}
			else if (stmt instanceof IASTDoStatement) {
				if (traceOn)
					System.out.println("DoStatement"); //$NON-NLS-1$
				IASTDoStatement doStmt = (IASTDoStatement) stmt;
				MPIBlock cond = (MPIBlock) getBlock(doStmt.getCondition(), stmt);
				if (doStmt.getBody() != null) {
					MPIBlock first = (MPIBlock) firstBlock(doStmt.getBody());
					PhiEdge(cond, first);
				}
			}
			else if (stmt instanceof IASTForStatement) {
				if (traceOn)
					System.out.println("ForStatement"); //$NON-NLS-1$
				IASTForStatement forStmt = (IASTForStatement) stmt;
				MPIBlock cond = (MPIBlock) getBlock(forStmt.getConditionExpression(), stmt);
				PhiEdge(cond, cond);

			}
			else if (stmt instanceof IASTIfStatement) {
				if (traceOn)
					System.out.println("IfStatement"); //$NON-NLS-1$
				IASTIfStatement ifStmt = (IASTIfStatement) stmt;
				MPIBlock condb = (MPIBlock) getBlock(ifStmt.getConditionExpression(), stmt);
				MPIBlock join = (MPIBlock) getBlock(null, stmt, Block.exit_join_type);
				PhiEdge(condb, join);
			}
			else if (stmt instanceof IASTReturnStatement) {
				if (traceOn)
					System.out.println("ReturnStatement"); //$NON-NLS-1$
				IASTReturnStatement rtStmt = (IASTReturnStatement) stmt;
				MPIBlock rv = (MPIBlock) getBlock(rtStmt.getReturnValue(), stmt);
				returnBlocks_.add(rv);
			}
			else if (stmt instanceof IASTWhileStatement) {
				if (traceOn)
					System.out.println("WhileStatement"); //$NON-NLS-1$
				IASTWhileStatement whStmt = (IASTWhileStatement) stmt;
				MPIBlock cond = (MPIBlock) getBlock(whStmt.getCondition(), stmt);
				PhiEdge(cond, cond);
			}
			return PROCESS_CONTINUE;
		}

		protected void PhiEdge(MPIBlock from, MPIBlock to) {
			if (!to.getCond().contains(from))
				to.getCond().add(from);
			List<IBlock> joins = from.getJoin();
			if (!joins.contains(to)) {
				joins.add(to);
				// System.out.println("Phi edge is added from " + from.getID() + " to " + to.getID());
			}
		}
	}

	class BreakContinueChecker extends ASTVisitor {

		public void run() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			prog_.accept(this);
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTBreakStatement ||
					stmt instanceof IASTContinueStatement) {
				IASTNode parent = stmt.getParent();
				while (parent != prog_) {
					if (parent instanceof IASTIfStatement) {
						break;
					} else {
						parent = parent.getParent();
					}
				}
				if (!(parent instanceof IASTIfStatement))
					return PROCESS_CONTINUE;
				IASTIfStatement ifS = (IASTIfStatement) parent;
				IASTExpression cond = ifS.getConditionExpression();
				MPIBlock condBlock = (MPIBlock) getBlock(cond, ifS);
				if (stmt instanceof IASTBreakStatement)
					condBlock.withBreak = true;
				else
					condBlock.withContinue = true;
			}
			return PROCESS_CONTINUE;
		}
	}

	public void print() {
		for (IBlock b = entry_; b != null; b = b.topNext()) {
			MPIBlock block = (MPIBlock) b;
			block.print();
		}
	}

}
