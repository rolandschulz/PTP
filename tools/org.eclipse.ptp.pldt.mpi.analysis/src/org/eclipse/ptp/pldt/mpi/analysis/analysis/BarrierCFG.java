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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.Block;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.ControlFlowGraph;

/**
 * Barrier Control Flow Graph
 */
public class BarrierCFG extends ControlFlowGraph {

	public BarrierCFG(IASTStatement prog) {
		super(prog);
	}

	protected void collectBlocks() {
		entry_ = new Block();
		exit_ = new Block();
		BarrierCFGBlockCollector bc = new BarrierCFGBlockCollector();
		bc.run();
	}

	class BarrierCFGBlockCollector extends ASTVisitor {

		public void run() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			prog_.accept(this);
		}

		/*
		public int visit(IASTStatement stmt) 
		{
			IBlock block;
			if(stmt instanceof IASTBreakStatement){
				block = new BarrierCFGBlock(stmt, stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTCaseStatement){
				block = new BarrierCFGBlock(stmt, stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTCompoundStatement){
			} 
			else if(stmt instanceof IASTContinueStatement){
				block = new BarrierCFGBlock(stmt, stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTDeclarationStatement){
				block = new BarrierCFGBlock(stmt, stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTDefaultStatement){
				block = new BarrierCFGBlock(stmt, stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTDoStatement){
				block = new BarrierCFGBlock(stmt, stmt);
				addBlock(block);
				return PROCESS_SKIP;
			} 
			else if(stmt instanceof IASTExpressionStatement){
				block = new BarrierCFGBlock(stmt, stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTForStatement){
				block = new BarrierCFGBlock(stmt, stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTGotoStatement){
				block = new Block(stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTIfStatement){
				IASTIfStatement ifStmt = (IASTIfStatement)stmt;
				block = new Block(ifStmt.getConditionExpression(), stmt);
				addBlock(block);
				IBlock join = new Block(null, stmt);
				addBlock(join);
			} 
			else if(stmt instanceof IASTLabelStatement){
				IASTLabelStatement label = (IASTLabelStatement)stmt;
				block = new Block(label.getName(), stmt, Block.label_type);
				addBlock(block);
			} 
			else if(stmt instanceof IASTNullStatement){
				block = new Block(stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTProblemStatement){
				block = new Block(stmt);
				addBlock(block);
			}
			else if(stmt instanceof IASTReturnStatement){
				IASTReturnStatement rtStmt = (IASTReturnStatement)stmt;
				block = new Block(rtStmt.getReturnValue(), stmt);
				addBlock(block);
			} 
			else if(stmt instanceof IASTSwitchStatement){
				IASTSwitchStatement swStmt = (IASTSwitchStatement)stmt;
				block = new Block(swStmt.getControllerExpression(), stmt);
				addBlock(block);
				IBlock join = new Block(null, stmt);
				addBlock(join);
			} 
			else if(stmt instanceof IASTWhileStatement){
				block = new Block(stmt);
				addBlock(block);
			} 
			return PROCESS_CONTINUE;
		} */
	}
}
