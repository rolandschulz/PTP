/**********************************************************************
 * Copyright (c) 2007,2008 IBM Corporation.
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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;

/**
 * Control Flow Graph implementation
 * 
 * @author Yuan Zhang
 * 
 */

public class ControlFlowGraph implements IControlFlowGraph {
	protected IASTStatement prog_;
	protected List<IBlock> BBs_;
	protected IBlock entry_;
	protected IBlock exit_;
	private static final boolean traceOn = false;

	public ControlFlowGraph(IASTStatement prog) {
		this.prog_ = prog;
		BBs_ = new ArrayList<IBlock>();
	}

	/**
	 * Constructing a CFG consists of several steps:
	 * Step 1: collecting all blocks in a function
	 * Step 2: calculating control flows among blocks
	 * Step 3: calculating dominator and post-dominator relations
	 * Step 4: Sort all blocks according to the topological order
	 * Step 5: Other additional operations
	 */
	public void buildCFG() {
		collectBlocks();
		setBlockFlow();
		buildDOM();
		buildPDOM();
		sort();
		otherOPs();
	}

	/** Collecting all blocks in a function */
	protected void collectBlocks() {
		entry_ = new Block();
		exit_ = new Block();
		BasicBlockCollector bc = new BasicBlockCollector();
		bc.run();
	}

	/** Calculating control flows among blocks */
	protected void setBlockFlow() {
		FlowBuilder flowBuilder = new FlowBuilder();
		flowBuilder.run();
	}

	class BasicBlockCollector extends ASTVisitor {

		public void run() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			prog_.accept(this);
		}

		public int visit(IASTStatement stmt)
		{
			IBlock block;
			if (stmt instanceof IASTBreakStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTCaseStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTCompoundStatement) {
			}
			else if (stmt instanceof IASTContinueStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTDeclarationStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTDefaultStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTDoStatement) {
				IASTDoStatement doStmt = (IASTDoStatement) stmt;
				block = new Block(doStmt.getCondition(), stmt);
				addBlock(block);
				IBlock exitjoin = new Block(null, stmt, Block.exit_join_type);
				addBlock(exitjoin);
				IBlock continuejoin = new Block(null, stmt, Block.continue_join_type);
				addBlock(continuejoin);
			}
			else if (stmt instanceof IASTExpressionStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTForStatement) {
				IASTForStatement forStmt = (IASTForStatement) stmt;
				/* The initialization is a statement, and will be added later */
				block = new Block(forStmt.getConditionExpression(), stmt);
				addBlock(block);
				if (forStmt.getIterationExpression() != null) {
					block = new Block(forStmt.getIterationExpression(), stmt);
					addBlock(block);
				}
				IBlock continuejoin = new Block(null, stmt, Block.continue_join_type);
				addBlock(continuejoin);
				IBlock exitjoin = new Block(null, stmt, Block.exit_join_type);
				addBlock(exitjoin);
			}
			else if (stmt instanceof IASTGotoStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTIfStatement) {
				IASTIfStatement ifStmt = (IASTIfStatement) stmt;
				block = new Block(ifStmt.getConditionExpression(), stmt);
				addBlock(block);
				IBlock join = new Block(null, stmt, Block.exit_join_type);
				addBlock(join);
			}
			else if (stmt instanceof IASTLabelStatement) {
				IASTLabelStatement labelS = (IASTLabelStatement) stmt;
				block = new Block(labelS.getName(), stmt, Block.label_type);
				addBlock(block);
			}
			else if (stmt instanceof IASTNullStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTProblemStatement) {
				block = new Block(stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTReturnStatement) {
				IASTReturnStatement rtStmt = (IASTReturnStatement) stmt;
				block = new Block(rtStmt.getReturnValue(), stmt);
				addBlock(block);
			}
			else if (stmt instanceof IASTSwitchStatement) {
				IASTSwitchStatement swStmt = (IASTSwitchStatement) stmt;
				block = new Block(swStmt.getControllerExpression(), stmt);
				addBlock(block);
				IBlock join = new Block(null, stmt, Block.exit_join_type);
				addBlock(join);
			}
			else if (stmt instanceof IASTWhileStatement) {
				IASTWhileStatement whStmt = (IASTWhileStatement) stmt;
				block = new Block(whStmt.getCondition(), stmt);
				addBlock(block);
				IBlock join = new Block(null, stmt, Block.continue_join_type);
				addBlock(join);
				IBlock exitjoin = new Block(null, stmt, Block.exit_join_type);
				addBlock(exitjoin);
			}
			return PROCESS_CONTINUE;
		}
	}

