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
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory;

import java.io.PrintStream;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.core.resources.IFile;

/**
 * Builds and interfaces map from file location to statement
 * 
 * @author pazel
 * 
 */
public class FileStatementMap extends StatementMap
{
	protected IASTTranslationUnit tu_ = null;
	protected String filename_ = "";

	/**
	 * FileStatementMap - constructor
	 * 
	 * @param file
	 *            - IFile
	 * @throws UnsupportedDialectException
	 */
	public FileStatementMap(IFile file) throws UnsupportedDialectException
	{
		tu_ = CDOM.getInstance().getTranslationUnit(file);
		filename_ = tu_.getFilePath();
	}

	/**
	 * FileStatementMap - constructor
	 * 
	 * @param tu
	 *            - IASTTranslationUnit
	 */
	public FileStatementMap(IASTTranslationUnit tu)
	{
		tu_ = tu;
		filename_ = tu_.getFilePath();
	}

	/**
	 * buildMap - build the actual map
	 * 
	 */
	public void buildMap()
	{
		Visitor v = new Visitor();
		tu_.accept(v);
	}

	private String getShortClassName(Class c)
	{
		String n = c.toString();
		int lastIndex = n.lastIndexOf('.');
		return n.substring(lastIndex + 1);
	}

	// -------------------------------------------------------------------------
	// Visitor - to visit all statements and some expressions
	// -------------------------------------------------------------------------
	protected class Visitor extends ASTVisitor
	{

		public Visitor()
		{
			shouldVisitStatements = true;
		}

		public int visit(IASTStatement statement)
		{

			if (filename_ == null || !(filename_.equals(statement.getContainingFilename())))
				return PROCESS_CONTINUE;

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
				add(statement);
				return PROCESS_CONTINUE;
			}
			else if (statement instanceof IASTContinueStatement) {
				add(statement);
				return PROCESS_CONTINUE;
			}
			else if (statement instanceof IASTReturnStatement) {
				add(statement);
				return PROCESS_CONTINUE;
			}

			add(statement);
			return PROCESS_CONTINUE;
		}

		protected void processIfStatement(IASTIfStatement statement)
		{
			add(statement.getConditionExpression());
			if (statement.getThenClause() != null) {
				Visitor thenVisitor = new Visitor();
				statement.getThenClause().accept(thenVisitor);
			}
			if (statement.getElseClause() != null) {
				Visitor elseVisitor = new Visitor();
				statement.getElseClause().accept(elseVisitor);
			}
		}

		protected void processSwitchStatement(IASTSwitchStatement statement)
		{
			add(statement.getControllerExpression());
			Visitor bodyVisitor = new Visitor();
			statement.getBody().accept(bodyVisitor);
		}

		protected void processCaseStatement(IASTCaseStatement statement)
		{
			add(statement.getExpression());
		}

		protected void processForStatement(IASTForStatement statement)
		{
			add(statement.getConditionExpression());
			add(statement.getIterationExpression());
			if (statement.getInitializerStatement() != null) {
				Visitor initVisitor = new Visitor();
				statement.getInitializerStatement().accept(initVisitor);
			}
			if (statement.getBody() != null) {
				Visitor bodyVisitor = new Visitor();
				statement.getBody().accept(bodyVisitor);
			}
		}

		protected void processWhileStatement(IASTWhileStatement statement)
		{
			add(statement.getCondition());
			if (statement.getBody() != null) {
				Visitor bodyVisitor = new Visitor();
				statement.getBody().accept(bodyVisitor);
			}
		}

		protected void processDoStatement(IASTDoStatement statement)
		{
			add(statement.getCondition());
			if (statement.getBody() != null) {
				Visitor bodyVisitor = new Visitor();
				statement.getBody().accept(bodyVisitor);
			}
		}

		protected void printInformation(IASTNode node, PrintStream out)
		{
			Location l = getLocation(node);
			System.out.println("Statement: " + getShortClassName(node.getClass()) +
					" loc=(" + l.low_ + "," + l.high_ + ")   file=" + node.getContainingFilename());

		}

	}

}
