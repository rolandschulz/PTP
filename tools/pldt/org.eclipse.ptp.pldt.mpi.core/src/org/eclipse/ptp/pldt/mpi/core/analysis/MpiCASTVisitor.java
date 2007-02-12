/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation.
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;

/**
 * This dom-walker collects "artifacts" related to the specific domain <br>
 * (e.g. MPI, OpenMP, etc.). Currently these artifacts include function calls
 * and constants. It add markers to the source file for C code, marking the
 * position of the artifacts found.
 * 
 * @author Beth Tibbitts
 * 
 */
public class MpiCASTVisitor extends PldtAstVisitor {
	private static final String PREFIX = "MPI_";

	private static final boolean traceOn = false;

	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	public MpiCASTVisitor(List mpiIncludes, String fileName, ScanReturn msr) {
		super(mpiIncludes, fileName, msr);
		ARTIFACT_CALL = "MPI Call";
		ARTIFACT_CONSTANT = "MPI Constant";

	}

	/**
	 * Visit an expression node.
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int visit(IASTExpression expression) {
		if (expression instanceof IASTFunctionCallExpression) {
			// in stdmake this is a CASTFunctionCallExpression but implements
			// IASTFUnctionCallExpr, so ok
			IASTExpression astExpr = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
			String signature = astExpr.getRawSignature();
			// note: getRawSig is the name BEFORE being processed by
			// preprocessor!
			// but it seems to be empty if it's different.
			// IASTFunctionCallExpression
			// fce=(IASTFunctionCallExpression)expression;
			// IASTExpression fne=fce.getFunctionNameExpression();

			// can we get post-pre-processor name here?
			if (astExpr instanceof IASTIdExpression) {
				IASTName tempFN = ((IASTIdExpression) astExpr).getName();
				IBinding tempBIND = tempFN.resolveBinding();
				String tempNAME = tempBIND.getName();
				if(traceOn)System.out.println("MCAV name: "+tempNAME+" rawsig: "+signature);
				// if e.g. preprocessor substitution used, use that for function
				// name
				boolean preProcUsed = !signature.equals(tempNAME);
				if (preProcUsed) {
					signature = tempNAME;
				}
			}
			if (signature.startsWith(PREFIX)) {
				if (astExpr instanceof IASTIdExpression) {
					IASTName funcName = ((IASTIdExpression) astExpr).getName();
					// IBinding binding = funcName.resolveBinding();
					// String name=binding.getName();// name ok for stdMake
					processFuncName(funcName, astExpr);
				}
			}
		} else if (expression instanceof IASTLiteralExpression) {
			processMacroLiteral((IASTLiteralExpression) expression);
		} else {
			// Other possibilities? Do we care? Assume not.
			// if(expression instanceof CASTUnaryExpression)
			// if(expression instanceof CASTIdExpression)

		}
		return PROCESS_CONTINUE;
	}
}