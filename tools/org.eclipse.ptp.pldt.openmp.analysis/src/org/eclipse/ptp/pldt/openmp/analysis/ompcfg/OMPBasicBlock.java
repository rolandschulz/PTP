/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

/**
 * Holds a consolidated (coarse) basic block for concurrency analysis
 * 
 * @author pazel
 */
public class OMPBasicBlock extends OMPCFGNode
{
	protected LinkedList statements_ = new LinkedList(); // contained stmts

	protected IASTName label_ = null; // non-null if labeled block

	protected IASTExpression branchingExpression_ = null;
	protected int branchingType_ = NoBranch;

	public static final int NoBranch = -1;
	public static final int IfBranch = 0;
	public static final int SwitchBranch = 1;
	public static final int WhileBranch = 2;
	public static final int DoBranch = 3;
	public static final int ForBranch = 4;

	/**
	 * OMPBasicBlock - Basic block for OMP CFG
	 * 
	 */
	public OMPBasicBlock()
	{
		super();
	}

	/**
	 * addStatement - add an element to this basic block
	 * 
	 * @param element
	 *            - IASTNode
	 */
	public void addStatement(IASTStatement element)
	{
		statements_.add(element);
	}

	public IASTName getLabel()
	{
		return label_;
	}

	/**
	 * isCase - informs if first statement is a CASE
	 * 
	 * @return boolean
	 */
	public boolean isCase()
	{
		if (statements_.isEmpty())
			return false;
		IASTStatement statement = ((IASTStatement) statements_.getFirst());
		return (statement instanceof IASTCaseStatement);
	}

	public void setBranchingExpression(IASTExpression branch, int type)
	{
		branchingExpression_ = branch;
		branchingType_ = type;
	}

	/**
	 * getStatements - get the current list of statement for the node
	 * 
	 * @return IASTStatement []
	 */
	public IASTStatement[] getStatements()
	{
		IASTStatement[] list = new IASTStatement[statements_.size()];
		int count = 0;
		for (Iterator i = statements_.iterator(); i.hasNext();)
			list[count++] = (IASTStatement) i.next();
		return list;
	}

	public IASTExpression getBranchingExpression() {
		return branchingExpression_;
	}

	public int getBranchingType() {
		return branchingType_;
	}

	protected LinkedList fundStmtsExprs_ = null;

	/**
	 * getFundamentals - break down all statements/exprs to basics for concurrency map
	 * 
	 * @return
	 */
	public LinkedList getFundamentals()
	{
		if (fundStmtsExprs_ != null)
			return fundStmtsExprs_;
		fundStmtsExprs_ = new LinkedList();

		for (Iterator i = statements_.iterator(); i.hasNext();) {
			IASTStatement stmt = (IASTStatement) i.next();
			FundVisitor fv = new FundVisitor(fundStmtsExprs_);
			stmt.accept(fv);
		}

		return fundStmtsExprs_;
	}

	// -------------------------------------------------------------------------
	// FundVisitor
	// -------------------------------------------------------------------------
	protected class FundVisitor extends ASTVisitor
	{
		protected LinkedList list_ = null;

		public FundVisitor(LinkedList list)
		{
			list_ = list;
			shouldVisitStatements = true;
		}

		public int visit(IASTStatement statement) {

			if (statement instanceof IASTIfStatement) {
				processIfStatement((IASTIfStatement) statement);
				return PROCESS_SKIP;
			}
			else if (statement instanceof IASTSwitchStatement) {
				processSwitchStatement((IASTSwitchStatement) statement);
				return PROCESS_SKIP;
			}
			else if (statement instanceof IASTCaseStatement) {
				processCaseStatement((IASTCaseStatement) statement);
				return PROCESS_CONTINUE;
			}
			else if (statement instanceof IASTForStatement) {
				processForStatement((IASTForStatement) statement);
				return PROCESS_SKIP;
			}
			else if (statement instanceof IASTWhileStatement) {
				processWhileStatement((IASTWhileStatement) statement);
				return PROCESS_SKIP;
			}
			else if (statement instanceof IASTDoStatement) {
				processDoStatement((IASTDoStatement) statement);
				return PROCESS_SKIP;
			}
			else if (statement instanceof IASTCompoundStatement) {
				return PROCESS_CONTINUE;
			}
			else if (statement instanceof IASTBreakStatement) {
				list_.add(statement);
				return PROCESS_CONTINUE;
			}
			else if (statement instanceof IASTContinueStatement) {
				list_.add(statement);
				return PROCESS_CONTINUE;
			}
			else if (statement instanceof IASTReturnStatement) {
				list_.add(statement);
				return PROCESS_CONTINUE;
			}

			list_.add(statement);
			return PROCESS_CONTINUE;
		}

		protected void processIfStatement(IASTIfStatement statement)
		{
			list_.add(statement.getConditionExpression());
			if (statement.getThenClause() != null) {
				FundVisitor thenVisitor = new FundVisitor(list_);
				statement.getThenClause().accept(thenVisitor);
			}
			if (statement.getElseClause() != null) {
				FundVisitor elseVisitor = new FundVisitor(list_);
				statement.getElseClause().accept(elseVisitor);
			}
		}

		protected void processSwitchStatement(IASTSwitchStatement statement)
		{
			list_.add(statement.getControllerExpression());
			FundVisitor bodyVisitor = new FundVisitor(list_);
			statement.getBody().accept(bodyVisitor);
		}

		protected void processCaseStatement(IASTCaseStatement statement)
		{
			list_.add(statement.getExpression());
		}

		protected void processForStatement(IASTForStatement statement)
		{
			list_.add(statement.getConditionExpression());
			list_.add(statement.getIterationExpression());
			if (statement.getInitializerStatement() != null) {
				FundVisitor initVisitor = new FundVisitor(list_);
				statement.getInitializerStatement().accept(initVisitor);
			}
			if (statement.getBody() != null) {
				FundVisitor bodyVisitor = new FundVisitor(list_);
				statement.getBody().accept(bodyVisitor);
			}
		}

		protected void processWhileStatement(IASTWhileStatement statement)
		{
			list_.add(statement.getCondition());
			if (statement.getBody() != null) {
				FundVisitor bodyVisitor = new FundVisitor(list_);
				statement.getBody().accept(bodyVisitor);
			}
		}

		protected void processDoStatement(IASTDoStatement statement)
		{
			list_.add(statement.getCondition());
			if (statement.getBody() != null) {
				FundVisitor bodyVisitor = new FundVisitor(list_);
				statement.getBody().accept(bodyVisitor);
			}
		}

	}

}
