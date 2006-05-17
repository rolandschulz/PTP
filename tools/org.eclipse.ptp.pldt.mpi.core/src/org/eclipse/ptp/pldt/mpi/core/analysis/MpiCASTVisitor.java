/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.mpi.core.analysis;


import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;

/**
 * This dom-walker collects "artifacts" related to the specific domain <br>
 * (e.g. MPI, OpenMP, etc.). Currently these artifacts include function calls
 * and constants. It add markers to the source file for C code, marking the
 * position of the artifacts found.
 * 
 * This version extends PldtAstVisitor instead of delegating to<br>
 * MpiGeneralASTVisitorBehavior.
 * 
 */
public class MpiCASTVisitor extends PldtAstVisitor {
	private static final String PREFIX="MPI_";

	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	public MpiCASTVisitor(List mpiIncludes, String fileName, ScanReturn msr) {
		super(mpiIncludes, fileName, msr);
		ARTIFACT_CALL = "MPI Call";
		ARTIFACT_CONSTANT="MPI Constant";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int visit(IASTExpression expression) {
		if (expression instanceof IASTFunctionCallExpression) {
			IASTExpression astExpr = ((IASTFunctionCallExpression) expression)
					.getFunctionNameExpression();
			String signature = astExpr.getRawSignature();
			// System.out.println("func signature=" + signature);
			if (signature.startsWith(PREFIX)) {
				if (astExpr instanceof IASTIdExpression) {
					IASTName funcName = ((IASTIdExpression) astExpr).getName();
					processFuncName(funcName,astExpr);
				}
			}
		} else if (expression instanceof IASTLiteralExpression) {
			processMacroLiteral((IASTLiteralExpression) expression);
		}
		return PROCESS_CONTINUE;
	}
}