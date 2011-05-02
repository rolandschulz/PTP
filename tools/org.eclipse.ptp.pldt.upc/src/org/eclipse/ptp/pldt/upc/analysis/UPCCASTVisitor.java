/**********************************************************************
 * Copyright (c) 2008,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc.analysis;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;
import org.eclipse.ptp.pldt.upc.messages.Messages;

/**
 * This dom-walker collects "artifacts" related to the specific domain <br>
 * (e.g. UPC). Currently these artifacts include function calls and constants.
 * It add markers to the source file for C code, marking the position of the
 * artifacts found.
 * 
 * This version extends PldtAstVisitor instead of delegating to<br>
 * MpiGeneralASTVisitorBehavior.
 * 
 * @author tibbitts
 * 
 */
public class UPCCASTVisitor extends PldtAstVisitor {
	private static final String PREFIX = "upc_"; //$NON-NLS-1$

	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	/**
	 * @since 4.0
	 */
	public UPCCASTVisitor(List upcIncludes, String fileName, boolean allowPrefixOnlyMatch, ScanReturn msr) {
		super(upcIncludes, fileName, allowPrefixOnlyMatch, msr);
		ARTIFACT_CALL = Messages.UPCCASTVisitor_upc_call;
		ARTIFACT_CONSTANT = Messages.UPCCASTVisitor_upc_constant;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom
	 * .ast.IASTExpression)
	 */
	@Override
	public int visit(IASTExpression expression) {
		if (expression instanceof IASTFunctionCallExpression) {
			IASTExpression astExpr = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
			String signature = astExpr.getRawSignature();
			// System.out.println("func signature=" + signature);
			if (signature.startsWith(PREFIX)) {
				if (astExpr instanceof IASTIdExpression) {
					IASTName funcName = ((IASTIdExpression) astExpr).getName();
					processFuncName(funcName, astExpr);
				}
			}
		} else if (expression instanceof IASTLiteralExpression) {
			processMacroLiteral((IASTLiteralExpression) expression);
		}
		return PROCESS_CONTINUE;
	}
}