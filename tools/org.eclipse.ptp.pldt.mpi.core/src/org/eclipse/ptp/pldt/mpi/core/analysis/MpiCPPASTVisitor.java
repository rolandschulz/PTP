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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLinkageSpecification;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUsingDirective;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;

/**
 * This dom-walker collects artifacts (currently function calls and
 * constants), and adds markers to the source file for C++ code.
 * 
 */
public class MpiCPPASTVisitor extends PldtAstVisitor {
	/**
	 * 
	 */
	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	private boolean usingNamespaceMPI;

	public MpiCPPASTVisitor(List mpiIncludes, String fileName, ScanReturn msr) {
		super(mpiIncludes, fileName, msr);
		usingNamespaceMPI = false;
		ARTIFACT_CALL = "MPI Call";
		ARTIFACT_CONSTANT="MPI Constant";
	}

	public int visit(IASTDeclaration declaration) {
		if (declaration instanceof CPPASTUsingDirective) {
			CPPASTUsingDirective cppASTUsingDirective = (CPPASTUsingDirective) declaration;
			if ("MPI".equals(cppASTUsingDirective.getQualifiedName()
					.getRawSignature())) {
				usingNamespaceMPI = true;
			}
		}

		// Workaround of a CDT problem where declaration for [extern "C"...] in
		// C++ code has null file location
		if ((declaration instanceof CPPASTLinkageSpecification)
				&& (declaration.getFileLocation() == null))
			return PROCESS_CONTINUE;

		if (declaration.getFileLocation() == null) {
			System.out.println("MpiCPPASTVisitor.visit(decl): null decl");
		}

		return super.visit(declaration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int visit(IASTExpression expression) {
		// System.out.println("expression=" + expression.getRawSignature());
		if (expression instanceof IASTFunctionCallExpression) {
			IASTExpression astExpr = ((IASTFunctionCallExpression) expression)
					.getFunctionNameExpression();
			String signature = astExpr.getRawSignature();
			// System.out.println("func signature=" + signature);
			// since all MPI symbols start with a certain prefix,
			// we'll do a quick test to cull the search
			if (usingNamespaceMPI || signature.startsWith("MPI_")
					|| signature.startsWith("MPI::")) {
				IASTName funcName = null;
				if (astExpr instanceof CPPASTFieldReference) {
					// This is method call through C++ member functions (e.g.
					// MPI::COMM_WORLD.Get_size)
					CPPASTFieldReference cppFieldReference = (CPPASTFieldReference) astExpr;
					funcName = cppFieldReference.getFieldName();

				} else if (astExpr instanceof IASTIdExpression) {
					// This is C-style direct method call (e.g. MPI_Init, or
					// MPI::Init)
					funcName = ((IASTIdExpression) astExpr).getName();
				}
				processFuncName(funcName, astExpr);
			}
		} else if ((expression instanceof CPPASTIdExpression)
				&& !(expression.getParent() instanceof IASTFunctionCallExpression)) { // this
			// excludes
			// a
			// CPPASTIdExpression acting as
			// method call (e.g. MPI_Init)
			processIdExprAsLiteral((CPPASTIdExpression) expression);
		} else if (expression instanceof IASTLiteralExpression) {
			processMacroLiteral((IASTLiteralExpression) expression);
		}

		return PROCESS_CONTINUE;
	}
}