	public class FlowBuilder extends ASTVisitor {
		private boolean exitBlock = false;

		public void run() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			this.shouldVisitTranslationUnit = true;
			this.shouldVisitExpressions = true;
			IBlock first = firstBlock(prog_);
			ControlFlowEdge(entry_, first);
			prog_.accept(this);
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTBreakStatement) {
				/** A break statement flows to a block with exit_join_type */
				if (traceOn)
					System.out.println("BreakStatement"); //$NON-NLS-1$
				IBlock block = getBlock(stmt);
				IASTNode parent = stmt.getParent();
				while (true) {
					if (parent instanceof IASTDoStatement ||
							parent instanceof IASTForStatement ||
							parent instanceof IASTWhileStatement ||
							parent instanceof IASTSwitchStatement) {
						IBlock exitjoin = getBlock(null, (IASTStatement) parent,
								Block.exit_join_type);
						ControlFlowEdge(block, exitjoin);
						break;
					}
					else {
						parent = parent.getParent();
					}
				}
			}
			else if (stmt instanceof IASTCaseStatement ||
					stmt instanceof IASTDefaultStatement) {
				/**
				 * A case(default) statement flows from the switch
				 * condition block or the previous case statement, and
				 * flows to the first statement in the case(default) body.
				 */
				if (traceOn)
					System.out.println("CaseStatement / DefaultStatement"); //$NON-NLS-1$
				IASTNode parent = stmt.getParent();
				while (true) {
					if (parent instanceof IASTSwitchStatement)
						break;
					else
						parent = parent.getParent();
				}
				IASTSwitchStatement swStmt = (IASTSwitchStatement) parent;
				IBlock swblock = getBlock(swStmt.getControllerExpression(),
						(IASTStatement) parent);
				IBlock caseblock = getBlock(stmt);
				ControlFlowEdge(swblock, caseblock);
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(caseblock, next);
			}
			else if (stmt instanceof IASTCompoundStatement) {
			}
			else if (stmt instanceof IASTContinueStatement) {
				/** A continue statement flows to the continue_join_type block */
				if (traceOn)
					System.out.println("ContinueStatement"); //$NON-NLS-1$
				IBlock block = getBlock(stmt);
				IASTNode parent = stmt.getParent();
				while (true) {
					if (parent instanceof IASTDoStatement ||
							parent instanceof IASTForStatement ||
							parent instanceof IASTWhileStatement) {
						IBlock continuejoin = getBlock(null, (IASTStatement) parent,
								Block.continue_join_type);
						ControlFlowEdge(block, continuejoin);
						break;
					}
					else {
						parent = parent.getParent();
					}
				}
			}
			else if (stmt instanceof IASTDeclarationStatement) {
				/**
				 * Except the regular variable declaration statements, the
				 * initializer of a for statement could also be a
				 * declaration statement. In the latter case, the flow
				 * relation is already calculated and therefore nothing
				 * is done here.
				 */
				if (traceOn)
					System.out.println("DeclarationStatement"); //$NON-NLS-1$
				IASTNode parent = stmt.getParent();
				if (parent instanceof IASTForStatement) {
					IASTForStatement forStmt = (IASTForStatement) parent;
					if (forStmt.getInitializerStatement() == stmt)
						return PROCESS_CONTINUE;
				}
				IBlock block = getBlock(stmt);
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(block, next);
			}
			else if (stmt instanceof IASTDoStatement) {
				/**
				 * We calculate four types of control flows here:
				 * (1) the condition block flows to the first block of the loop body
				 * (2) the condition block flows to the exit_join_type block
				 * (3) the continue_join_type block flows to the condition block
				 * (4) the exit_join_type block flows to the next statement after
				 * this do loop
				 */
				if (traceOn)
					System.out.println("DoStatement"); //$NON-NLS-1$
				IASTDoStatement doStmt = (IASTDoStatement) stmt;
				IBlock cond = getBlock(doStmt.getCondition(), stmt);
				if (doStmt.getBody() == null) {
					ControlFlowEdge(cond, cond);
				} else {
					IBlock first = firstBlock(doStmt.getBody());
					ControlFlowEdge(cond, first);
					IBlock continuejoin = getBlock(null, stmt, Block.continue_join_type);
					ControlFlowEdge(continuejoin, cond);
				}
				IBlock exitjoin = getBlock(null, stmt, Block.exit_join_type);
				ControlFlowEdge(cond, exitjoin);
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(exitjoin, next);
			}
			else if (stmt instanceof IASTExpressionStatement) {
				/**
				 * If the expression statement is the initializer of a for
				 * loop, do nothing here. If this statement contains an
				 * "exit" function call, it flows to the exit block of the
				 * current function
				 */
				if (traceOn)
					System.out.println("ExpressionSatement"); //$NON-NLS-1$
				IASTNode parent = stmt.getParent();
				if (parent instanceof IASTForStatement) {
					IASTForStatement forStmt = (IASTForStatement) parent;
					if (forStmt.getInitializerStatement() == stmt)
						return PROCESS_CONTINUE;
				}
				IBlock block = getBlock(stmt);
				IASTExpressionStatement exprStmt = (IASTExpressionStatement) stmt;
				exitBlock = false;
				exprStmt.getExpression().accept(this);
				if (exitBlock) {
					ControlFlowEdge(block, exit_);
				}
				else {
					IBlock next = nextBlock(stmt);
					ControlFlowEdge(block, next);
				}
			}
			else if (stmt instanceof IASTForStatement) {
				/**
				 * In a for loop, the initializer flows to the loop condition,
				 * the loop condition flows to the first statement in the loop
				 * body and the block with exit_join_type. The continue_join_type
				 * block flows to the iterator, and the iterator flows to the
				 * condition block
				 */
				if (traceOn)
					System.out.println("ForStatement"); //$NON-NLS-1$
				IASTForStatement forStmt = (IASTForStatement) stmt;
				IASTStatement initStmt = forStmt.getInitializerStatement();
				IBlock init = getBlock(initStmt);
				IBlock cond = getBlock(forStmt.getConditionExpression(), stmt);
				IBlock iter = null;
				IBlock continuejoin = getBlock(null, stmt, Block.continue_join_type);
				IBlock exitjoin = getBlock(null, stmt, Block.exit_join_type);

				if (forStmt.getIterationExpression() != null)
					iter = getBlock(forStmt.getIterationExpression(), stmt);

				/* The empty initialization will be a Null statement */
				ControlFlowEdge(init, cond);

				if (forStmt.getBody() != null) {
					IBlock first = firstBlock(forStmt.getBody());
					ControlFlowEdge(cond, first);
					if (iter != null) {
						ControlFlowEdge(continuejoin, iter);
						ControlFlowEdge(iter, cond);
					} else
						ControlFlowEdge(continuejoin, cond);
				} else {
					if (iter != null) {
						ControlFlowEdge(cond, iter);
						ControlFlowEdge(iter, cond);
					} else {
						ControlFlowEdge(cond, cond);
					}
				}
				ControlFlowEdge(cond, exitjoin);
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(exitjoin, next);
			}
			else if (stmt instanceof IASTGotoStatement) {
				/** Goto statement flows to the corresponding label statement */
				if (traceOn)
					System.out.println("GotoStatement"); //$NON-NLS-1$
				IASTGotoStatement gotoStmt = (IASTGotoStatement) stmt;
				IBlock label = getBlock(gotoStmt.getName(), gotoStmt, Block.label_type);
				if (label == null) {
					System.out.println("Null Label Error"); //$NON-NLS-1$
				}
				IBlock block = getBlock(gotoStmt);
				ControlFlowEdge(block, label);
			}
			else if (stmt instanceof IASTIfStatement) {
				/**
				 * The if condition block flows to the first block of the
				 * then(else) clause if it is not empty, the exit_join_type
				 * block otherwise
				 */
				if (traceOn)
					System.out.println("IfStatement"); //$NON-NLS-1$
				IASTIfStatement ifStmt = (IASTIfStatement) stmt;
				IBlock condb = getBlock(ifStmt.getConditionExpression(), stmt);
				IBlock join = getBlock(null, stmt, Block.exit_join_type);
				if (ifStmt.getThenClause() != null) {
					IBlock thenb = firstBlock(ifStmt.getThenClause());
					ControlFlowEdge(condb, thenb);
				} else {
					ControlFlowEdge(condb, join);
				}

				if (ifStmt.getElseClause() != null) {
					IBlock elseb = firstBlock(ifStmt.getElseClause());
					ControlFlowEdge(condb, elseb);
				} else {
					ControlFlowEdge(condb, join);
				}
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(join, next);
			}
			else if (stmt instanceof IASTLabelStatement) {
				/**
				 * If there is a nested statement, the label statement flows
				 * to the first block of the nested statement
				 */
				if (traceOn)
					System.out.println("LabelStatement"); //$NON-NLS-1$
				IASTLabelStatement label = (IASTLabelStatement) stmt;
				IBlock block = getBlock(label.getName(), stmt, Block.label_type);
				if (label.getNestedStatement() == null) {
					IBlock next = nextBlock(stmt);
					ControlFlowEdge(block, next);
				} else {
					IBlock next = firstBlock(label.getNestedStatement());
					ControlFlowEdge(block, next);
				}
			}
			else if (stmt instanceof IASTNullStatement) {
				if (traceOn)
					System.out.println("NullStatement"); //$NON-NLS-1$
				IASTNode parent = stmt.getParent();
				if (parent instanceof IASTForStatement) {
					IASTForStatement forStmt = (IASTForStatement) parent;
					if (forStmt.getInitializerStatement() == stmt)
						return PROCESS_CONTINUE;
				}
				IBlock block = getBlock(stmt);
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(block, next);
			}
			else if (stmt instanceof IASTProblemStatement) {
				if (traceOn)
					System.out.println("ProblemStatement"); //$NON-NLS-1$
				IBlock block = getBlock(stmt);
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(block, next);
			}
			else if (stmt instanceof IASTReturnStatement) {
				/* The return statement flows to the exit block */
				if (traceOn)
					System.out.println("ReturnStatement"); //$NON-NLS-1$
				IASTReturnStatement rtStmt = (IASTReturnStatement) stmt;
				IBlock rv = getBlock(rtStmt.getReturnValue(), stmt);
				ControlFlowEdge(rv, exit_);
			}
			else if (stmt instanceof IASTSwitchStatement) {
				/**
				 * The exit_join block of a switch statement flows to the
				 * first block of the next statement
				 */
				if (traceOn)
					System.out.println("SwitchStatement"); //$NON-NLS-1$
				IBlock join = getBlock(null, stmt, Block.exit_join_type);
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(join, next);
			}
			else if (stmt instanceof IASTWhileStatement) {
				/**
				 * The condition block of a while loop flows to the first
				 * statement in the loop body and the exit_join block.
				 * The condition_join block flows to the condition block.
				 */
				if (traceOn)
					System.out.println("WhileStatement"); //$NON-NLS-1$
				IASTWhileStatement whStmt = (IASTWhileStatement) stmt;
				IBlock cond = getBlock(whStmt.getCondition(), stmt);
				IBlock continuejoin = getBlock(null, stmt, Block.continue_join_type);
				IBlock exitjoin = getBlock(null, stmt, Block.exit_join_type);
				if (whStmt.getBody() == null) {
					ControlFlowEdge(cond, cond);
				} else {
					IBlock first = firstBlock(whStmt.getBody());
					ControlFlowEdge(cond, first);
					ControlFlowEdge(continuejoin, cond);
				}
				IBlock next = nextBlock(stmt);
				ControlFlowEdge(cond, exitjoin);
				ControlFlowEdge(exitjoin, next);
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * @return the first block of stmt
		 */
		public IBlock firstBlock(IASTStatement stmt) {

			if (stmt instanceof IASTBreakStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTCaseStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTCompoundStatement) {
				IASTCompoundStatement cmpStmt = (IASTCompoundStatement) stmt;
				IASTStatement[] stmts = cmpStmt.getStatements();
				for (int i = 0; i < stmts.length; i++) {
					if (stmts[i] != null)
						return firstBlock(stmts[i]);
				}
				/*
				 * The compound statement is empty. Return the first block
				 * of the next Statement.
				 */
				return nextBlock(stmt);
			}
			else if (stmt instanceof IASTContinueStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTDeclarationStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTDefaultStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTDoStatement) {
				IASTDoStatement doStmt = (IASTDoStatement) stmt;
				if (doStmt.getBody() != null)
					return firstBlock(doStmt.getBody());
				else
					return getBlock(doStmt.getCondition(), stmt);
			}
			else if (stmt instanceof IASTExpressionStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTForStatement) {
				IASTForStatement forStmt = (IASTForStatement) stmt;
				IASTStatement initS = forStmt.getInitializerStatement();
				if (initS != null)
					return getBlock(initS);
				else
					return getBlock(forStmt.getConditionExpression(), stmt);
			}
			else if (stmt instanceof IASTGotoStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTIfStatement) {
				IASTIfStatement ifStmt = (IASTIfStatement) stmt;
				return getBlock(ifStmt.getConditionExpression(), stmt);
			}
			else if (stmt instanceof IASTLabelStatement) {
				IASTLabelStatement label = (IASTLabelStatement) stmt;
				return getBlock(label.getName(), stmt, Block.label_type);
			}
			else if (stmt instanceof IASTNullStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTProblemStatement) {
				return getBlock(stmt);
			}
			else if (stmt instanceof IASTReturnStatement) {
				IASTReturnStatement rtStmt = (IASTReturnStatement) stmt;
				return getBlock(rtStmt.getReturnValue(), stmt);
			}
			else if (stmt instanceof IASTSwitchStatement) {
				IASTSwitchStatement swStmt = (IASTSwitchStatement) stmt;
				return getBlock(swStmt.getControllerExpression(), stmt);
			}
			else if (stmt instanceof IASTWhileStatement) {
				IASTWhileStatement whStmt = (IASTWhileStatement) stmt;
				return getBlock(whStmt.getCondition(), stmt);
			}
			return null;
		}

		/*
		 * @return
		 */
		public IBlock nextBlock(IASTStatement stmt) {

			IASTNode node = stmt.getParent();
			if (!(node instanceof IASTStatement)) {
				/* empty return statement for void-typed functions */
				return exit_;
			}

			IASTStatement parent = (IASTStatement) node;
			if (parent instanceof IASTCompoundStatement) {
				IASTCompoundStatement cmpStmt = (IASTCompoundStatement) parent;
				IASTStatement[] stmts = cmpStmt.getStatements();
				int i;
				for (i = 0; i < stmts.length; i++) {
					if (stmts[i] == stmt)
						break;
				}
				for (i = i + 1; i < stmts.length; i++) {
					if (stmts[i] != null)
						return firstBlock(stmts[i]);
				}
				/* stmt is the last one in compound statement */
				return nextBlock(parent);
			}
			else if (parent instanceof IASTDeclarationStatement) {
				return nextBlock(parent);
			}
			else if (parent instanceof IASTDoStatement) {
				/*
				 * "stmt" is the body of Do statement, control flows to
				 * the continue join block
				 */
				return getBlock(null, parent, Block.continue_join_type);
			}
			else if (parent instanceof IASTForStatement) {
				/* control flows to the for loop continue join block */
				return getBlock(null, parent, Block.continue_join_type);
			}
			else if (parent instanceof IASTIfStatement) {
				return getBlock(null, parent, Block.exit_join_type);
			}
			else if (parent instanceof IASTLabelStatement) {
				return nextBlock(parent);
			}
			else if (parent instanceof IASTSwitchStatement) {
				return getBlock(null, parent, Block.exit_join_type);
			}
			else if (parent instanceof IASTWhileStatement) {
				return getBlock(null, parent, Block.continue_join_type);
			}
			else
				return nextBlock(parent);
		}

		private void ControlFlowEdge(IBlock from, IBlock to) {
			if (!from.getSuccs().contains(to))
				from.getSuccs().add(to);
			if (!to.getPreds().contains(from))
				to.getPreds().add(from);
		}

		public int visit(IASTExpression expr) {
			if (expr instanceof IASTFunctionCallExpression) {
				IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression) expr;
				IASTExpression funcname = funcExpr.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				if (signature.equals("exit")) { //$NON-NLS-1$
					exitBlock = true;
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}

	protected void addBasicBlock(IBlock block) {
		if (!BBs_.contains(block))
			BBs_.add(block);
	}

	public IBlock getBlock(IASTExpression expr, IASTStatement parent) {
		for (Iterator<IBlock> i = BBs_.iterator(); i.hasNext();) {
			IBlock bb = i.next();
			if (bb.search(expr, parent))
				return bb;
		}
		return null;
	}

	public IBlock getBlock(IASTStatement stmt) {
		for (Iterator<IBlock> i = BBs_.iterator(); i.hasNext();) {
			IBlock bb = i.next();
			if (bb.search(stmt))
				return bb;
		}
		return null;
	}

	public IBlock getBlock(IASTName label) {
		for (Iterator<IBlock> i = BBs_.iterator(); i.hasNext();) {
			IBlock bb = i.next();
			if (bb.search(label))
				return bb;
		}
		return null;
	}

	public IBlock getBlock(IASTNode content, IASTStatement parent, int type) {
		for (Iterator<IBlock> i = BBs_.iterator(); i.hasNext();) {
			Block bb = (Block) i.next();
			if (bb.search(content, parent, type))
				return bb;
		}
		return null;
	}

	public IBlock getEntry() {
		return entry_;
	}

	public IBlock getExit() {
		return exit_;
	}

	public void addBlock(IBlock bb) {
		if (!BBs_.contains(bb))
			BBs_.add(bb);
	}

	@SuppressWarnings("unchecked")
	public void buildDOM() {
		List<IBlock> all = new ArrayList<IBlock>();
		all.add(entry_);
		all.addAll(BBs_);
		all.add(exit_);

		entry_.getDOM().add(entry_);

		Iterator<IBlock> i;
		for (i = BBs_.iterator(); i.hasNext();) {
			i.next().setDOM(all);
		}
		exit_.setDOM(all);

		boolean change = true;
		while (change) {
			change = false;
			for (i = all.iterator(); i.hasNext();) {
				IBlock block = i.next();
				if (block == entry_)
					continue;
				List<IBlock> temp = new ArrayList<IBlock>(all);
				for (Iterator<IBlock> ii = block.getPreds().iterator(); ii.hasNext();) {
					IBlock pred = ii.next();
					temp = intersect(temp, pred.getDOM());
				}
				List<IBlock> D = new ArrayList<IBlock>(temp);
				if (!D.contains(block))
					D.add(block);
				if (!equals(D, block.getDOM())) {
					change = true;
					block.setDOM(D);
				}
			}
		}
	}

	public void buildPDOM() {
		/* TODO */
	}

	@SuppressWarnings("unchecked")
	public List intersect(List A, List B) {
		if (A == null || B == null)
			return null;
		List list = new ArrayList();
		for (Iterator i = A.iterator(); i.hasNext();) {
			Object o = i.next();
			if (B.contains(o))
				list.add(o);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(List A, List B) {
		if (A == null && B == null)
			return true;
		if (A == null && B != null)
			return false;
		if (A != null && B == null)
			return false;
		if (A.size() != B.size())
			return false;
		for (Iterator i = A.iterator(); i.hasNext();) {
			if (!B.contains(i.next()))
				return false;
		}
		return true;
	}

	/**
	 * Sort Blocks in topological order
	 */
	private Stack<IBlock> order;

	protected void sort() {
		List<IBlock> all = new ArrayList<IBlock>();
		all.add(entry_);
		all.addAll(BBs_);
		all.add(exit_);
		for (Iterator<IBlock> i = all.iterator(); i.hasNext();) {
			IBlock block = i.next();
			block.setAttr("color", new Integer(0)); //$NON-NLS-1$
		}
		order = new Stack<IBlock>();
		for (Iterator<IBlock> i = all.iterator(); i.hasNext();) {
			IBlock block = (IBlock) i.next();
			int color = ((Integer) block.getAttr("color")).intValue(); //$NON-NLS-1$
			if (color == 0)
				DFSVisit(block);
		}

		IBlock b1 = order.pop();
		IBlock b2 = null;
		while (!order.empty()) {
			b2 = order.pop();
			b1.setTopNext(b2);
			b1 = b2;
		}
	}

	protected void DFSVisit(IBlock block) {
		block.setAttr("color", new Integer(1)); //gray //$NON-NLS-1$
		for (Iterator<IBlock> i = block.getSuccs().iterator(); i.hasNext();) {
			IBlock succ = i.next();
			if (isBackEdgeSucc(block, succ))
				continue;
			int color = ((Integer) succ.getAttr("color")).intValue(); //$NON-NLS-1$
			if (color == 0) // white
				DFSVisit(succ);
		}
		order.push(block);
	}

	/**
	 * @return "true" if edge (from, to) is a back edge.
	 */
	protected boolean isBackEdgeSucc(IBlock from, IBlock to) {
		return from.getSuccs().contains(to) && from.getDOM().contains(to);
	}

	protected void otherOPs() {
		/* Empty method for future extensions */
	}

	public void print() {
		for (IBlock b = entry_; b != null; b = b.topNext())
			b.print();
	}

}
