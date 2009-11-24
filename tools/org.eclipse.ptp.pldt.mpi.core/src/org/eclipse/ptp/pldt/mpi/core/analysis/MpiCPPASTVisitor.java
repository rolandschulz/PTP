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
import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;
import org.eclipse.ptp.pldt.mpi.core.Messages;

/**
 * This dom-walker collects artifacts (currently function calls and constants),
 * and adds markers to the source file for C++ code.
 * 
 */
public class MpiCPPASTVisitor extends PldtAstVisitor {
	private static final boolean traceOn = false;

	private static final String PREFIX1 = "MPI_"; //$NON-NLS-1$

	private static final String PREFIX2 = "MPI::"; //$NON-NLS-1$

	{
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
	}

	private boolean usingNamespaceMPI;

	public MpiCPPASTVisitor(List mpiIncludes, String fileName, boolean allowPrefixOnlyMatch, ScanReturn msr) {
		super(mpiIncludes, fileName, allowPrefixOnlyMatch, msr);
		usingNamespaceMPI = false;
		ARTIFACT_CALL = Messages.getString("MpiCPPASTVisitor.mpiCall"); //$NON-NLS-1$
		ARTIFACT_CONSTANT = Messages.getString("MpiCPPASTVisitor.mpiConstant"); //$NON-NLS-1$
	}

	public int visit(IASTDeclaration declaration) {
		if (declaration instanceof CPPASTUsingDirective) {
			CPPASTUsingDirective cppASTUsingDirective = (CPPASTUsingDirective) declaration;
			if ("MPI".equals(cppASTUsingDirective.getQualifiedName().getRawSignature())) { //$NON-NLS-1$
				usingNamespaceMPI = true;
			}
		}

		// Workaround of a CDT problem where declaration for [extern "C"...] in
		// C++ code has null file location
		if ((declaration instanceof CPPASTLinkageSpecification) && (declaration.getFileLocation() == null))
			return PROCESS_CONTINUE;

		if (declaration.getFileLocation() == null) {
			if (traceOn)
				System.out.println("MpiCPPASTVisitor.visit(decl): null decl"); //$NON-NLS-1$
		}

		return super.visit(declaration);
	}

	/**
	 * Visit an expression node
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int visit(IASTExpression expression) {

		if (expression instanceof IASTFunctionCallExpression) {
			IASTExpression astExpr = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
			String signature = astExpr.getRawSignature();
			// note: getRawSig is BEFORE processed by preprocessor!

			// System.out.println("func signature=" + signature);
			// since all MPI symbols start with a certain prefix,
			// we'll do a quick test to cull the search
			if (usingNamespaceMPI || signature.startsWith(PREFIX1) || signature.startsWith(PREFIX2)) {
				IASTName funcName = null;
				if (astExpr instanceof CPPASTFieldReference) {
					// This is method call through C++ member functions (e.g.
					// MPI::COMM_WORLD.Get_size)
					CPPASTFieldReference cppFieldReference = (CPPASTFieldReference) astExpr;
					funcName = cppFieldReference.getFieldName();

				} else if (astExpr instanceof IASTIdExpression) {
					// This is C-style direct method call (e.g. MPI_Init, or
					// MPI::Init)

					// IASTName tempFN = ((IASTIdExpression) astExpr).getName();
					// IBinding tempBIND = tempFN.resolveBinding();
					// String tempNAME=tempBIND.getName(); //
					// signature="MPI::Init" but this is "Init"

					funcName = ((IASTIdExpression) astExpr).getName();
					// String funcNameString=funcName.toString();//MPI::Init
				}
				processFuncName(funcName, astExpr);
			}
		} else if ((expression instanceof CPPASTIdExpression)
				&& !(expression.getParent() instanceof IASTFunctionCallExpression)) { // this
			// excludes a CPPASTIdExpression acting as method call (e.g.
			// MPI_Init)
			processIdExprAsLiteral((CPPASTIdExpression) expression);
		} else if (expression instanceof IASTLiteralExpression) {
			processMacroLiteral((IASTLiteralExpression) expression);
		}

		return PROCESS_CONTINUE;
	}
	@Override
	protected String chooseName(String bindingName, String rawSignature){
		String name=bindingName;
		if(bindingName.length()==0) {
			name=rawSignature;
		}
		if(rawSignature.startsWith(PREFIX2)){
			name=rawSignature;
		}
		return name;
	}
	/**
	 * Don't allow dynamic include path adding for C++ -- too complicated to know if something is an MPI artifact.
	 * Consider implementing this later.
	 */
	@Override
	 public boolean allowIncludePathAdd() {
	    return false;
	  }
	@Override
  public boolean addIncludePath( IPath path,  String name, boolean dontAskAgain) {
	  return false;
	}
}