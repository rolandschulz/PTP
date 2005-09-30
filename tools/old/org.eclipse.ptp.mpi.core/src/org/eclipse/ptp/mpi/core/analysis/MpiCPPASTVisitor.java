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

package org.eclipse.ptp.mpi.core.analysis;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLinkageSpecification;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUsingDirective;
import org.eclipse.ptp.mpi.core.ScanReturn;

/**
 * This dom-walker collects MPI related constructs (currently function calls and constants), and add markers to the
 * source file for C++ code. Currently, it delegates most work to MpiGeneralASTVisitorBehavior.
 * 
 * @author xue
 */
public class MpiCPPASTVisitor extends CPPASTVisitor
{
    /**
     * 
     */
    {
        this.shouldVisitExpressions = true;
        this.shouldVisitStatements = true;
        this.shouldVisitDeclarations = true;
        this.shouldVisitTranslationUnit = true;
    }

    private GeneralASTVisitorBehavior generalMpiVisitorBehavior;
    private boolean                      usingNamespaceMPI;

    public MpiCPPASTVisitor(List mpiIncludes, String fileName, ScanReturn msr)
    {
        generalMpiVisitorBehavior = new GeneralASTVisitorBehavior(mpiIncludes, fileName, msr);
        usingNamespaceMPI = false;
    }

    public int visit(IASTStatement statement)
    {
        return generalMpiVisitorBehavior.visit(statement);
    }

    public int visit(IASTDeclaration declaration)
    {
        // System.out.println("declaration="+declaration.getRawSignature()+":"+declaration.getClass());

        if (declaration instanceof CPPASTUsingDirective) {
            CPPASTUsingDirective cppASTUsingDirective = (CPPASTUsingDirective) declaration;
            if ("MPI".equals(cppASTUsingDirective.getQualifiedName().getRawSignature())) {
                usingNamespaceMPI = true;
            }
        }

        // Workaround of a CDT problem where declaration for [extern "C"...] in C++ code has null file location
        if ((declaration instanceof CPPASTLinkageSpecification) && (declaration.getFileLocation() == null))
            return PROCESS_CONTINUE;

        if (declaration.getFileLocation() == null) {
            System.out.println("null decl");
        }

        return generalMpiVisitorBehavior.visit(declaration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public int visit(IASTExpression expression)
    {
        // System.out.println("expression=" + expression.getRawSignature());
        if (expression instanceof IASTFunctionCallExpression) {
            IASTExpression astExpr = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
            String signature = astExpr.getRawSignature();
            System.out.println("func signature=" + signature);
            if (usingNamespaceMPI || signature.startsWith("MPI_") || signature.startsWith("MPI::")) {
                IASTName funcName = null;
                if (astExpr instanceof CPPASTFieldReference) {
                    // This is method call through C++ member functions (e.g. MPI::COMM_WORLD.Get_size)
                    CPPASTFieldReference cppFieldReference = (CPPASTFieldReference) astExpr;
                    funcName = cppFieldReference.getFieldName();

                } else if (astExpr instanceof IASTIdExpression) {
                    // This is C-style direct method call (e.g. MPI_Init, or MPI::Init)
                    funcName = ((IASTIdExpression) astExpr).getName();
                }
                generalMpiVisitorBehavior.processFuncName(funcName, astExpr);
            }
        } else if ((expression instanceof CPPASTIdExpression)
                && !(expression.getParent() instanceof IASTFunctionCallExpression)) { // this excludes a
            // CPPASTIdExpression acting as
            // method call (e.g. MPI_Init)
            generalMpiVisitorBehavior.processIdExprAsLiteral((CPPASTIdExpression) expression);
        } else if (expression instanceof IASTLiteralExpression) {
            generalMpiVisitorBehavior.processMacroLiteral((IASTLiteralExpression) expression);
        }

        return PROCESS_CONTINUE;
    }
}