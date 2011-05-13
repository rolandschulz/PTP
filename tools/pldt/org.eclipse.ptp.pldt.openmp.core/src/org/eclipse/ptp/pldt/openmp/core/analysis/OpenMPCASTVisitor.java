/**********************************************************************
 * Copyright (c) 2005,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.core.analysis;

import java.util.List;

import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;
import org.eclipse.ptp.pldt.openmp.core.messages.Messages;

/**
 * This dom-walker collects OpenMP related constructs (currently function calls and constants), and add markers to the
 * source file for C code. Currently, it delegates work to MpiGeneralASTVisitorBehavior.
 * 
 */
public class OpenMPCASTVisitor extends PldtAstVisitor
{
	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	public OpenMPCASTVisitor(List<String> includes, String fileName, boolean allowPrefixOnlyMatch, ScanReturn msr)
	{
		super(includes, fileName, allowPrefixOnlyMatch, msr);
		ARTIFACT_CALL = Messages.OpenMPCASTVisitor_OpenMP_Call;
		ARTIFACT_CONSTANT = Messages.OpenMPCASTVisitor_OpenMP_Constant;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	private static final String PREFIX = "omp_"; //$NON-NLS-1$

	@Override
	public boolean matchesPrefix(String name) {
		return name.startsWith(PREFIX);
	}
}