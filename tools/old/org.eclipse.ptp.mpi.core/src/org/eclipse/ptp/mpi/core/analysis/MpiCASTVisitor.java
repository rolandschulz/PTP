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
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.ptp.mpi.core.ScanReturn;

/**
 * This dom-walker collects MPI related constructs (currently function calls and constants), and add markers to the
 * source file for C code. Currently, it delegates work to MpiGeneralASTVisitorBehavior.
 * 
 * @author xue
 */
public class MpiCASTVisitor extends CASTVisitor
{
	private static final String MPI_PREFIX = "MPI_";
	
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

    public MpiCASTVisitor(List mpiIncludes, String fileName, ScanReturn msr)
    {
        generalMpiVisitorBehavior = new GeneralASTVisitorBehavior(mpiIncludes, fileName, msr);
    }

    public int visit(IASTStatement statement)
    {
        return generalMpiVisitorBehavior.visit(statement);
    }

    public int visit(IASTDeclaration declaration)
    {
        return generalMpiVisitorBehavior.visit(declaration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public int visit(IASTExpression expression)
    {
        if (expression instanceof IASTFunctionCallExpression) {
            IASTExpression astExpr = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
            String signature = astExpr.getRawSignature();
            System.out.println("func signature=" + signature);
            if (signature.startsWith(MPI_PREFIX)) {
                if (astExpr instanceof IASTIdExpression) {
                    IASTName funcName = ((IASTIdExpression) astExpr).getName();
                    generalMpiVisitorBehavior.processFuncName(funcName, astExpr);
                }
            }
        } else if (expression instanceof IASTLiteralExpression) {
            generalMpiVisitorBehavior.processMacroLiteral((IASTLiteralExpression) expression);
        }
        return PROCESS_CONTINUE;
    }
}