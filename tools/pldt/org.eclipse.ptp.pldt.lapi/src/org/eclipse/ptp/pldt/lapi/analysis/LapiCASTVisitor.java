/**********************************************************************
 * Copyright (c) 2005,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.analysis;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;
import org.eclipse.ptp.pldt.lapi.Activator;
import org.eclipse.ptp.pldt.lapi.IDs;
import org.eclipse.ptp.pldt.lapi.messages.Messages;

/**
 * This dom-walker collects "artifacts" related to the specific domain <br>
 * (e.g. LAPI). Currently these artifacts include function calls and constants.
 * It add markers to the source file for C code, marking the position of the
 * artifacts found.
 * 
 * This version extends PldtAstVisitor instead of delegating to<br>
 * MpiGeneralASTVisitorBehavior.
 * 
 * @author tibbitts
 * 
 */
public class LapiCASTVisitor extends PldtAstVisitor {
	/**
	 * Note that LAPI APIs and constants differ as to whether they use
	 * "lapi_" or "LAPI_" so we handle that in matchesPrefix() method
	 */
	private static final String PREFIX = "LAPI_"; //$NON-NLS-1$
	boolean traceOn = false;

	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	public LapiCASTVisitor(List<String> includes, String fileName, boolean allowPrefixOnlyMatch, ScanReturn msr) {
		super(includes, fileName, allowPrefixOnlyMatch, msr);
		ARTIFACT_CALL = Messages.LapiCASTVisitor_lapi_call;
		ARTIFACT_CONSTANT = Messages.LapiCASTVisitor_lapi_constant;
	}

	/**
	 * Unused now; the version in PldtAstVisitor is called instead.
	 */
	// @Override
	public int visitORIG(IASTExpression expression) {

		if (expression instanceof IASTFunctionCallExpression) {
			IASTExpression astExpr = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
			String signature = astExpr.getRawSignature();
			if (traceOn)
				System.out.println("LAPI func signature=" + signature);
			if (signature.startsWith(PREFIX)) {
				if (astExpr instanceof IASTIdExpression) {
					IASTName funcName = ((IASTIdExpression) astExpr).getName();
					processFuncName(funcName, astExpr);
				}
			}
		} else if (expression instanceof IASTLiteralExpression) {
			processMacroLiteral((IASTLiteralExpression) expression);

			// calls w/i macro expansion will only fall below here
		} else if (expression instanceof IASTIdExpression) {

			IASTIdExpression idExpr = (IASTIdExpression) expression;
			IASTName name = idExpr.getName(); // LAPI_Amsend
			System.out.println("LAPI idexpr " + name);
			String rawName = name.getRawSignature(); // CHECK() macro call
			String artName = name.toString();
			if (artName.startsWith(PREFIX)) {
				System.out.println("   found " + artName);
			}
			System.out.println("   rawName: " + rawName);
			processFuncName(name, expression);
		}
		return PROCESS_CONTINUE;// Continue with traversing the children of this node.
	}

	/**
	 * needs to be overridden for derived classes that need to dynamically
	 * update the pref store e.g. for the includes path
	 * 
	 * @return
	 */
	@Override
	protected String getIncludesPrefID() {
		return IDs.PREF_INCLUDES;
	}

	/**
	 * needs to be overridden for derived classes that need to dynamically
	 * update the pref store e.g. for the includes path
	 * 
	 * @return
	 */
	@Override
	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * Note we need to actually DO this test now because artifacts hidden inside preprocessor expansions
	 * otherwise bring along all their "friends" of fn calls and ids within the macro expansion that aren't
	 * really artifacts.<br>
	 * Note that LAPI APIs and constants differ as to whether they use "lapi_" or "LAPI_" so we handle that here.
	 */
	@Override
	public boolean matchesPrefix(String name) {
		name = name.toUpperCase();
		return name.startsWith(PREFIX);
	}

}